package com.lukatu.agenerator.converter;

import com.lukatu.agenerator.Event;
import com.lukatu.agenerator.Screen;
import com.lukatu.agenerator.Utils;
import com.lukatu.agenerator.label.LabelEnum;
import com.lukatu.agenerator.label.LabelRaw;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

/**
 * Created by vberegovoy on 06.12.16.
 */
public class ConverterJava extends Converter {
    public final String packageName;
    public final String className;
    public final String path;

    public ConverterJava(String packageName, String className, String path) {
        super("Java");
        this.packageName = packageName;
        this.className = className;
        this.path = path;
    }

    @Override
    public List<File> convert(List<Screen> screens) throws IOException {
        final File file = new File(path, className + ".java");
        final FileOutputStream fos = new FileOutputStream(file);
        final StringBuilder sb = new StringBuilder(10000);

        sb.append("package ").append(packageName).append(";\n\n");

        sb.append("public final class ").append(className).append(" {\n");

        for (Screen screen : screens) {
            //variable
            sb.append("\tpublic final ").append(screen.name).append(' ').append(Utils.fieldName(screen.name))
                    .append(";\n");
        }
        sb.append("\n\tpublic ").append(className).append("(Bridge bridge) {\n");
        for (Screen screen : screens) {
            //variable
            sb.append("\t\t").append(Utils.fieldName(screen.name))
                    .append(" = new ").append(screen.name).append("(bridge);\n");
        }

        sb.append("\t}\n\n");

        sb.append("\tpublic interface Bridge {\n");
        sb.append("\t\tvoid screen(String screenName);\n\n");
        sb.append("\t\tvoid event(String eventName, String label);\n");
        sb.append("\t}\n\n");

        sb.append("\tpublic static class Screen {\n");
        sb.append("\t\tpublic final String name;\n\n");
        sb.append("\t\tprotected final Bridge bridge;\n\n");
        sb.append("\t\tprotected Screen(String name, Bridge bridge) {\n");
        sb.append("\t\t\tthis.name = name;\n");
        sb.append("\t\t\tthis.bridge = bridge;\n");
        sb.append("\t\t}\n\n");
        sb.append("\t\tpublic final void shown() {\n");
        sb.append("\t\t\tbridge.screen(name);\n");
        sb.append("\t\t}\n");
        sb.append("\t}");

        for (Screen screen : screens) {
            //class for variable
            sb.append("\n\n\tpublic static final class ").append(screen.name).append(" extends Screen {\n");
            sb.append("\t\tprivate ").append(screen.name).append("(Bridge bridge) {\n");
            sb.append("\t\t\tsuper(\"").append(screen.name).append("\", bridge);\n");
            sb.append("\t\t}\n");
            //methods
            for (Event event : screen.events) {
                if (event.label != null) {
                    if (event.label instanceof LabelEnum) {
                        final LabelEnum labelEnum = (LabelEnum) event.label;
                        sb.append("\n\t\tpublic enum ").append(event.name).append(" {\n");
                        final int labelValuesCount = labelEnum.values.size();
                        for (int i = 0; ; ) {
                            sb.append("\t\t\t").append(labelEnum.values.get(i));
                            ++i;
                            if (i == labelValuesCount) {
                                break;
                            }
                            sb.append(",\n");
                        }
                        sb.append("\n\t\t}\n");
                    }
                }

                sb.append('\n');
                sb.append("\t\tpublic final void ").append(Utils.methodName(event.name));
                final String labelVarName;
                final String labelVarInCallName;
                if (event.label == null) {
                    sb.append("() {\n");
                    labelVarName = labelVarInCallName = null;
                } else {
                    if (event.label instanceof LabelEnum) {
                        sb.append("(").append(event.name);
                        labelVarName = "label";
                        labelVarInCallName = labelVarName + ".name()";
                    } else if (event.label instanceof LabelRaw) {
                        final LabelRaw labelRaw = (LabelRaw) event.label;
                        labelVarName = labelRaw.name;
                        labelVarInCallName = "String.valueOf("+labelVarName+")";
                        sb.append("(").append(javaType(labelRaw.type));
                    } else {
                        throw new RuntimeException("wtf?");
                    }
                    sb.append(' ').append(labelVarName).append(") {\n");
                }
                if (labelVarName != null) {
                    sb.append("\t\t\tnotNull(").append(labelVarName).append(");\n");
                }
                sb.append("\t\t\tbridge.event(\"").append(event.name).append("\", ")
                        .append(labelVarInCallName).append(");\n");
                sb.append("\t\t}\n");
            }
            sb.append("\t}");
        }

        sb.append("\n\n\tprivate static void notNull(Object value) {\n");
        sb.append("\t\tif (value == null) throw new NullPointerException();\n");
        sb.append("\t}");


        sb.append("\n}");


        fos.write(sb.toString().getBytes());
        fos.close();


        return Collections.singletonList(file);
    }

    public static String javaType(LabelRaw.Type type) {
        if (LabelRaw.Type.string == type) {
            return "String";
        } else {
            throw new IllegalArgumentException();
        }
    }
}

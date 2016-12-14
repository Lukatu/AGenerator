package com.lukatu.agenerator.converter;

import com.lukatu.agenerator.Event;
import com.lukatu.agenerator.Screen;
import com.lukatu.agenerator.Utils;
import com.lukatu.agenerator.label.LabelEnum;
import com.lukatu.agenerator.label.LabelRaw;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * Created by vberegovoy on 06.12.16.
 */
public class ConverterObjectiveC extends Converter {
    public final String prefix;
    public final String className;
    public final String path;

    public ConverterObjectiveC(String prefix, String className, String path) {
        super("Objective-C");
        this.prefix = prefix;
        this.className = prefix+className;
        this.path = path;
    }

    @Override
    public List<File> convert(List<Screen> screens) throws IOException {
        final String headerFileName = className+".h";
        final File headerFile = new File(path, headerFileName);
        final File implFile = new File(path, className+".m");
        final FileOutputStream headerFos = new FileOutputStream(headerFile);
        final FileOutputStream implFos = new FileOutputStream(implFile);
        final StringBuilder headerSb = new StringBuilder(2000);
        final StringBuilder implSb = new StringBuilder(2000);

        headerSb.append("#import <Foundation/Foundation.h>\n\n");

        headerSb.append("@interface ").append(prefix).append("Screen : NSObject\n");
        headerSb.append("@property(strong, nonatomic, readonly) NSString *name;\n");
        headerSb.append("- (void)shown;\n");
        headerSb.append("@end\n\n");

        for (Screen screen : screens) {
            for (Event event : screen.events) {
                if (event.label instanceof LabelEnum) {
                    final LabelEnum labelEnum = (LabelEnum) event.label;
                    headerSb.append("typedef enum : NSUInteger {\n");
                    final int labelValuesCount = labelEnum.values.size();
                    for (int i = 0; ; ) {
                        headerSb.append("\t").append(prefix).append(labelEnum.values.get(i));
                        ++i;
                        if (i == labelValuesCount) {
                            break;
                        }
                        headerSb.append(",\n");
                    }
                    headerSb.append("\n} ").append(prefix).append(event.name).append(";\n\n");
                }
            }

            headerSb.append("@interface ").append(prefix).append(screen.name).append(" : ")
                    .append(prefix).append("Screen\n");
            for (Event event : screen.events) {
                headerSb.append("- (void)").append(Utils.methodName(event.name));
                if (event.label != null) {
                    if (event.label instanceof LabelRaw) {
                        final LabelRaw labelRaw = (LabelRaw) event.label;
                        headerSb.append(":(").append(objcType(labelRaw.type)).append(") ").append(labelRaw.name);
                    } else if (event.label instanceof LabelEnum) {
                        headerSb.append(":(").append(prefix).append(event.name).append(") label");
                    } else {
                        throw new RuntimeException("wtf?");
                    }
                }
                headerSb.append(";\n");
            }
            headerSb.append("@end\n\n");
        }

        headerSb.append("@protocol ").append(prefix).append("Bridge <NSObject>\n");
        headerSb.append("- (void)screen:(NSString *) screenName;\n");
        headerSb.append("- (void)event:(NSString *) eventName andLabel:(NSString *)label;\n");
        headerSb.append("@end\n\n");

        headerSb.append("@interface ").append(className).append(" : NSObject\n");
        headerSb.append("- (instancetype)initWithBridge:(id<").append(prefix).append("Bridge>) bridge;\n");
        for (Screen screen : screens) {
            headerSb.append("@property(strong, nonatomic, readonly) ").append(prefix)
                    .append(screen.name).append(" *").append(Utils.fieldName(screen.name)).append(";\n");
        }
        headerSb.append("@end\n\n");

        implSb.append("#import \"").append(headerFileName).append("\"\n\n");

        implSb.append("@interface ").append(prefix).append("Screen ()\n");
        implSb.append("@property(strong, nonatomic, readwrite) NSString *name;\n");
        implSb.append("@property(strong, nonatomic, readwrite) id<").append(prefix).append("Bridge> bridge;\n");
        implSb.append("@end\n\n");

        implSb.append("@implementation ").append(prefix).append("Screen\n");
        implSb.append("- (instancetype)initWithName:(NSString *)name andBridge:(id<").append(prefix)
                .append("Bridge>)bridge {\n");
        implSb.append("\tself.name = name;\n");
        implSb.append("\tself.bridge = bridge;\n");
        implSb.append("\treturn self;\n");
        implSb.append("}\n");
        implSb.append("- (void)shown {\n");
        implSb.append("\t[self.bridge screen:self.name];\n");
        implSb.append("}\n");
        implSb.append("+ (void)nonNil:(id) param {\n");
        implSb.append("\tif (param == nil) @throw [NSException exceptionWithName:@\"Illegal argument\" reason:@\"Argument is nil\" userInfo:nil];\n");
        implSb.append("}\n");
        implSb.append("@end\n\n");

        for (Screen screen : screens) {
            implSb.append("@implementation ").append(prefix).append(screen.name).append("\n");
            for (Event event : screen.events) {
                implSb.append("- (void)").append(Utils.methodName(event.name));
                if (event.label != null) {
                    if (event.label instanceof LabelRaw) {
                        final LabelRaw labelRaw = (LabelRaw) event.label;
                        implSb.append(":(").append(objcType(labelRaw.type)).append(") ").append(labelRaw.name);
                    } else if (event.label instanceof LabelEnum) {
                        implSb.append(":(").append(prefix).append(event.name).append(") label");
                    }
                }
                implSb.append(" {\n");
                final String labelVarValue;
                if (event.label == null) {
                    labelVarValue = "nil";
                } else {
                    if (event.label instanceof LabelRaw) {
                        final LabelRaw labelRaw = (LabelRaw) event.label;
                        implSb.append("\t[").append(prefix).append("Screen nonNil: ").append(labelRaw.name).append("];\n");
                        labelVarValue = labelRaw.name;
                    } else if (event.label instanceof LabelEnum) {
                        final LabelEnum labelEnum = (LabelEnum) event.label;
                        labelVarValue = "labelValue";
                        implSb.append("\tNSString *").append(labelVarValue).append(";\n");
                        implSb.append("\tswitch(label) {\n");
                        for (String labelEnumValue : labelEnum.values) {
                            final String labelEnumFine = Utils.suffixName(labelEnumValue);
                            implSb.append("\t\tcase ").append(prefix).append(labelEnumFine).append(":\n");
                            implSb.append("\t\t\t").append(labelVarValue).append(" = @\"").append(labelEnumValue).append("\";\n");
                            implSb.append("\t\t\tbreak;\n");
                        }
                        implSb.append("\t\tdefault:\n");
                        implSb.append("\t\t\t@throw [NSException exceptionWithName:@\"Illegal enum value\" reason:@\"Unknown enum value\" userInfo:nil];\n");
                        implSb.append("\t}\n");
                    } else {
                        throw new RuntimeException("wtf?");
                    }
                }
                implSb.append("\t[self.bridge event:@\"").append(event.name).append("\" andLabel:")
                        .append(labelVarValue).append("];\n");
                implSb.append("}\n");
            }
            implSb.append("@end\n\n");
        }

        implSb.append("@interface ").append(className).append(" ()\n");
        for (Screen screen : screens) {
            implSb.append("@property(strong, nonatomic, readwrite) ").append(prefix)
                    .append(screen.name).append(" *").append(Utils.fieldName(screen.name)).append(";\n");
        }
        implSb.append("@end\n\n");

        implSb.append("@implementation ").append(className).append("\n");
        implSb.append("-(instancetype)initWithBridge:(id<").append(prefix).append("Bridge>)bridge {\n");
        for (Screen screen : screens) {
            implSb.append("\tself.").append(Utils.fieldName(screen.name)).append(" = [[").append(prefix)
                    .append(screen.name).append(" alloc] initWithName:@\"").append(screen.name)
                    .append("\" andBridge:bridge];\n");
        }
        implSb.append("\treturn self;\n");
        implSb.append("}\n");
        implSb.append("@end\n\n");

        headerFos.write(headerSb.toString().getBytes());
        implFos.write(implSb.toString().getBytes());
        headerFos.close();
        implFos.close();

        return Arrays.asList(headerFile, implFile);
    }

    public static String objcType(LabelRaw.Type type) {
        if (LabelRaw.Type.string == type) {
            return "NSString *";
        } else {
            throw new IllegalArgumentException();
        }
    }
}

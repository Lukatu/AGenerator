package com.lukatu.agenerator;

import org.apache.commons.cli.*;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Main {
    private static final String LAYOUT_FILE_DEFAULT = "main.json";

    public static void main(String[] args) throws Exception {
        final Option oJavaPackage, oJavaOutput, oObjcOutput;
        final Options options = new Options();
        options.addOption(new Option("l", "layout", true, "Layout file path. Default - " + LAYOUT_FILE_DEFAULT));
        options.addOption(new Option("c", "class", true, "Class name"));
        options.addOption(oJavaPackage = new Option("p", "java_package", true, "Java package"));
        options.addOption(oJavaOutput = new Option("k", "java_output", true, "Java output"));
        options.addOption(new Option("f", "objc_prefix", true, "Objective-C classes prefix"));
        options.addOption(oObjcOutput = new Option("m", "objc_output", true, "Objective-C output"));
        options.addOption(new Option("h", "help", false, "Help :)"));

        final CommandLineParser commandLineParser = new PosixParser();
        final CommandLine commandLine = commandLineParser.parse(options, args);
        if (commandLine.hasOption('h')) {
            HelpFormatter helpFormatter = new HelpFormatter();
            helpFormatter.printHelp("posix", options, true);
            return;
        }

        String layoutFile = commandLine.getOptionValue('l', null);
        if (layoutFile == null) {
            System.out.println("Layout file path not defined. Using default - \""+LAYOUT_FILE_DEFAULT+"\".");
            layoutFile = LAYOUT_FILE_DEFAULT;
        }
        final String className = commandLine.getOptionValue('c');
        final String javaPackage = commandLine.getOptionValue('p', null);
        final String javaOutputDir = commandLine.getOptionValue('k', null);
        final boolean hasJava = javaPackage != null || javaOutputDir != null;

        final String objcPrefix = commandLine.getOptionValue('f', "");
        final String objcOutputDir = commandLine.getOptionValue('m');
        final boolean hasObjC = !objcPrefix.isEmpty() || objcOutputDir != null;

        final List<Converter> converters = new ArrayList<Converter>();
        final List<Screen> screens;
        try {
            if (!hasJava && !hasObjC) {
                throw new E("At least one language must be specified.");
            }

            if (hasJava) {
                if (javaPackage == null) {
                    E.e(oJavaPackage);
                }
                if (javaOutputDir == null) {
                    E.e(oJavaOutput);
                }
                converters.add(new ConverterJava(javaPackage, className, javaOutputDir));
            }
            if (hasObjC) {
                if (objcOutputDir == null) {
                    E.e(oObjcOutput);
                }
                converters.add(new ConverterObjectiveC(objcPrefix, className, objcOutputDir));
            }

            screens = loadLayout(layoutFile);
        } catch (E e) {
            System.err.println(e.getMessage());
            return;
        }




        for (Converter converter : converters) {
            final List<File> files = converter.convert(screens);
            for (File file : files) {
                System.out.println("[DONE] "+converter.name + " -> " +file.getAbsolutePath());
            }
        }
    }

    private static List<Screen> loadLayout(String path) throws Exception {
        final File file = new File(path);
        FileInputStream fin = null;
        final String content;
        try {
            fin = new FileInputStream(file);
            content = Utils.readAll(fin, new StringBuilder((int) file.length()));
        } catch (Exception e) {
            throw new E("File can't be read \""+path+"\".");
        } finally {
            if (fin != null) {
                fin.close();
            }
        }

        final JSONObject json = new JSONObject(content);
        final JSONArray screens = json.getJSONArray("screens");
        final int screenCount = screens.length();
        final ArrayList<Screen> result = new ArrayList<Screen>(screenCount);
        for (int i = 0; i < screenCount; ++i) {
            final JSONObject screen = screens.getJSONObject(i);
            final JSONArray events = screen.getJSONArray("events");
            final int eventsCount = events.length();
            final Event eventsArray[] = new Event[eventsCount];
            for (int j = 0; j < eventsCount; ++j) {
                eventsArray[j] = new Event(events.getString(j));
            }

            result.add(new Screen(screen.getString("name"), eventsArray));
        }

        return result;
    }

    private static class E extends Exception {
        public E(String e) {
            super(e);
        }

        public static void e(Option option) throws E {
            throw new E(option.getLongOpt()+" is empty");
        }
    }
}

package com.lukatu.agenerator;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Created by vberegovoy on 07.12.16.
 */
public class Utils {
    public static String methodName(String name) {
        final char first = name.charAt(0);
        if (Character.isLowerCase(first)) {
            return name;
        } else {
            return Character.toLowerCase(first) + name.substring(1);
        }
    }

    public static String suffixName(String name) {
        final char first = name.charAt(0);
        if (Character.isUpperCase(first)) {
            return name;
        } else {
            return Character.toUpperCase(first) + name.substring(1);
        }
    }

    public static String fieldName(String name) {
        return name.toLowerCase();
    }

    static String readAll(InputStream in) throws Exception {
        return readAll(in, new StringBuilder());
    }

    public static String readAll(InputStream in, StringBuilder sb) throws Exception {
        final BufferedReader reader = new BufferedReader(new InputStreamReader(in));
        String line;
        while ((line = reader.readLine()) != null) {
            sb.append(line);
            sb.append('\n');
        }
        return sb.toString();
    }
}

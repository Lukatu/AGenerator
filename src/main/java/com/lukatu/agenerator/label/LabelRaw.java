package com.lukatu.agenerator.label;

/**
 * Created by vberegovoy on 14.12.16.
 */
public class LabelRaw extends Label {
    public enum Type {
        string;
    }

    public final String name;
    public final Type type;

    public LabelRaw(String name, Type type) {
        this.name = name;
        this.type = type;
    }
}

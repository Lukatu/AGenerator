package com.lukatu.agenerator;

import com.lukatu.agenerator.label.Label;

/**
 * Created by vberegovoy on 06.12.16.
 */
public class Event {
    public final String name;
    public final Label label;

    public Event(String name, Label label) {
        this.name = name;
        this.label = label;
    }
}

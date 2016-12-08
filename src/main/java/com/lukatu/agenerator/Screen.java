package com.lukatu.agenerator;

/**
 * Created by vberegovoy on 06.12.16.
 */
public class Screen {
    public final String name;
    public final Event[] events;

    public Screen(String name, Event... events) {
        this.name = name;
        this.events = events;
    }
}

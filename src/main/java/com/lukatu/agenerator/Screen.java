package com.lukatu.agenerator;

import java.util.List;

/**
 * Created by vberegovoy on 06.12.16.
 */
public class Screen {
    public final String name;
    public final List<Event> events;

    public Screen(String name, List<Event> events) {
        this.name = name;
        this.events = events;
    }
}

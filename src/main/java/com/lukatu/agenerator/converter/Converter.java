package com.lukatu.agenerator.converter;

import com.lukatu.agenerator.Screen;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * Created by vberegovoy on 06.12.16.
 */
public abstract class Converter {
    public final String name;

    protected Converter(String name) {
        this.name = name;
    }

    public abstract List<File> convert(List<Screen> screens) throws IOException;
}

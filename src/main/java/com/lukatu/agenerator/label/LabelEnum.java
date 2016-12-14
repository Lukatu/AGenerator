package com.lukatu.agenerator.label;

import java.util.List;

/**
 * Created by vberegovoy on 14.12.16.
 */
public class LabelEnum extends Label {
    public final List<String> values;

    public LabelEnum(List<String> values) {
        this.values = values;
    }
}

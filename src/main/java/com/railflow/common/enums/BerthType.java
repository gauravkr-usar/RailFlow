package com.railflow.common.enums;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum BerthType {
    LOWER,
    MIDDLE,
    UPPER,
    SIDE_LOWER,
    SIDE_UPPER,
    WINDOW,
    AISLE;

    @JsonCreator
    public static BerthType fromValue(String value) {
        if (value == null || value.isBlank()) return LOWER;
        String normalized = value.trim().toUpperCase().replace(" ", "_").replace("-", "_");
        for (BerthType bt : values()) {
            if (bt.name().equalsIgnoreCase(normalized)) return bt;
        }
        return LOWER;
    }
}

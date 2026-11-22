package com.railflow.common.enums;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum CoachType {
    SL,      // Sleeper
    THREE_A, // 3-tier AC
    TWO_A,   // 2-tier AC
    ONE_A,   // First AC
    CC,      // Chair Car
    EC,      // Executive Chair
    GEN;     // General

    @JsonCreator
    public static CoachType fromValue(String value) {
        if (value == null || value.isBlank()) return THREE_A;
        String normalized = value.trim().toUpperCase().replace("-", "_").replace(" ", "_");
        if (normalized.equals("3A") || normalized.equals("3_A")) return THREE_A;
        if (normalized.equals("2A") || normalized.equals("2_A")) return TWO_A;
        if (normalized.equals("1A") || normalized.equals("1_A")) return ONE_A;
        for (CoachType ct : values()) {
            if (ct.name().equalsIgnoreCase(normalized)) return ct;
        }
        return THREE_A;
    }
}

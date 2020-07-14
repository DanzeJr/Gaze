package com.ecotioco.gaze.data;

import java.io.Serializable;

public enum OrderStatus implements Serializable {
    Submitted,
    Delivering,
    Completed,
    Cancelled;

    public static String getName(int ordinal) {
        return OrderStatus.values()[ordinal].name();
    }

    public static OrderStatus getEnum(int ordinal) {
        return OrderStatus.values()[ordinal];
    }
}

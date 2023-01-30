package com.acikek.calibrated.util;

public interface AccessTicker {

    void setAccessTicks(int accessTicks);

    int getAccessTicks();

    default boolean isAccessing() {
        return getAccessTicks() != 0;
    }
}

package com.acikek.calibrated.util;

public interface RemoteAccessPlayer {

    void setAccessTicks(int accessTicks);

    int getAccessTicks();

    default boolean isAccessing() {
        return getAccessTicks() != 0;
    }
}

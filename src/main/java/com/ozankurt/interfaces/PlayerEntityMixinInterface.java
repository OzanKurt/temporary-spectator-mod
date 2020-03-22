package com.ozankurt.interfaces;

public interface PlayerEntityMixinInterface {
    void startTempSpectate(int ticks);

    void stopTempSpectate();

    int getTempSpectateDuration();
}

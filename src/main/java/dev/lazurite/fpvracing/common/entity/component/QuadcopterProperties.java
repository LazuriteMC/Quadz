package dev.lazurite.fpvracing.common.entity.component;

import dev.lazurite.fpvracing.common.entity.QuadcopterEntity;

public interface QuadcopterProperties extends Controllable, VideoTransmission {
    void setGodMode(boolean godMode);
    boolean isInGodMode();

    void setState(QuadcopterEntity.State state);
    QuadcopterEntity.State getState();
}

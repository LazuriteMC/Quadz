package dev.lazurite.fpvracing.common.util.type;

import dev.lazurite.fpvracing.common.entity.QuadcopterEntity;

public interface QuadcopterState extends Controllable, VideoCapable.Transmitter {
    void setGodMode(boolean godMode);
    boolean isInGodMode();

    void setState(QuadcopterEntity.State state);
    QuadcopterEntity.State getState();
}

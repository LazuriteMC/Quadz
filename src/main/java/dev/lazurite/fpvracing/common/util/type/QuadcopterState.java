package dev.lazurite.fpvracing.common.util.type;

import dev.lazurite.fpvracing.common.entity.QuadcopterEntity;

public interface QuadcopterState extends Controllable, VideoCapable {
    void setGodMode(boolean godMode);
    boolean isInGodMode();

    void setState(QuadcopterEntity.State state);
    QuadcopterEntity.State getState();
}

package dev.lazurite.fpvracing.common.entity.component;

import dev.lazurite.fpvracing.common.util.Frequency;

public interface VideoTransmission {
    void setFrequency(Frequency frequency);
    Frequency getFrequency();

    void setPower(int power);
    int getPower();

    void setFieldOfView(int fieldOfView);
    int getFieldOfView();

    void setCameraAngle(int cameraAngle);
    int getCameraAngle();
}

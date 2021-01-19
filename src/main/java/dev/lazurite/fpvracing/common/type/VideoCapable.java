package dev.lazurite.fpvracing.common.type;

import dev.lazurite.fpvracing.common.util.Frequency;

public interface VideoCapable {
    void setFrequency(Frequency frequency);
    Frequency getFrequency();

    void setPower(int power);
    int getPower();

    void setFieldOfView(int fieldOfView);
    int getFieldOfView();

    void setCameraAngle(int cameraAngle);
    int getCameraAngle();
}

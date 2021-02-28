package dev.lazurite.quadz.common.util.type;

import dev.lazurite.quadz.common.util.Frequency;

public interface VideoCapable {
    interface Transmitter extends VideoCapable {
        void setPower(int power);
        int getPower();

        void setFieldOfView(int fieldOfView);
        int getFieldOfView();

        void setCameraAngle(int cameraAngle);
        int getCameraAngle();
    }

    void setFrequency(Frequency frequency);
    Frequency getFrequency();
}

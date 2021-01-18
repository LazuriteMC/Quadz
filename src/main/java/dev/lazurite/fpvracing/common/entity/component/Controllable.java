package dev.lazurite.fpvracing.common.entity.component;

import dev.lazurite.fpvracing.client.input.InputFrame;

public interface Controllable {
    void setBindId(int bindId);
    int getBindId();

    void setInputFrame(InputFrame frame);
    InputFrame getInputFrame();

    void setRate(float rate);
    void setSuperRate(float superRate);
    void setExpo(float expo);

    float getRate();
    float getSuperRate();
    float getExpo();
}

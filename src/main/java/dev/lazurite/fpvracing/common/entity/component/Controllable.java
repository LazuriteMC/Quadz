package dev.lazurite.fpvracing.common.entity.component;

import dev.lazurite.fpvracing.client.input.InputFrame;

public interface Controllable {
    void setBindId(int bindId);
    int getBindId();

    void setInputFrame(InputFrame frame);
    InputFrame getInputFrame();
}

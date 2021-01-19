package dev.lazurite.fpvracing.common.type;

import dev.lazurite.fpvracing.client.input.InputFrame;

public interface Controllable extends Bindable {
    void setInputFrame(InputFrame frame);
    InputFrame getInputFrame();
}

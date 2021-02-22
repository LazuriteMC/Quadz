package dev.lazurite.fpvracing.common.util.type;

import dev.lazurite.fpvracing.client.input.frame.InputFrame;

public interface Controllable extends Bindable {
    void setInputFrame(InputFrame frame);
    InputFrame getInputFrame();
}

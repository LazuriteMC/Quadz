package dev.lazurite.fpvracing.common.entity.component;

import dev.lazurite.fpvracing.client.input.InputFrame;

public interface Controllable extends Bindable {
    void setInputFrame(InputFrame frame);
    InputFrame getInputFrame();
}

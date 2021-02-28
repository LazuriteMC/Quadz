package dev.lazurite.quadz.common.util.type;

import dev.lazurite.quadz.client.input.frame.InputFrame;

public interface Controllable extends Bindable {
    void setInputFrame(InputFrame frame);
    InputFrame getInputFrame();
}

package dev.lazurite.quadz.common.util.type;

import dev.lazurite.quadz.common.util.Frequency;

public interface VideoDevice {
    void setFrequency(Frequency frequency);
    Frequency getFrequency();
}

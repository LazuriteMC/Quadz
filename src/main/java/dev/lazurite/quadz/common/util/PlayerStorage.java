package dev.lazurite.quadz.common.util;

public interface PlayerStorage {
    void setFrequency(Frequency frequency);
    Frequency getFrequency();

    void setCallSign(String callSign);
    String getCallSign();
}

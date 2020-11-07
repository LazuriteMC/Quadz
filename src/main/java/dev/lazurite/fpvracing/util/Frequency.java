package dev.lazurite.fpvracing.util;

import net.minecraft.nbt.CompoundTag;

public class Frequency {
    public static final int CHANNELS = 8;
    public static final char[] BANDS = {'A', 'B', 'E', 'F', 'R'};
    public static final int[][] FREQUENCY_TABLE = {
            {5865, 5845, 5825, 5805, 5785, 5765, 5745, 5725},
            {5733, 5752, 5771, 5790, 5809, 5828, 5847, 5866},
            {5705, 5685, 5665, 5645, 5885, 5905, 5925, 5945},
            {5740, 5760, 5780, 5800, 5820, 5840, 5860, 5880},
            {5658, 5695, 5732, 5769, 5806, 5843, 5880, 5917}
    };

    private char band;
    private int channel;

    public Frequency() {

    }

    public Frequency(int frequency) {
        band = getBand(frequency);
        channel = getBand(frequency);
    }

    public Frequency(char band, int channel) {
        setBand(band);
        setChannel(channel);
    }

    public void setBand(char band) {
        this.band = band;
    }

    public void setChannel(int channel) {
        this.channel = channel;
    }

    public int getFrequency() {
        return getFrequency(band, channel);
    }

    public char getBand() {
        return band;
    }

    public int getChannel() {
        return channel;
    }

    public void toTag(CompoundTag tag) {
        tag.putInt("band", getBand());
        tag.putInt("channel", getChannel());
    }

    public static Frequency fromTag(CompoundTag tag) {
        return new Frequency((char) tag.getInt("band"), tag.getInt("channel"));
    }

    public static int getFrequency(char band, int channel) {
        for (int i = 0; i < BANDS.length; i++) {
            if (BANDS[i] == band) {
                return FREQUENCY_TABLE[i][channel-1];
            }
        }

        return -1;
    }

    public static char getBand(int frequency) {
        for (int i = 0; i < BANDS.length; i++) {
            for (int j = 0; j < CHANNELS; j++) {
                if (FREQUENCY_TABLE[i][j] == frequency) {
                    return BANDS[i];
                }
            }
        }

        return (char) -1;
    }

    public static int getChannel(int frequency) {
        for (int i = 0; i < BANDS.length; i++) {
            for (int j = 0; j < CHANNELS; j++) {
                if (FREQUENCY_TABLE[i][j] == frequency) {
                    return j + 1;
                }
            }
        }

        return -1;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Frequency)
            return ((Frequency) obj).getFrequency() == this.getFrequency();
        else return false;
    }

    @Override
    public String toString() {
        return "Frequency: " + getFrequency();
    }
}
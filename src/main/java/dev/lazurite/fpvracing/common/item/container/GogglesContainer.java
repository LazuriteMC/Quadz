package dev.lazurite.fpvracing.common.item.container;

import dev.lazurite.fpvracing.FPVRacing;
import dev.lazurite.fpvracing.common.type.VideoCapable;
import dev.lazurite.fpvracing.common.util.Frequency;
import dev.onyxstudios.cca.api.v3.component.ComponentV3;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;

/**
 * A dumping ground for goggles information. Mainly for storing
 * frequency and whether or not the goggles are powered on.
 * @see VideoCapable
 */
public class GogglesContainer implements ComponentV3, VideoCapable {
    private final ItemStack stack;
    private final Frequency frequency;
    private boolean enabled;

    public GogglesContainer(ItemStack stack) {
        this.stack = stack;
        this.frequency = new Frequency();
        this.enabled = false;
    }

    public static GogglesContainer get(ItemStack stack) {
        try {
            return FPVRacing.GOGGLES_CONTAINER.get(stack);
        } catch (Exception e) {
            return null;
        }
    }

    public ItemStack getStack() {
        return this.stack;
    }

    @Override
    public void setFrequency(Frequency frequency) {
        this.frequency.set(frequency);
    }

    @Override
    public Frequency getFrequency() {
        return new Frequency(frequency);
    }

    @Override
    public void setPower(int power) {
    }

    @Override
    public int getPower() {
        return 0;
    }

    @Override
    public void setFieldOfView(int fieldOfView) {
    }

    @Override
    public int getFieldOfView() {
        return 0;
    }

    @Override
    public void setCameraAngle(int cameraAngle) {
    }

    @Override
    public int getCameraAngle() {
        return 0;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isEnabled() {
        return this.enabled;
    }

    @Override
    public void readFromNbt(CompoundTag tag) {
        frequency.setBand((char) tag.getInt("band"));
        frequency.setChannel(tag.getInt("channel"));
        enabled = tag.getBoolean("enabled");
    }

    @Override
    public void writeToNbt(CompoundTag tag) {
        tag.putInt("band", frequency.getBand());
        tag.putInt("channel", frequency.getChannel());
        tag.putBoolean("enabled", enabled);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof GogglesContainer) {
            return ((GogglesContainer) obj).getFrequency().equals(getFrequency());
        }

        return false;
    }
}

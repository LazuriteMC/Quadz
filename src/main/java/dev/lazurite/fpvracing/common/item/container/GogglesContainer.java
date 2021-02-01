package dev.lazurite.fpvracing.common.item.container;

import dev.lazurite.fpvracing.common.util.type.VideoCapable;
import dev.lazurite.fpvracing.common.util.Frequency;
import dev.onyxstudios.cca.api.v3.item.ItemComponent;
import net.minecraft.item.ItemStack;

/**
 * A dumping ground for goggles information. Mainly for storing
 * frequency and whether or not the goggles are powered on.
 * @see VideoCapable
 */
public class GogglesContainer extends ItemComponent implements VideoCapable {
    private final ItemStack stack;

    public GogglesContainer(ItemStack stack) {
        super(stack);
        this.stack = stack;
    }

    public ItemStack getStack() {
        return this.stack;
    }

    @Override
    public void setFrequency(Frequency frequency) {
        putInt("channel", frequency.getChannel());
        putInt("band", frequency.getBand());
    }

    @Override
    public Frequency getFrequency() {
        return new Frequency((char) getInt("band"), getInt("channel"));
    }

    public int getRange() {
        return 80;
    }

    public void setEnabled(boolean enabled) {
        putBoolean("enabled", enabled);
    }

    public boolean isEnabled() {
        return getBoolean("enabled");
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof GogglesContainer) {
            return ((GogglesContainer) obj).getFrequency().equals(getFrequency());
        }

        return false;
    }
}

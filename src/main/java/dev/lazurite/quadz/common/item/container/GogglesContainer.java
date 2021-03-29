package dev.lazurite.quadz.common.item.container;

import dev.lazurite.quadz.Quadz;
import dev.lazurite.quadz.common.util.Frequency;
import dev.lazurite.quadz.common.util.type.VideoDevice;
import dev.onyxstudios.cca.api.v3.item.ItemComponent;
import net.minecraft.item.ItemStack;

/**
 * Stores the frequency and power state of the goggles using the {@link VideoDevice} interface.
 * @see VideoDevice
 */
public class GogglesContainer extends ItemComponent implements VideoDevice {
    public GogglesContainer(ItemStack stack) {
        super(stack, Quadz.GOGGLES_CONTAINER);
    }

    @Override
    public void setFrequency(Frequency frequency) {
        putInt("channel", frequency.getChannel());
        putInt("band", frequency.getBand());
    }

    @Override
    public Frequency getFrequency() {
        if (!this.hasTag("channel")) {
            this.putInt("channel", 1);
        }

        if (!this.hasTag("band")) {
            this.putInt("band", 'R');
        }

        return new Frequency((char) getInt("band"), getInt("channel"));
    }

    public void setEnabled(boolean enabled) {
        putBoolean("enabled", enabled);
    }

    public boolean isEnabled() {
        if (!this.hasTag("enabled")) {
            this.setEnabled(false);
        }

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

package dev.lazurite.quadz.common.item.container;

import dev.lazurite.quadz.Quadz;
import dev.lazurite.quadz.common.util.type.QuadcopterState;
import dev.lazurite.quadz.common.util.Frequency;
import dev.onyxstudios.cca.api.v3.item.ItemComponent;
import net.fabricmc.fabric.api.util.NbtType;
import net.minecraft.item.ItemStack;

/**
 * Stores all relevant quadcopter information in quadcopter spawner items
 * through the {@link QuadcopterState} interface.
 * @see QuadcopterState
 */
public class QuadcopterContainer extends ItemComponent implements QuadcopterState {
    public QuadcopterContainer(ItemStack stack) {
        super(stack, Quadz.QUADCOPTER_CONTAINER);
    }

    @Override
    public void setGodMode(boolean godMode) {
        this.putBoolean("godmode", godMode);
    }

    @Override
    public boolean isInGodMode() {
        if (!this.hasTag("godmode", NbtType.BYTE)) {
            this.setGodMode(false);
        }

        return this.getBoolean("godmode");
    }

    @Override
    public void setBindId(int bindId) {
        this.putInt("bind_id", bindId);
    }

    @Override
    public int getBindId() {
        if (!this.hasTag("bind_id", NbtType.INT)) {
            this.setBindId(-1);
        }

        return this.getInt("bind_id");
    }

    @Override
    public void setFrequency(Frequency frequency) {
        putInt("channel", frequency.getChannel());
        putInt("band", frequency.getBand());
    }

    @Override
    public Frequency getFrequency() {
        if (!this.hasTag("channel", NbtType.INT)) {
            this.putInt("channel", 1);
        }

        if (!this.hasTag("band", NbtType.INT)) {
            this.putInt("band", 'R');
        }

        return new Frequency((char) getInt("band"), getInt("channel"));
    }

    @Override
    public void setCameraAngle(int cameraAngle) {
        this.putInt("camera_angle", cameraAngle);
    }

    @Override
    public int getCameraAngle() {
        if (!this.hasTag("camera_angle", NbtType.INT)) {
            this.setCameraAngle(0);
        }

        return this.getInt("camera_angle");
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof QuadcopterContainer) {
            return getBindId() == ((QuadcopterContainer) obj).getBindId();
        }

        return false;
    }
}

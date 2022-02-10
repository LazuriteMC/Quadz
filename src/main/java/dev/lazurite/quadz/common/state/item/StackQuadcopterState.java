package dev.lazurite.quadz.common.state.item;

import dev.lazurite.quadz.common.state.Quadcopter;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;

/**
 * Stores all relevant quadcopter information in quadcopter spawner items
 * through the {@link Quadcopter} interface.
 * @see Quadcopter
 */
public class StackQuadcopterState implements Quadcopter {
    private final ItemStack stack;
    private final CompoundTag tag;

    public StackQuadcopterState(ItemStack stack) {
        this.stack = stack;
        this.tag = this.stack.getOrCreateTagElement("container");
    }

    @Override
    public void setBindId(int bindId) {
        tag.putInt("bind_id", bindId);
    }

    @Override
    public void setCameraAngle(int cameraAngle) {
        tag.putInt("camera_angle", cameraAngle);
    }

    @Override
    public void setTemplate(String template) {
        tag.putString("template", template);
    }

    @Override
    public void setActive(boolean active) {

    }

    @Override
    public int getBindId() {
        if (!tag.contains("bind_id")) {
            setBindId(-1);
        }

        return tag.getInt("bind_id");
    }

    @Override
    public int getCameraAngle() {
        if (!tag.contains("camera_angle")) {
            setCameraAngle(0);
        }

        return tag.getInt("camera_angle");
    }

    @Override
    public String getTemplate() {
        return tag.getString("template");
    }

    @Override
    public boolean isActive() {
        return false;
    }

    public ItemStack getStack() {
        return this.stack;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof final StackQuadcopterState container) {
            return getBindId() == container.getBindId() &&
                    getCameraAngle() == container.getCameraAngle();
        }

        return false;
    }
}

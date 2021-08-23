package dev.lazurite.quadz.common.state.item;

import dev.lazurite.quadz.common.state.QuadcopterState;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;

/**
 * Stores all relevant quadcopter information in quadcopter spawner items
 * through the {@link QuadcopterState} interface.
 * @see QuadcopterState
 */
public class StackQuadcopterState implements QuadcopterState {
    private final ItemStack stack;
    private final CompoundTag tag;

    public StackQuadcopterState(ItemStack stack) {
        this.stack = stack;
        this.tag = this.stack.getOrCreateSubTag("container");
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

    public ItemStack getStack() {
        return this.stack;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof StackQuadcopterState) {
            StackQuadcopterState container = (StackQuadcopterState) obj;
            return getBindId() == container.getBindId() &&
                    getCameraAngle() == container.getCameraAngle();
        }

        return false;
    }
}

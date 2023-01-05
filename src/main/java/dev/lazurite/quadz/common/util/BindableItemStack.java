package dev.lazurite.quadz.common.util;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;

/**
 * Stores the bind id in the transmitter through the {@link Bindable} interface.
 * @see Bindable
 */
public class BindableItemStack implements Bindable {

    private final ItemStack stack;
    private final CompoundTag tag;

    public BindableItemStack(ItemStack stack) {
        this.stack = stack;
        this.tag = this.stack.getOrCreateTagElement("bindable");
    }

    @Override
    public void setBindId(int bindId) {
        tag.putInt("bind_id", bindId);
    }

    @Override
    public int getBindId() {
        if (!tag.contains("bind_id")) {
            this.setBindId(-1);
        }

        return tag.getInt("bind_id");
    }

    public ItemStack getStack() {
        return this.stack;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof BindableItemStack) {
            return getBindId() == ((BindableItemStack) obj).getBindId();
        }

        return false;
    }

}

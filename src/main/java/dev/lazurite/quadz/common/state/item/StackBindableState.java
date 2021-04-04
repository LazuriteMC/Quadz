package dev.lazurite.quadz.common.state.item;

import dev.lazurite.quadz.common.state.Bindable;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;

/**
 * Stores the bind id in the transmitter through the {@link Bindable} interface.
 * @see Bindable
 */
public class StackBindableState implements Bindable {
    private final ItemStack stack;
    private final CompoundTag tag;

    public StackBindableState(ItemStack stack) {
        this.stack = stack;
        this.tag = this.stack.getOrCreateSubTag("container");
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
        if (obj instanceof StackBindableState) {
            return getBindId() == ((StackBindableState) obj).getBindId();
        }

        return false;
    }
}

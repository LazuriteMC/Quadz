package dev.lazurite.quadz.common.item.container;

import dev.lazurite.quadz.common.util.type.Bindable;
import dev.onyxstudios.cca.api.v3.component.ComponentV3;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;

/**
 * A dumping ground for transmitter information. Really
 * only stores the bind ID.
 * @see Bindable
 */
public class TransmitterContainer implements ComponentV3, Bindable {
    private final ItemStack stack;
    private int bindId;

    public TransmitterContainer(ItemStack stack) {
        this.stack = stack;
    }

    public ItemStack getStack() {
        return this.stack;
    }

    @Override
    public void setBindId(int bindId) {
        this.bindId = bindId;
    }

    @Override
    public int getBindId() {
        return this.bindId;
    }

    @Override
    public void readFromNbt(CompoundTag tag) {
        bindId = tag.getInt("bind_id");
    }

    @Override
    public void writeToNbt(CompoundTag tag) {
        tag.putInt("bind_id", bindId);
    }


    @Override
    public boolean equals(Object obj) {
        if (obj instanceof TransmitterContainer) {
            return getBindId() == ((TransmitterContainer) obj).getBindId();
        }

        return false;
    }
}

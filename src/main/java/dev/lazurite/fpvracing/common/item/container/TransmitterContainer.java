package dev.lazurite.fpvracing.common.item.container;

import dev.lazurite.fpvracing.FPVRacing;
import dev.lazurite.fpvracing.common.entity.component.Bindable;
import dev.onyxstudios.cca.api.v3.component.ComponentV3;
import dev.onyxstudios.cca.api.v3.component.sync.AutoSyncedComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;

public class TransmitterContainer implements ComponentV3, AutoSyncedComponent, Bindable {
    private final ItemStack stack;
    private int bindId;

    public TransmitterContainer(ItemStack stack) {
        this.stack = stack;
    }

    public static TransmitterContainer get(ItemStack stack) {
        try {
            return FPVRacing.TRANSMITTER_CONTAINER.get(stack);
        } catch (Exception e) {
            return null;
        }
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
    public void applySyncPacket(PacketByteBuf buf) {
        bindId = buf.readInt();
    }

    @Override
    public void writeSyncPacket(PacketByteBuf buf, ServerPlayerEntity recipient) {
        buf.writeInt(bindId);
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

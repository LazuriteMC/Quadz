package dev.lazurite.fpvracing.common.item.container;

import dev.lazurite.fpvracing.FPVRacing;
import dev.lazurite.fpvracing.common.util.Frequency;
import dev.onyxstudios.cca.api.v3.component.ComponentV3;
import dev.onyxstudios.cca.api.v3.component.sync.AutoSyncedComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;

public class GogglesContainer implements ComponentV3, AutoSyncedComponent {
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

    public void setFrequency(Frequency frequency) {
        this.frequency.set(frequency);
    }

    public Frequency getFrequency() {
        return new Frequency(frequency);
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isEnabled() {
        return this.enabled;
    }

    @Override
    public void applySyncPacket(PacketByteBuf buf) {
        frequency.setBand((char) buf.readInt());
        frequency.setChannel(buf.readInt());
        enabled = buf.readBoolean();
    }

    @Override
    public void writeSyncPacket(PacketByteBuf buf, ServerPlayerEntity recipient) {
        buf.writeInt(frequency.getBand());
        buf.writeInt(frequency.getChannel());
        buf.writeBoolean(enabled);
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

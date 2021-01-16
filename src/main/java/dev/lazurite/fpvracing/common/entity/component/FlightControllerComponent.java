package dev.lazurite.fpvracing.common.entity.component;

import dev.lazurite.fpvracing.FPVRacing;
import dev.lazurite.fpvracing.client.input.InputFrame;
import dev.onyxstudios.cca.api.v3.component.ComponentV3;
import dev.onyxstudios.cca.api.v3.component.sync.AutoSyncedComponent;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;

public class FlightControllerComponent implements ComponentV3, AutoSyncedComponent {
    private final Entity entity;

    private float rate;
    private float superRate;
    private float expo;

    public FlightControllerComponent(Entity entity) {
        this.entity = entity;
    }

    public static FlightControllerComponent get(Entity entity) {
        try {
            return FPVRacing.FLIGHT_CONTROLLER.get(entity);
        } catch (Exception e) {
            return null;
        }
    }

    public static boolean is(Entity entity) {
        return get(entity) != null;
    }

    public Entity getEntity() {
        return this.entity;
    }

    @Override
    public void applySyncPacket(PacketByteBuf buf) {
        getFrame().set(InputFrame.fromBuffer(buf));
    }

    @Override
    public void writeSyncPacket(PacketByteBuf buf, ServerPlayerEntity recipient) {
        getFrame().toBuffer(buf);
    }

    @Override
    public void readFromNbt(CompoundTag tag) {
        getFrame().set(InputFrame.fromTag(tag));
    }

    @Override
    public void writeToNbt(CompoundTag tag) {
        getFrame().toTag(tag);
    }
}

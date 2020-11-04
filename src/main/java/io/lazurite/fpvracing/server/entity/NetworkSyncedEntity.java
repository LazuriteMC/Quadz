package io.lazurite.fpvracing.server.entity;

import io.lazurite.fpvracing.network.tracker.GenericDataTrackerRegistry;
import io.lazurite.fpvracing.util.TickTimer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Packet;
import net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket;
import net.minecraft.world.World;

public abstract class NetworkSyncedEntity extends Entity {
    private final TickTimer timer;

    public NetworkSyncedEntity(EntityType<?> type, World world, int syncRate) {
        super(type, world);
        this.timer = new TickTimer(syncRate);
    }

    public NetworkSyncedEntity(EntityType<?> type, World world) {
        this(type, world, 5);
    }

    public abstract void sendNetworkPacket();

    @Override
    public void tick() {
        super.tick();

        if (timer.tick()) {
            sendNetworkPacket();
        }
    }

    public <T> void setValue(GenericDataTrackerRegistry.Entry<T> entry, T value) {
        this.getDataTracker().set(entry.getTrackedData(), value);

        if (entry.getConsumer() != null) {
            entry.getConsumer().accept(this, value);
        }
    }

    public <T> T getValue(GenericDataTrackerRegistry.Entry<T> entry) {
        T data = getDataTracker().get(entry.getTrackedData());

        if (data == null) {
            return entry.getFallback();
        }

        return data;
    }

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    protected void writeCustomDataToTag(CompoundTag tag) {
        GenericDataTrackerRegistry.getAll(getClass()).forEach(entry -> GenericDataTrackerRegistry.writeToTag(tag, (GenericDataTrackerRegistry.Entry) entry, getValue(entry)));
    }

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    protected void readCustomDataFromTag(CompoundTag tag) {
        GenericDataTrackerRegistry.getAll(getClass()).forEach(entry -> setValue((GenericDataTrackerRegistry.Entry) entry, GenericDataTrackerRegistry.readFromTag(tag, entry)));
    }

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    protected void initDataTracker() {
        GenericDataTrackerRegistry.getAll(getClass()).forEach(entry -> getDataTracker().startTracking(((GenericDataTrackerRegistry.Entry) entry).getTrackedData(), entry.getFallback()));
    }

    @Override
    public Packet<?> createSpawnPacket() {
        return new EntitySpawnS2CPacket(this);
    }
}

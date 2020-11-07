package dev.lazurite.fpvracing.server.entity;

import dev.lazurite.fpvracing.network.tracker.Config;
import dev.lazurite.fpvracing.network.tracker.GenericDataTrackerRegistry;
import dev.lazurite.fpvracing.util.TickTimer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.nbt.CompoundTag;
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

    public <T> void setValue (Config.Key<T> key, T value) {
        for (GenericDataTrackerRegistry.Entry<T> entry : GenericDataTrackerRegistry.getAll(getClass(), key.getType())) {
            if (entry.getKey().equals(key)) {
                setValue(entry, value);
            }
        }
    }
    public <T> T getValue(GenericDataTrackerRegistry.Entry<T> entry) {
        T data = getDataTracker().get(entry.getTrackedData());

        if (data == null) {
            return entry.getFallback();
        }

        return data;
    }

    public <T> T getValue(Config.Key<T> key) {
        for (GenericDataTrackerRegistry.Entry<T> entry : GenericDataTrackerRegistry.getAll(getClass(), key.getType())) {
            if (entry.getKey().equals(key)) {
                return getValue(entry);
            }
        }

        return null;
    }

    protected abstract void sendNetworkPacket();

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
}

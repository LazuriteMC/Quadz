package io.lazurite.fpvracing.network.tracker;

import io.lazurite.fpvracing.network.tracker.generic.GenericType;
import io.lazurite.fpvracing.physics.entity.PhysicsEntity;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.nbt.CompoundTag;

import java.util.*;

public class GenericDataTrackerRegistry {
    private static final List<Entry<?>> entries = new LinkedList<>();

    public static <T> Entry<T> register(String name, T fallback, GenericType<T> dataType, Class<? extends PhysicsEntity> classType) {
        Entry<T> entry = new Entry<>(name, fallback, dataType, classType);
        entries.add(entry);
        return entry;
    }

    public static <T> void writeToTag(CompoundTag tag, Entry<T> entry, T value) {
        entry.getDataType().toTag(tag, entry.getName(), value);
    }

    public static <T> T readFromTag(CompoundTag tag, Entry<T> entry) {
        return entry.getDataType().fromTag(tag, entry.getName());
    }

    public static List<Entry<?>> getAll() {
        return new LinkedList<>(entries);
    }

    public static List<Entry<?>> getAll(Class<?> classType) {
        List<Entry<?>> out = new LinkedList<>();

        entries.forEach(entry -> {
            if (entry.getEntityType() == classType) {
                out.add(entry);
            }
        });

        return out;
    }

    @SuppressWarnings("unchecked")
    public static <T> List<Entry<T>> getAll(Class<?> entityType, GenericType<T> dataType) {
        List<Entry<T>> out = new LinkedList<>();

        entries.forEach(entry ->  {
            if (entry.getEntityType() == entityType && entry.getDataType() == dataType) {
                out.add((Entry<T>) entry);
            }
        });

        return out;
    }

    public static class Entry<T> {
        private final String name;
        private final TrackedData<T> trackedData;
        private final GenericType<T> dataType;
        private final Class<? extends PhysicsEntity> entityType;
        private final T fallback;

        public Entry(String name, T fallback, GenericType<T> dataType, Class<? extends PhysicsEntity> entityType) {
            this.name = name;
            this.fallback = fallback;
            this.dataType = dataType;
            this.entityType = entityType;
            this.trackedData = DataTracker.registerData(entityType, dataType);
        }

        public String getName() {
            return this.name;
        }

        public T getFallback() {
            return this.fallback;
        }

        public TrackedData<T> getTrackedData() {
            return this.trackedData;
        }

        public GenericType<T> getDataType() {
            return this.dataType;
        }

        public Class<? extends PhysicsEntity> getEntityType() {
            return this.entityType;
        }
    }
}
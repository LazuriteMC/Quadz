package bluevista.fpvracing.network.datatracker;

import bluevista.fpvracing.network.datatracker.datatype.FlyableDataType;
import bluevista.fpvracing.network.datatracker.datatype.FlyableDataTypeRegistry;
import bluevista.fpvracing.server.entities.FlyableEntity;
import bluevista.fpvracing.server.entities.QuadcopterEntity;
import bluevista.fpvracing.util.Frequency;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.nbt.CompoundTag;

import java.util.*;

public class FlyableTrackerRegistry {
    private static final List<Entry<?>> entries = new LinkedList<>();

    public static final Entry<Integer> BIND_ID = FlyableTrackerRegistry.register("bindID",   -1, FlyableDataTypeRegistry.INTEGER, FlyableEntity.class);
    public static final Entry<Integer> PLAYER_ID = FlyableTrackerRegistry.register("playerId", -1, FlyableDataTypeRegistry.INTEGER, FlyableEntity.class);
    public static final Entry<Boolean> GOD_MODE = FlyableTrackerRegistry.register("godMode", false, FlyableDataTypeRegistry.BOOLEAN, FlyableEntity.class);
    public static final Entry<Boolean> NO_CLIP = FlyableTrackerRegistry.register("noClip", false, FlyableDataTypeRegistry.BOOLEAN, FlyableEntity.class);
    public static final Entry<Integer> FIELD_OF_VIEW = FlyableTrackerRegistry.register("fieldOfView", 120, FlyableDataTypeRegistry.INTEGER, FlyableEntity.class);
    public static final Entry<Float> MASS = FlyableTrackerRegistry.register("mass", 1.0F, FlyableDataTypeRegistry.FLOAT, FlyableEntity.class);
    public static final Entry<Integer> SIZE = FlyableTrackerRegistry.register("size", 8, FlyableDataTypeRegistry.INTEGER, FlyableEntity.class);
    public static final Entry<Float> DRAG_COEFFICIENT = FlyableTrackerRegistry.register("dragCoefficient", 0.5F, FlyableDataTypeRegistry.FLOAT, FlyableEntity.class);
    public static final Entry<Frequency> FREQUENCY = FlyableTrackerRegistry.register("frequency", new Frequency('R', 1), FlyableDataTypeRegistry.FREQUENCY, FlyableEntity.class);

    public static final Entry<Float> RATE = FlyableTrackerRegistry.register("rate", 0.5F, FlyableDataTypeRegistry.FLOAT, QuadcopterEntity.class);
    public static final Entry<Float> SUPER_RATE = FlyableTrackerRegistry.register("superRate", 0.5F, FlyableDataTypeRegistry.FLOAT, QuadcopterEntity.class);
    public static final Entry<Float> EXPO = FlyableTrackerRegistry.register("expo", 0.0F, FlyableDataTypeRegistry.FLOAT, QuadcopterEntity.class);
    public static final Entry<Integer> THRUST = FlyableTrackerRegistry.register("thrust", 50, FlyableDataTypeRegistry.INTEGER, QuadcopterEntity.class);
    public static final Entry<Float> THRUST_CURVE = FlyableTrackerRegistry.register("thrustCurve", 0.95F, FlyableDataTypeRegistry.FLOAT, QuadcopterEntity.class);
    public static final Entry<Integer> CAMERA_ANGLE = FlyableTrackerRegistry.register("cameraAngle", 0, FlyableDataTypeRegistry.INTEGER, QuadcopterEntity.class);

    public static <T> Entry<T> register(String name, T fallback, FlyableDataType<T> dataType, Class<? extends FlyableEntity> classType) {
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
    public static <T> List<Entry<T>> getAll(Class<?> entityType, FlyableDataType<T> dataType) {
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
        private final FlyableDataType<T> dataType;
        private final Class<? extends FlyableEntity> entityType;
        private final T fallback;

        public Entry(String name, T fallback, FlyableDataType<T> dataType, Class<? extends FlyableEntity> entityType) {
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

        public FlyableDataType<T> getDataType() {
            return this.dataType;
        }

        public Class<? extends FlyableEntity> getEntityType() {
            return this.entityType;
        }
    }
}
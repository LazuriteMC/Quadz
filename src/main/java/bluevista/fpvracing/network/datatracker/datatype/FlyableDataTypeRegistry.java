package bluevista.fpvracing.network.datatracker.datatype;

import bluevista.fpvracing.config.Config;
import bluevista.fpvracing.network.datatracker.FlyableTrackerRegistry;
import bluevista.fpvracing.util.Frequency;
import bluevista.fpvracing.util.PacketHelper;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.PacketByteBuf;

import java.util.Properties;

public class FlyableDataTypeRegistry {
    public static final FlyableDataType<Integer> INTEGER = new FlyableDataType<Integer>() {
        public void write(PacketByteBuf buf, Integer value) {
            buf.writeInt(value);
        }

        public Integer read(PacketByteBuf buf) {
            return buf.readInt();
        }

        public Integer copy(Integer object) {
            return null;
        }

        public void toTag(CompoundTag tag, String key, Integer value) {
            tag.putInt(key, value);
        }

        public Integer fromTag(CompoundTag tag, String key) {
            return tag.getInt(key);
        }

        public void toConfig(Config config, String key, Integer value) {
            config.getProperties().setProperty(key, value.toString());
        }

        public Integer fromConfig(Config config, String key) {
            return Integer.parseInt(config.getProperties().getProperty(key, "0"));
        }
    };

    public static final FlyableDataType<Float> FLOAT = new FlyableDataType<Float>() {
        public void write(PacketByteBuf buf, Float value) {
            buf.writeFloat(value);
        }

        public Float read(PacketByteBuf buf) {
            return buf.readFloat();
        }

        public Float copy(Float object) {
            return null;
        }

        public void toTag(CompoundTag tag, String key, Float value) {
            tag.putFloat(key, value);
        }

        public Float fromTag(CompoundTag tag, String key) {
            return tag.getFloat(key);
        }

        public void toConfig(Config config, String key, Float value) {
            config.getProperties().setProperty(key, value.toString());
        }

        public Float fromConfig(Config config, String key) {
            return Float.parseFloat(config.getProperties().getProperty(key, "0.0"));
        }
    };

    public static final FlyableDataType<Boolean> BOOLEAN = new FlyableDataType<Boolean>() {
        public void write(PacketByteBuf buf, Boolean value) {
            buf.writeBoolean(value);
        }

        public Boolean read(PacketByteBuf buf) {
            return buf.readBoolean();
        }

        public Boolean copy(Boolean object) {
            return null;
        }

        public void toTag(CompoundTag tag, String key, Boolean value) {
            tag.putBoolean(key, value);
        }

        public Boolean fromTag(CompoundTag tag, String key) {
            return tag.getBoolean(key);
        }

        public void toConfig(Config config, String key, Boolean value) {
            config.getProperties().setProperty(key, value.toString());
        }

        public Boolean fromConfig(Config config, String key) {
            return Boolean.parseBoolean(config.getProperties().getProperty(key, "false"));
        }
    };

    public static final FlyableDataType<Frequency> FREQUENCY = new FlyableDataType<Frequency>() {
        public void write(PacketByteBuf buf, Frequency value) {
            PacketHelper.serializeFrequency(buf, value);
        }

        public Frequency read(PacketByteBuf buf) {
            return PacketHelper.deserializeFrequency(buf);
        }

        public Frequency copy(Frequency object) {
            return null;
        }

        public void toTag(CompoundTag tag, String key, Frequency value) {
            value.toTag(tag);
        }

        public Frequency fromTag(CompoundTag tag, String key) {
            return Frequency.fromTag(tag);
        }

        public void toConfig(Config config, String key, Frequency value) {
            config.getProperties().setProperty("band", String.valueOf(value.getBand()));
            config.getProperties().setProperty("channel", String.valueOf(value.getChannel()));
        }

        public Frequency fromConfig(Config config, String key) {
            Frequency fallback = FlyableTrackerRegistry.FREQUENCY.getFallback();
            Properties properties = config.getProperties();

            return new Frequency(
                    properties.getProperty("band", String.valueOf(fallback.getBand())).charAt(0),
                    Integer.parseInt(properties.getProperty("channel", String.valueOf(fallback.getChannel())))
            );
        }
    };

    public static void register() {
        TrackedDataHandlerRegistry.register(FlyableDataTypeRegistry.INTEGER);
        TrackedDataHandlerRegistry.register(FlyableDataTypeRegistry.FLOAT);
        TrackedDataHandlerRegistry.register(FlyableDataTypeRegistry.BOOLEAN);
        TrackedDataHandlerRegistry.register(FlyableDataTypeRegistry.FREQUENCY);
    }
}

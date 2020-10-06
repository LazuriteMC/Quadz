package bluevista.fpvracing.util;

import bluevista.fpvracing.client.input.AxisValues;
import bluevista.fpvracing.config.Config;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.math.Vec3d;

import javax.vecmath.Quat4f;
import javax.vecmath.Vector3f;

public class PacketHelper {
    public static void serializeVector3f(PacketByteBuf buf, Vector3f vec) {
        buf.writeFloat(vec.x);
        buf.writeFloat(vec.y);
        buf.writeFloat(vec.z);
    }

    public static Vector3f deserializeVector3f(PacketByteBuf buf) {
        return new Vector3f(
                buf.readFloat(),
                buf.readFloat(),
                buf.readFloat()
        );
    }

    public static void serializeVec3d(PacketByteBuf buf, Vec3d vec) {
        buf.writeDouble(vec.x);
        buf.writeDouble(vec.y);
        buf.writeDouble(vec.z);
    }

    public static Vec3d deserializeVec3d(PacketByteBuf buf) {
        return new Vec3d(
                buf.readDouble(),
                buf.readDouble(),
                buf.readDouble()
        );
    }

    public static void serializeQuaternion(PacketByteBuf buf, Quat4f q) {
        buf.writeFloat(q.x);
        buf.writeFloat(q.y);
        buf.writeFloat(q.z);
        buf.writeFloat(q.w);
    }

    public static Quat4f deserializeQuaternion(PacketByteBuf buf) {
        Quat4f q = new Quat4f();
        q.x = buf.readFloat();
        q.y = buf.readFloat();
        q.z = buf.readFloat();
        q.w = buf.readFloat();
        return q;
    }

    public static void serializeConfig(PacketByteBuf buf, Config config) {
        String key = buf.readString(32767); // majik
        buf.writeString(key); // put it back
        if (key.equals(Config.ALL)) {
            for (String option : Config.ALL_OPTIONS) {
                if (Config.INT_KEYS.contains(option)) {
                    buf.writeInt(config.getIntOption(option));
                } else if (Config.FLOAT_KEYS.contains(option)) {
                    buf.writeFloat(config.getFloatOption(option));
                }
            }
        } else if (Config.INT_KEYS.contains(key)) {
            buf.writeInt(config.getIntOption(key));
        } else if (Config.FLOAT_KEYS.contains(key)) {
            buf.writeFloat(config.getFloatOption(key));
        }
    }

    public static Config deserializeConfig(PacketByteBuf buf) {
        Config config = new Config();
        String key = buf.readString(32767); // majik
        if (key.equals(Config.ALL)) {
            for (String option : Config.ALL_OPTIONS) {
                if (Config.INT_KEYS.contains(option)) {
                    config.setOption(option, buf.readInt());
                } else if (Config.FLOAT_KEYS.contains(option)) {
                    config.setOption(option, buf.readFloat());
                }
            }
        } else if (Config.INT_KEYS.contains(key)) {
            config.setOption(key, buf.readInt());
        } else if (Config.FLOAT_KEYS.contains(key)) {
            config.setOption(key, buf.readFloat());
        }
        return config;
    }

    public static void serializeAxisValues(PacketByteBuf buf, AxisValues axisValues) {
        buf.writeFloat(axisValues.currT);
        buf.writeFloat(axisValues.currX);
        buf.writeFloat(axisValues.currY);
        buf.writeFloat(axisValues.currZ);
    }

    public static AxisValues deserializeAxisValues(PacketByteBuf buf) {
        AxisValues axisValues = new AxisValues();
        axisValues.set(
                buf.readFloat(),
                buf.readFloat(),
                buf.readFloat(),
                buf.readFloat());
        return axisValues;
    }
}

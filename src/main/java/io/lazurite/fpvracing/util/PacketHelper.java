package io.lazurite.fpvracing.util;

import io.lazurite.fpvracing.client.input.AxisValues;
import io.lazurite.fpvracing.network.tracker.Config;
import net.minecraft.network.PacketByteBuf;

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
        buf.writeInt(config.size());
        config.forEach((key, value) -> {
            buf.writeString((String) key);
            buf.writeString((String) value);
        });
    }

    public static Config deserializeConfig(PacketByteBuf buf) {
        Config config = new Config();

        int size = buf.readInt();
        for (int i = 0; i < size; i++)
        config.setProperty(buf.readString(32767), buf.readString(32767));

        return config;
    }

    public static void serializeFrequency(PacketByteBuf buf, Frequency frequency) {
        buf.writeChar(frequency.getBand());
        buf.writeInt(frequency.getChannel());
    }

    public static Frequency deserializeFrequency(PacketByteBuf buf) {
        return new Frequency(buf.readChar(), buf.readInt());
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

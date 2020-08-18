package bluevista.fpvracingmod.client.math;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.math.Quaternion;

import javax.vecmath.Quat4f;

public class QuaternionHelper {
    public static void rotateX(Quaternion q, double deg) {
        double radHalfAngle = Math.toRadians(deg) / 2.0;
        Quaternion rot = new Quaternion((float) Math.sin(radHalfAngle), 0.0f, 0.0f, (float) Math.cos(radHalfAngle));
        q.hamiltonProduct(rot);
    }

    public static void rotateY(Quaternion q, double deg) {
        double radHalfAngle = Math.toRadians(deg) / 2.0;
        Quaternion rot = new Quaternion(0.0f, (float) Math.sin(radHalfAngle), 0.0f, (float) Math.cos(radHalfAngle));
        q.hamiltonProduct(rot);
    }

    public static void rotateZ(Quaternion q, double deg) {
        double radHalfAngle = Math.toRadians(deg) / 2.0;
        Quaternion rot = new Quaternion(0.0f, 0.0f, (float) Math.sin(radHalfAngle), (float) Math.cos(radHalfAngle));
        q.hamiltonProduct(rot);
    }

    public static Quaternion quat4fToQuaternion(Quat4f quat) {
        return new Quaternion(quat.x, quat.y, quat.z, quat.w);
    }

    public static Quat4f quaternionToQuat4f(Quaternion quat) {
        return new Quat4f(quat.getX(), quat.getY(), quat.getZ(), quat.getW());
    }

    public static void serialize(Quaternion q, PacketByteBuf buf) {
        buf.writeFloat(q.getX());
        buf.writeFloat(q.getY());
        buf.writeFloat(q.getZ());
        buf.writeFloat(q.getW());
    }

    public static Quaternion deserialize(PacketByteBuf buf) {
        return new Quaternion(
                buf.readFloat(),
                buf.readFloat(),
                buf.readFloat(),
                buf.readFloat()
        );
    }
}

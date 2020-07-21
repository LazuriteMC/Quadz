package bluevista.fpvracingmod.client.math;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.math.Quaternion;

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

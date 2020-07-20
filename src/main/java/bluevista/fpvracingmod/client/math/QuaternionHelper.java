package bluevista.fpvracingmod.client.math;

import net.minecraft.util.math.Quaternion;

public class QuaternionHelper {
    public void rotateX(Quaternion q, double deg) {
        double radHalfAngle = Math.toRadians(deg) / 2.0;
        Quaternion rot = new Quaternion((float) Math.sin(radHalfAngle), 0.0f, 0.0f, (float) Math.cos(radHalfAngle));
        q.hamiltonProduct(rot);
    }

    public void rotateY(Quaternion q, double deg) {
        double radHalfAngle = Math.toRadians(deg) / 2.0;
        Quaternion rot = new Quaternion(0.0f, (float) Math.sin(radHalfAngle), 0.0f, (float) Math.cos(radHalfAngle));
        q.hamiltonProduct(rot);
    }

    public void rotateZ(Quaternion q, double deg) {
        double radHalfAngle = Math.toRadians(deg) / 2.0;
        Quaternion rot = new Quaternion(0.0f, 0.0f, (float) Math.sin(radHalfAngle), (float) Math.cos(radHalfAngle));
        q.hamiltonProduct(rot);
    }

    public void rotateX(NetworkQuaternion q, double deg) {
        double radHalfAngle = Math.toRadians(deg) / 2.0;
        Quaternion rot = new Quaternion((float) Math.sin(radHalfAngle), 0.0f, 0.0f, (float) Math.cos(radHalfAngle));

        Quaternion conv = q.toQuaternion();
        conv.hamiltonProduct(rot);
        q.set(conv.getX(), conv.getY(), conv.getZ(), conv.getW());
    }

    public void rotateY(NetworkQuaternion q, double deg) {
        double radHalfAngle = Math.toRadians(deg) / 2.0;
        Quaternion rot = new Quaternion(0.0f, (float) Math.sin(radHalfAngle), 0.0f, (float) Math.cos(radHalfAngle));

        Quaternion conv = q.toQuaternion();
        conv.hamiltonProduct(rot);
        q.set(conv.getX(), conv.getY(), conv.getZ(), conv.getW());
    }

    public void rotateZ(NetworkQuaternion q, double deg) {
        double radHalfAngle = Math.toRadians(deg) / 2.0;
        Quaternion rot = new Quaternion(0.0f, 0.0f, (float) Math.sin(radHalfAngle), (float) Math.cos(radHalfAngle));

        Quaternion conv = q.toQuaternion();
        conv.hamiltonProduct(rot);
        q.set(conv.getX(), conv.getY(), conv.getZ(), conv.getW());
    }
}

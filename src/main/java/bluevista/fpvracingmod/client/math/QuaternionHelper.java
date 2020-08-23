package bluevista.fpvracingmod.client.math;

import net.minecraft.util.math.Quaternion;

import javax.vecmath.Quat4f;

public class QuaternionHelper {
    public static void rotateX(Quat4f q, double deg) {
        double radHalfAngle = Math.toRadians(deg) / 2.0;
        Quat4f rot = new Quat4f();
        rot.x = (float) Math.sin(radHalfAngle);
        rot.w = (float) Math.cos(radHalfAngle);
        q.mul(rot);
    }

    public static void rotateY(Quat4f q, double deg) {
        double radHalfAngle = Math.toRadians(deg) / 2.0;
        Quat4f rot = new Quat4f();
        rot.y = (float) Math.sin(radHalfAngle);
        rot.w = (float) Math.cos(radHalfAngle);
        q.mul(rot);
    }

    public static void rotateZ(Quat4f q, double deg) {
        double radHalfAngle = Math.toRadians(deg) / 2.0;
        Quat4f rot = new Quat4f();
        rot.z = (float) Math.sin(radHalfAngle);
        rot.w = (float) Math.cos(radHalfAngle);
        q.mul(rot);
    }

    public static Quaternion quat4fToQuaternion(Quat4f quat) {
        return new Quaternion(quat.x, quat.y, quat.z, quat.w);
    }

    public static Quat4f quaternionToQuat4f(Quaternion quat) {
        Quat4f q = new Quat4f();
        q.x = quat.getX();
        q.y = quat.getY();
        q.z = quat.getZ();
        q.w = quat.getW();
        return q;
    }
}

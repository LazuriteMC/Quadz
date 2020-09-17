package bluevista.fpvracingmod.helper;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Quaternion;

import javax.vecmath.Quat4f;
import javax.vecmath.Vector3f;

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

    // roll, pitch, yaw
    public static Vector3f toEulerAngles(Quat4f quat) {
        Quat4f q = new Quat4f();
        q.set(quat.z, quat.x, quat.y, quat.w);

        Vector3f angles = new Vector3f();

        // roll (x-axis rotation)
        double sinr_cosp = 2 * (q.w * q.x + q.y * q.z);
        double cosr_cosp = 1 - 2 * (q.x * q.x + q.y * q.y);
        angles.x = (float) Math.atan2(sinr_cosp, cosr_cosp);

        // pitch (y-axis rotation)
        double sinp = 2 * (q.w * q.y - q.z * q.x);
        if (Math.abs(sinp) >= 1)
            angles.y = (float) Math.copySign(Math.PI / 2, sinp); // use 90 degrees if out of range
        else
            angles.y = (float) Math.asin(sinp);

        // yaw (z-axis rotation)
        double siny_cosp = 2 * (q.w * q.z + q.x * q.y);
        double cosy_cosp = 1 - 2 * (q.y * q.y + q.z * q.z);
        angles.z = (float) Math.atan2(siny_cosp, cosy_cosp);

        return angles;
    }

    public static void toTag(Quat4f quat, CompoundTag tag) {
        tag.putFloat("x", quat.x);
        tag.putFloat("y", quat.y);
        tag.putFloat("z", quat.z);
        tag.putFloat("w", quat.w);
    }

    public static Quat4f fromTag(CompoundTag tag) {
        Quat4f quat = new Quat4f();
        quat.set(tag.getFloat("x"),
                 tag.getFloat("y"),
                 tag.getFloat("z"),
                 tag.getFloat("w"));
        return quat;
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

    public static float getYaw(Quat4f quat) {
        return -1 * (float) Math.toDegrees(QuaternionHelper.toEulerAngles(quat).z);
    }

    public static float getPitch(Quat4f quat) {
        return (float) Math.toDegrees(QuaternionHelper.toEulerAngles(quat).y);
    }
}

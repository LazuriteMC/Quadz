package bluevista.fpvracingmod.client.math;

import net.minecraft.util.math.*;

public class QuaternionHelper {

	public static Quaternion negateRotation(Quaternion quat, Quaternion rot) {
		rot.conjugate();
		quat.hamiltonProduct(rot);
		return quat;
	}

	public static Quaternion rotateX(Quaternion quat, float amount) {
	    double radHalfAngle = Math.toRadians((double) amount) / 2.0;
	    Quaternion rot = new Quaternion((float) Math.sin(radHalfAngle), 0.0f, 0.0f, (float) Math.cos(radHalfAngle));
	    quat.hamiltonProduct(rot);
		return quat;
	}
	
	public static Quaternion rotateY(Quaternion quat, float amount) {
	    double radHalfAngle = Math.toRadians((double) amount) / 2.0;
	    Quaternion rot = new Quaternion(0.0f, (float) Math.sin(radHalfAngle), 0.0f, (float) Math.cos(radHalfAngle));
	    quat.hamiltonProduct(rot);
		return quat;
	}
	
	public static Quaternion rotateZ(Quaternion quat, float amount) {
	    double radHalfAngle = Math.toRadians((double) amount) / 2.0;
	    Quaternion rot = new Quaternion(0.0f, 0.0f, (float) Math.sin(radHalfAngle), (float) Math.cos(radHalfAngle));
	    quat.hamiltonProduct(rot);
	    return quat;
	}

	public static Vec3d getVector(Quaternion q) {
		Vec3d down = new Vec3d(0, -1, 0);
		Vec3d v = new Vec3d(q.getX(), q.getY(), q.getZ());
		float s = q.getW();

		return v.multiply(2.0f * v.dotProduct(down))
				.add(down.multiply(s * s - v.dotProduct(v)))
				.add(v.crossProduct(down).multiply(2.0f * s));
	}

	public static EulerAngle toEulerAngles(Quaternion q) {
		float w = q.getW();
		float x = q.getX();
		float y = q.getY();
		float z = q.getZ();
		float roll, yaw, pitch;

		// roll (x-axis rotation)
		double sinr_cosp = 2 * (w * x + y * z);
		double cosr_cosp = 1 - 2 * (x * x + y * y);
		roll = (float) MathHelper.atan2(sinr_cosp, cosr_cosp);

		// yaw (y-axis rotation)
		double sinp = 2 * (w * y - z * x);
		if (Math.abs(sinp) >= 1)
			yaw = (float) Math.copySign(Math.PI / 2, sinp); // use 90 degrees if out of range
		else
			yaw = (float) Math.asin(sinp);

		// pitch (z-axis rotation)
		double siny_cosp = 2 * (w * z + x * y);
		double cosy_cosp = 1 - 2 * (y * y + z * z);
		pitch = (float) MathHelper.atan2(siny_cosp, cosy_cosp);

		return new EulerAngle(pitch, yaw, roll);
	}
}

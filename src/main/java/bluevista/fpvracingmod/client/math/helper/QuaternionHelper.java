package bluevista.fpvracingmod.client.math.helper;

import bluevista.fpvracingmod.client.math.Matrix4f;
import bluevista.fpvracingmod.client.math.Vector3f;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Quaternion;
import org.lwjgl.BufferUtils;
import java.nio.FloatBuffer;

public class QuaternionHelper {

	private static FloatBuffer MATRIX_BUFFER = BufferUtils.createFloatBuffer(16);
//	GLX.make(MemoryUtil.memAllocFloat(16), (p_209238_0_) -> {
//		LWJGLMemoryUntracker.untrack(MemoryUtil.memAddress(p_209238_0_));
//	});

//	public static net.minecraft.util.math.Quaternion convertToMCQuat(Quaternion q) {
//		return new net.minecraft.util.math.Quaternion(
//				q.getB(),
//				q.getC(),
//				q.getD(),
//				q.getA()
//		);
//	}

//	public static void applyRotQuat(MatrixStack stack, Quaternion q) {
//		Matrix4f mat = new Matrix4f(q);
//		mat.write(MATRIX_BUFFER, true);
//		MATRIX_BUFFER.rewind();
//		stack.peek().getModel().multiply(convertToMCQuat(q));
//		GL11.glMultMatrixf(MATRIX_BUFFER);
//	}

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
	
	public static Vector3f rotationMatrixToVector(Matrix4f mat) {
		return new Vector3f(mat.get(0,2), mat.get(1,2), mat.get(2,2));
	}
	
    public static FloatBuffer toBuffer(Matrix4f mat) {
        FloatBuffer buffer = BufferUtils.createFloatBuffer(16);

        for(int i = 0; i < 4; i++)
        	for(int j = 0; j < 4; j++)
        		buffer.put(mat.get(i, j));

//        buffer.put(mat.m00);
//        buffer.put(mat.m01);
//        buffer.put(mat.m02);
//        buffer.put(mat.m03);
//        buffer.put(mat.m10);
//        buffer.put(mat.m11);
//        buffer.put(mat.m12);
//        buffer.put(mat.m13);
//        buffer.put(mat.m20);
//        buffer.put(mat.m21);
//        buffer.put(mat.m22);
//        buffer.put(mat.m23);
//        buffer.put(mat.m30);
//        buffer.put(mat.m31);
//        buffer.put(mat.m32);
//        buffer.put(mat.m33);

        buffer.flip();

        return buffer;
    }

	public static Matrix4f quatToMatrix(Quaternion q) {
		Matrix4f mat = new Matrix4f();

	    double sqw = q.getW() * q.getW();
	    double sqx = q.getX() * q.getX();
	    double sqy = q.getY() * q.getY();
	    double sqz = q.getZ() * q.getZ();

//	     invs (inverse square length) is only required if quaternion is not already normalised
	    double invs = 1 / (sqx + sqy + sqz + sqw);
	    mat.set(0, 0, (float) (( sqx - sqy - sqz + sqw)*invs)); // since sqw + sqx + sqy + sqz =1/invs*invs
	    mat.set(1, 1, (float) ((-sqx + sqy - sqz + sqw)*invs));
	    mat.set(2, 2, (float) ((-sqx - sqy + sqz + sqw)*invs));

	    double tmp1 = q.getX() * q.getY();
	    double tmp2 = q.getZ() * q.getW();
	    mat.set(1, 0, (float) (2.0 * (tmp1 + tmp2)*invs));
	    mat.set(0, 1, (float) (2.0 * (tmp1 - tmp2)*invs));

	    tmp1 = q.getX() * q.getZ();
	    tmp2 = q.getY() * q.getW();
	    mat.set(2, 0, (float) (2.0 * (tmp1 - tmp2)*invs));
	    mat.set(0, 2, (float) (2.0 * (tmp1 + tmp2)*invs));
	    tmp1 = q.getY() * q.getZ();
	    tmp2 = q.getX() * q.getW();
	    mat.set(2, 1, (float) (2.0 * (tmp1 + tmp2)*invs));
	    mat.set(1, 2, (float) (2.0 * (tmp1 - tmp2)*invs));

	    return mat;
	}
}

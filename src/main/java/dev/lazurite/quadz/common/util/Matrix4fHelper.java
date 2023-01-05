package dev.lazurite.quadz.common.util;

import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

// jank
public interface Matrix4fHelper {

    static Vector3f matrixToVector(Matrix4f mat) {
        return new Vector3f(mat.m02(), mat.m12(), mat.m22());
    }

    static void fromQuaternion(Matrix4f mat, Quaternionf q) {
        double tmp1;
        double tmp2;

        double sqw = q.w() * q.w();
        double sqx = q.x() * q.x();
        double sqy = q.y() * q.y();
        double sqz = q.z() * q.z();

        double invs = 1 / (sqx + sqy + sqz + sqw);
        mat.m22((float) ((-sqx - sqy + sqz + sqw)*invs));

        tmp1 = q.x() * q.z();
        tmp2 = q.y() * q.w();
        mat.m02((float) (2.0 * (tmp1 + tmp2)*invs));

        tmp1 = q.y() * q.z();
        tmp2 = q.x() * q.w();
        mat.m12((float) (2.0 * (tmp1 - tmp2)*invs));
    }

}

package dev.lazurite.fpvracing.util;

import net.minecraft.util.math.Matrix4f;
import net.minecraft.util.math.Quaternion;

import javax.vecmath.Vector3f;

public interface Matrix4fInject {
    Vector3f matrixToVector();
    void fromQuaternion(Quaternion q);

    @SuppressWarnings("ConstantConditions")
    static Matrix4fInject from(Matrix4f self) {
        return (Matrix4fInject) (Object) self;
    }
}

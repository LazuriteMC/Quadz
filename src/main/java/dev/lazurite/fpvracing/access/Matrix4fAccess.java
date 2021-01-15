package dev.lazurite.fpvracing.access;

import net.minecraft.util.math.Matrix4f;
import net.minecraft.util.math.Quaternion;
import physics.javax.vecmath.Vector3f;

public interface Matrix4fAccess {
    Vector3f matrixToVector();
    void fromQuaternion(Quaternion q);

    @SuppressWarnings("ConstantConditions")
    static Matrix4fAccess from(Matrix4f self) {
        return (Matrix4fAccess) (Object) self;
    }
}

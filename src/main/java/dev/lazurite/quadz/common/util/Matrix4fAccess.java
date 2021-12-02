package dev.lazurite.quadz.common.util;

import com.jme3.math.Vector3f;
import com.mojang.math.Matrix4f;
import com.mojang.math.Quaternion;

public interface Matrix4fAccess {
    Vector3f matrixToVector();
    void fromQuaternion(Quaternion q);

    @SuppressWarnings("ConstantConditions")
    static Matrix4fAccess from(Matrix4f self) {
        return (Matrix4fAccess) (Object) self;
    }
}

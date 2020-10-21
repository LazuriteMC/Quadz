package bluevista.fpvracing.util;

import net.minecraft.util.math.Matrix4f;
import net.minecraft.util.math.Quaternion;
import net.minecraft.util.math.Vec3d;

public interface Matrix4fInject {
    Vec3d matrixToVector();
    void fromQuaternion(Quaternion q);

    @SuppressWarnings("ConstantConditions")
    static Matrix4fInject from(Matrix4f self) {
        return (Matrix4fInject) (Object) self;
    }
}

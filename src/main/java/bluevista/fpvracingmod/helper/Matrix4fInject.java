package bluevista.fpvracingmod.helper;

import net.minecraft.util.math.Matrix4f;
import net.minecraft.util.math.Vec3d;

import javax.vecmath.Quat4f;

public interface Matrix4fInject {
    Vec3d matrixToVector();
    void fromQuaternion(Quat4f q);

    @SuppressWarnings("ConstantConditions")
    static Matrix4fInject from(Matrix4f self) {
        return (Matrix4fInject) (Object) self;
    }
}

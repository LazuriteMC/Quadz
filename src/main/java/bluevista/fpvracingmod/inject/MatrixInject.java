package bluevista.fpvracingmod.inject;

import net.minecraft.util.math.Matrix4f;
import net.minecraft.util.math.Quaternion;
import net.minecraft.util.math.Vec3d;

public interface MatrixInject {
    Vec3d matrixToVector();
    void fromQuaternion(Quaternion q);

    @SuppressWarnings("ConstantConditions")
    static MatrixInject from(Matrix4f self) {
        return (MatrixInject) (Object) self;
    }
}

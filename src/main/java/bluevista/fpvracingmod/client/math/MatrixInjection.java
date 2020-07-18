package bluevista.fpvracingmod.client.math;

import net.minecraft.util.math.Matrix4f;
import net.minecraft.util.math.Quaternion;
import net.minecraft.util.math.Vec3d;

public interface MatrixInjection {
    Vec3d matrixToVector();
    void fromQuaternion(Quaternion q);

    @SuppressWarnings("ConstantConditions")
    static MatrixInjection from(Matrix4f self) {
        return (MatrixInjection) (Object) self;
    }
}

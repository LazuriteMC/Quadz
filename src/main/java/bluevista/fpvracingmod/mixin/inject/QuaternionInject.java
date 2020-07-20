package bluevista.fpvracingmod.mixin.inject;

import net.minecraft.util.math.Quaternion;

public interface QuaternionInject {
    void rotateX(double degrees);
    void rotateY(double degrees);
    void rotateZ(double degrees);

    @SuppressWarnings("ConstantConditions")
    static QuaternionInject from(Quaternion self) {
        return (QuaternionInject) (Object) self;
    }
}

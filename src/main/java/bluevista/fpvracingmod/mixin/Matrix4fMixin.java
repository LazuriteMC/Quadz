package bluevista.fpvracingmod.mixin;

import bluevista.fpvracingmod.math.Matrix4fInject;
import net.minecraft.util.math.Matrix4f;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import javax.vecmath.Quat4f;

@Mixin(Matrix4f.class)
public class Matrix4fMixin implements Matrix4fInject {
    @Shadow float a02;
    @Shadow float a12;
    @Shadow float a22;

    public Vec3d matrixToVector() {
        return new Vec3d(a02, a12, a22);
    }

    public void fromQuaternion(Quat4f q) {
        double tmp1;
        double tmp2;

        double sqw = q.w*q.w;
        double sqx = q.x*q.x;
        double sqy = q.y*q.y;
        double sqz = q.z*q.z;

        double invs = 1 / (sqx + sqy + sqz + sqw);
        a22 = (float) ((-sqx - sqy + sqz + sqw)*invs);

        tmp1 = q.x*q.z;
        tmp2 = q.y*q.w;
        a02 = (float) (2.0 * (tmp1 + tmp2)*invs);

        tmp1 = q.y*q.z;
        tmp2 = q.x*q.w;
        a12 = (float) (2.0 * (tmp1 - tmp2)*invs);
    }
}

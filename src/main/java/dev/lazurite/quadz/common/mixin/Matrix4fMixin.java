package dev.lazurite.quadz.common.mixin;

import com.jme3.math.Vector3f;
import com.mojang.math.Matrix4f;
import com.mojang.math.Quaternion;
import dev.lazurite.quadz.common.util.Matrix4fAccess;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

@Mixin(Matrix4f.class)
public class Matrix4fMixin implements Matrix4fAccess {
    @Shadow protected float m02;
    @Shadow protected float m12;
    @Shadow protected float m22;

    @Unique
    public Vector3f matrixToVector() {
        return new Vector3f(m02, m12, m22);
    }

    @Unique
    public void fromQuaternion(Quaternion q) {
        double tmp1;
        double tmp2;

        double sqw = q.r() * q.r();
        double sqx = q.i() * q.i();
        double sqy = q.j() * q.j();
        double sqz = q.k() * q.k();

        double invs = 1 / (sqx + sqy + sqz + sqw);
        m22 = (float) ((-sqx - sqy + sqz + sqw)*invs);

        tmp1 = q.i() * q.k();
        tmp2 = q.j() * q.r();
        m02 = (float) (2.0 * (tmp1 + tmp2)*invs);

        tmp1 = q.j() * q.k();
        tmp2 = q.i() * q.r();
        m12 = (float) (2.0 * (tmp1 - tmp2)*invs);
    }
}

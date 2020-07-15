package bluevista.fpvracingmod.mixin;

import bluevista.fpvracingmod.client.math.MatrixInjection;
import net.minecraft.util.math.Matrix4f;
import net.minecraft.util.math.Quaternion;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(Matrix4f.class)
public class MatrixMixin implements MatrixInjection {
    @Shadow float a00;
    @Shadow float a01;
    @Shadow float a02;
    @Shadow float a10;
    @Shadow float a11;
    @Shadow float a12;
    @Shadow float a20;
    @Shadow float a21;
    @Shadow float a22;

    public Vec3d matrixToVector() {
        return new Vec3d(a02, a12, a22);
    }

    public void fromQuaternion(Quaternion q) {
        double sqw = q.getW()*q.getW();
        double sqx = q.getX()*q.getX();
        double sqy = q.getY()*q.getY();
        double sqz = q.getZ()*q.getZ();

        // invs (inverse square length) is only required if quaternion is not already normalised
        double invs = 1 / (sqx + sqy + sqz + sqw);
//        a00 = (float) (( sqx - sqy - sqz + sqw)*invs) ; // since sqw + sqx + sqy + sqz =1/invs*invs
//        a11 = (float) ((-sqx + sqy - sqz + sqw)*invs) ;
        a22 = (float) ((-sqx - sqy + sqz + sqw)*invs);

        double tmp1 = q.getX()*q.getY();
        double tmp2 = q.getZ()*q.getW();
//        a10 = (float) (2.0 * (tmp1 + tmp2)*invs) ;
//        a01 = (float) (2.0 * (tmp1 - tmp2)*invs) ;

        tmp1 = q.getX()*q.getZ();
        tmp2 = q.getY()*q.getW();
//        a20 = (float) (2.0 * (tmp1 - tmp2)*invs) ;
        a02 = (float) (2.0 * (tmp1 + tmp2)*invs);
        tmp1 = q.getY()*q.getZ();
        tmp2 = q.getX()*q.getW();
//        a21 = (float) (2.0 * (tmp1 + tmp2)*invs) ;
        a12 = (float) (2.0 * (tmp1 - tmp2)*invs);
    }
}

package bluevista.fpvracingmod.mixin;

import bluevista.fpvracingmod.client.math.MatrixInjection;
import net.minecraft.util.math.Matrix4f;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(Matrix4f.class)
public class MatrixMixin implements MatrixInjection {
    @Shadow float a20;
    @Shadow float a21;
    @Shadow float a22;

    public Vec3d matrixToVector() {
        return new Vec3d(a20, a22, a21);
    }
}

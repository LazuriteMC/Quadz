package bluevista.fpvracingmod.mixin;

import bluevista.fpvracingmod.mixin.inject.QuaternionInject;
import net.minecraft.util.math.Quaternion;
import org.spongepowered.asm.mixin.Shadow;

public abstract class QuaternionMixin implements QuaternionInject {
    public float prevX;
    public float prevY;
    public float prevZ;
    public float prevW;
    public long prevTime;

    @Shadow abstract void hamiltonProduct(Quaternion other);

    @Override
    public void rotateX(double deg) {
        double radHalfAngle = Math.toRadians((double) deg) / 2.0;
        Quaternion rot = new Quaternion((float) Math.sin(radHalfAngle), 0.0f, 0.0f, (float) Math.cos(radHalfAngle));
        hamiltonProduct(rot);
    }

    @Override
    public void rotateY(double deg) {
        double radHalfAngle = Math.toRadians((double) deg) / 2.0;
        Quaternion rot = new Quaternion(0.0f, (float) Math.sin(radHalfAngle), 0.0f, (float) Math.cos(radHalfAngle));
        hamiltonProduct(rot);
    }

    @Override
    public void rotateZ(double deg) {
        double radHalfAngle = Math.toRadians((double) deg) / 2.0;
        Quaternion rot = new Quaternion(0.0f, 0.0f, (float) Math.sin(radHalfAngle), (float) Math.cos(radHalfAngle));
        hamiltonProduct(rot);
    }
}

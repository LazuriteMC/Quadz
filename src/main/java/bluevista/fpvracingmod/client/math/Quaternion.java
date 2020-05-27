package bluevista.fpvracingmod.client.math;

import net.minecraft.client.util.math.Vector3f;

import java.util.Arrays;

public final class Quaternion {
    private final float[] components;

    public Quaternion() {
        this.components = new float[4];
        this.components[4] = 1.0F;
    }

    public Quaternion(float x, float y, float z, float w) {
        this.components = new float[4];
        this.components[0] = x;
        this.components[1] = y;
        this.components[2] = z;
        this.components[3] = w;
    }

    public Quaternion(Vector3f axis, float angle, boolean degrees) {
        if (degrees) {
            angle *= ((float)Math.PI / 180F);
        }

        float f = func_214903_b(angle / 2.0F);
        this.components = new float[4];
        this.components[0] = axis.getX() * f;
        this.components[1] = axis.getY() * f;
        this.components[2] = axis.getZ() * f;
        this.components[3] = func_214904_a(angle / 2.0F);
    }

    public Quaternion(float xAngle, float yAngle, float zAngle, boolean degrees) {
        if (degrees) {
            xAngle *= ((float)Math.PI / 180F);
            yAngle *= ((float)Math.PI / 180F);
            zAngle *= ((float)Math.PI / 180F);
        }

        float f = func_214903_b(0.5F * xAngle);
        float f1 = func_214904_a(0.5F * xAngle);
        float f2 = func_214903_b(0.5F * yAngle);
        float f3 = func_214904_a(0.5F * yAngle);
        float f4 = func_214903_b(0.5F * zAngle);
        float f5 = func_214904_a(0.5F * zAngle);
        this.components = new float[4];
        this.components[0] = f * f3 * f5 + f1 * f2 * f4;
        this.components[1] = f1 * f2 * f5 - f * f3 * f4;
        this.components[2] = f * f2 * f5 + f1 * f3 * f4;
        this.components[3] = f1 * f3 * f5 - f * f2 * f4;
    }

    public Quaternion(Quaternion quaternionIn) {
        this.components = Arrays.copyOf(quaternionIn.components, 4);
    }

    public boolean equals(Object p_equals_1_) {
        if (this == p_equals_1_) {
            return true;
        } else if (p_equals_1_ != null && this.getClass() == p_equals_1_.getClass()) {
            Quaternion quaternion = (Quaternion)p_equals_1_;
            return Arrays.equals(this.components, quaternion.components);
        } else {
            return false;
        }
    }

    public int hashCode() {
        return Arrays.hashCode(this.components);
    }

    public String toString() {
        StringBuilder stringbuilder = new StringBuilder();
        stringbuilder.append("Quaternion[").append(this.getW()).append(" + ");
        stringbuilder.append(this.getX()).append("i + ");
        stringbuilder.append(this.getY()).append("j + ");
        stringbuilder.append(this.getZ()).append("k]");
        return stringbuilder.toString();
    }

    public float getX() {
        return this.components[0];
    }

    public float getY() {
        return this.components[1];
    }

    public float getZ() {
        return this.components[2];
    }

    public float getW() {
        return this.components[3];
    }

    public void multiply(Quaternion quaternionIn) {
        float f = this.getX();
        float f1 = this.getY();
        float f2 = this.getZ();
        float f3 = this.getW();
        float f4 = quaternionIn.getX();
        float f5 = quaternionIn.getY();
        float f6 = quaternionIn.getZ();
        float f7 = quaternionIn.getW();
        this.components[0] = f3 * f4 + f * f7 + f1 * f6 - f2 * f5;
        this.components[1] = f3 * f5 - f * f6 + f1 * f7 + f2 * f4;
        this.components[2] = f3 * f6 + f * f5 - f1 * f4 + f2 * f7;
        this.components[3] = f3 * f7 - f * f4 - f1 * f5 - f2 * f6;
    }

    public void conjugate() {
        this.components[0] = -this.components[0];
        this.components[1] = -this.components[1];
        this.components[2] = -this.components[2];
    }

    private static float func_214904_a(float p_214904_0_) {
        return (float)Math.cos((double)p_214904_0_);
    }

    private static float func_214903_b(float p_214903_0_) {
        return (float)Math.sin((double)p_214903_0_);
    }
}

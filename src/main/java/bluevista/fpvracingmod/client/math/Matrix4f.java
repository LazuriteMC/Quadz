package bluevista.fpvracingmod.client.math;

import java.nio.FloatBuffer;
import java.util.Arrays;

public final class Matrix4f {
    private final float[] elements = new float[16];

    public Matrix4f() {
    }

    public Matrix4f(Quaternion quaternionIn) {
        this();
        float f = quaternionIn.getX();
        float f1 = quaternionIn.getY();
        float f2 = quaternionIn.getZ();
        float f3 = quaternionIn.getW();
        float f4 = 2.0F * f * f;
        float f5 = 2.0F * f1 * f1;
        float f6 = 2.0F * f2 * f2;
        this.elements[0] = 1.0F - f5 - f6;
        this.elements[5] = 1.0F - f6 - f4;
        this.elements[10] = 1.0F - f4 - f5;
        this.elements[15] = 1.0F;
        float f7 = f * f1;
        float f8 = f1 * f2;
        float f9 = f2 * f;
        float f10 = f * f3;
        float f11 = f1 * f3;
        float f12 = f2 * f3;
        this.elements[1] = 2.0F * (f7 + f12);
        this.elements[4] = 2.0F * (f7 - f12);
        this.elements[2] = 2.0F * (f9 - f11);
        this.elements[8] = 2.0F * (f9 + f11);
        this.elements[6] = 2.0F * (f8 + f10);
        this.elements[9] = 2.0F * (f8 - f10);
    }

    public boolean equals(Object p_equals_1_) {
        if (this == p_equals_1_) {
            return true;
        } else if (p_equals_1_ != null && this.getClass() == p_equals_1_.getClass()) {
            Matrix4f matrix4f = (Matrix4f)p_equals_1_;
            return Arrays.equals(this.elements, matrix4f.elements);
        } else {
            return false;
        }
    }

    public int hashCode() {
        return Arrays.hashCode(this.elements);
    }

    public void read(FloatBuffer floatBufferIn) {
        this.read(floatBufferIn, false);
    }

    public void read(FloatBuffer floatBufferIn, boolean transposeIn) {
        if (transposeIn) {
            for(int i = 0; i < 4; ++i) {
                for(int j = 0; j < 4; ++j) {
                    this.elements[i * 4 + j] = floatBufferIn.get(j * 4 + i);
                }
            }
        } else {
            floatBufferIn.get(this.elements);
        }

    }

    public String toString() {
        StringBuilder stringbuilder = new StringBuilder();
        stringbuilder.append("Matrix4f:\n");

        for(int i = 0; i < 4; ++i) {
            for(int j = 0; j < 4; ++j) {
                stringbuilder.append(this.elements[i + j * 4]);
                if (j != 3) {
                    stringbuilder.append(" ");
                }
            }

            stringbuilder.append("\n");
        }

        return stringbuilder.toString();
    }

    public void write(FloatBuffer floatBufferIn) {
        this.write(floatBufferIn, false);
    }

    public void write(FloatBuffer floatBufferIn, boolean transposeIn) {
        if (transposeIn) {
            for(int i = 0; i < 4; ++i) {
                for(int j = 0; j < 4; ++j) {
                    floatBufferIn.put(j * 4 + i, this.elements[i * 4 + j]);
                }
            }
        } else {
            floatBufferIn.put(this.elements);
        }

    }

    public float get(int col, int row) {
        return this.elements[col + 4 * row];
    }

    public void set(int col, int row, float val) {
        this.elements[col + 4 * row] = val;
    }

    public static Matrix4f perspective(double fov, float aspectRatio, float nearPlane, float farPlane) {
        float f = (float)(1.0D / Math.tan(fov * (double)((float)Math.PI / 180F) / 2.0D));
        Matrix4f matrix4f = new Matrix4f();
        matrix4f.set(0, 0, f / aspectRatio);
        matrix4f.set(1, 1, f);
        matrix4f.set(2, 2, (farPlane + nearPlane) / (nearPlane - farPlane));
        matrix4f.set(3, 2, -1.0F);
        matrix4f.set(2, 3, 2.0F * farPlane * nearPlane / (nearPlane - farPlane));
        return matrix4f;
    }

    public static Matrix4f orthographic(float width, float height, float nearPlane, float farPlane) {
        Matrix4f matrix4f = new Matrix4f();
        matrix4f.set(0, 0, 2.0F / width);
        matrix4f.set(1, 1, 2.0F / height);
        float f = farPlane - nearPlane;
        matrix4f.set(2, 2, -2.0F / f);
        matrix4f.set(3, 3, 1.0F);
        matrix4f.set(0, 3, -1.0F);
        matrix4f.set(1, 3, -1.0F);
        matrix4f.set(2, 3, -(farPlane + nearPlane) / f);
        return matrix4f;
    }
}


package bluevista.fpvracing.client.input;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.util.math.MathHelper;

public class AxisValues {
    public float currT;
    public float currX;
    public float currY;
    public float currZ;

    public AxisValues() {

    }

    public AxisValues(AxisValues axisValues) {
        this.set(axisValues.currT, axisValues.currX, axisValues.currY, axisValues.currZ);
    }

    public AxisValues(float currT, float currX, float currY, float currZ) {
        this.set(currT, currX, currY, currZ);
    }

    public AxisValues(AxisValues start, AxisValues end, float delta) {
        this.set(lerp(start, end, delta));
    }

    public void set(AxisValues axisValues) {
        this.set(axisValues.currT, axisValues.currX, axisValues.currY, axisValues.currZ);
    }

    public void set(float currT, float currX, float currY, float currZ) {
        this.currT = currT;
        this.currX = currX;
        this.currY = currY;
        this.currZ = currZ;
    }

    public String toString() {
        return "[CurrT: " + currT + " CurrX: " + currX + " CurrY: " + currY + " CurrZ: " + currZ + "]";
    }

    @Environment(EnvType.CLIENT)
    public static AxisValues lerp(AxisValues start, AxisValues end, float delta) {
        AxisValues av = new AxisValues();
        av.currT = MathHelper.lerp(start.currT, end.currT, delta);
        av.currX = MathHelper.lerp(start.currX, end.currX, delta);
        av.currY = MathHelper.lerp(start.currY, end.currY, delta);
        av.currZ = MathHelper.lerp(start.currZ, end.currZ, delta);
        return av;
    }
}

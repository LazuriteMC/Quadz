package bluevista.fpvracingmod.client.input;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.util.math.MathHelper;

public class AxisValues {
    public float currT;
    public float currX;
    public float currY;
    public float currZ;

    public float prevT;
    public float prevX;
    public float prevY;
    public float prevZ;

    public AxisValues() {

    }

    public AxisValues(float currT, float currX, float currY, float currZ) {
        this.set(currT, currX, currY, currZ);
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
    public void lerp(float delta) {
        currT = MathHelper.lerp(prevT, currT, delta);
        currX = MathHelper.lerp(prevX, currX, delta);
        currY = MathHelper.lerp(prevY, currY, delta);
        currZ = MathHelper.lerp(prevZ, currZ, delta);
    }
}

package bluevista.fpvracingmod.client.input;

import net.minecraft.network.PacketByteBuf;

public class AxisValues {
    public float currT;
    public float currX;
    public float currY;
    public float currZ;

    public AxisValues() {

    }

    public AxisValues(float currT, float currX, float currY, float currZ) {
        this();
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

    //TODO            |
    //TODO            |
    //TODO move these v

    public void serialize(PacketByteBuf buf) {
        buf.writeFloat(currT);
        buf.writeFloat(currX);
        buf.writeFloat(currY);
        buf.writeFloat(currZ);
    }

    public static AxisValues deserialize(PacketByteBuf buf) {
        AxisValues axisValues = new AxisValues();
        axisValues.set(
                buf.readFloat(),
                buf.readFloat(),
                buf.readFloat(),
                buf.readFloat());
        return axisValues;
    }
}

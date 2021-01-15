package dev.lazurite.fpvracing.client.input;

import net.minecraft.network.PacketByteBuf;

/**
 * This represents one "frame" of time when
 * the user's controller input is actively
 * being polled.
 * @see InputTick
 */
public class InputFrame {
    private float t;
    private float x;
    private float y;
    private float z;

    public InputFrame() {

    }

    public InputFrame(InputFrame frame) {
        set(frame.t, frame.x, frame.y, frame.z);
    }

    public InputFrame(float t, float x, float y, float z) {
        set(t, x, y, z);
    }

    public void set(InputFrame frame) {
        set(frame.t, frame.x, frame.y, frame.z);
    }

    public void set(float t, float x, float y, float z) {
        this.t = t;
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public void setT(float t) {
        this.t = t;
    }

    public void setX(float x) {
        this.x = x;
    }

    public void setY(float y) {
        this.y = y;
    }

    public void setZ(float z) {
        this.z = z;
    }

    public float getT() {
        return this.t;
    }

    public float getX() {
        return this.x;
    }

    public float getY() {
        return this.y;
    }

    public float getZ() {
        return this.z;
    }

    public void toBuffer(PacketByteBuf buf) {
        buf.writeFloat(t);
        buf.writeFloat(x);
        buf.writeFloat(y);
        buf.writeFloat(z);
    }

    public static InputFrame fromBuffer(PacketByteBuf buf) {
        return new InputFrame(buf.readFloat(), buf.readFloat(), buf.readFloat(), buf.readFloat());
    }

    @Override
    public String toString() {
        return "[t: " + t + " x: " + x + " y: " + y + " z: " + z + "]";
    }
}

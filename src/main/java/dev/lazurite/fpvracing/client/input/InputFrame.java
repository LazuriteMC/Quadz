package dev.lazurite.fpvracing.client.input;

import net.minecraft.nbt.CompoundTag;

/**
 * This represents one "frame" of time when
 * the user's controller input is actively
 * being polled.
 * @see InputTick
 */
public class InputFrame {
    private float throttle;
    private float pitch;
    private float yaw;
    private float roll;

    public InputFrame() {

    }

    public InputFrame(InputFrame frame) {
        set(frame.throttle, frame.pitch, frame.yaw, frame.roll);
    }

    public InputFrame(float throttle, float pitch, float yaw, float roll) {
        set(throttle, pitch, yaw, roll);
    }

    public void set(InputFrame frame) {
        set(frame.throttle, frame.pitch, frame.yaw, frame.roll);
    }

    public void set(float throttle, float pitch, float yaw, float roll) {
        this.throttle = throttle;
        this.pitch = pitch;
        this.yaw = yaw;
        this.roll = roll;
    }

    public void setThrottle(float throttle) {
        this.throttle = throttle;
    }

    public void setPitch(float pitch) {
        this.pitch = pitch;
    }

    public void setYaw(float yaw) {
        this.yaw = yaw;
    }

    public void setRoll(float roll) {
        this.roll = roll;
    }

    public float getThrottle() {
        return this.throttle;
    }

    public float getPitch() {
        return this.pitch;
    }

    public float getYaw() {
        return this.yaw;
    }

    public float getRoll() {
        return this.roll;
    }

    public void toTag(CompoundTag tag) {
        CompoundTag sub = new CompoundTag();
        sub.putFloat("t", throttle);
        sub.putFloat("x", pitch);
        sub.putFloat("y", yaw);
        sub.putFloat("z", roll);
        tag.put("frame", sub);
    }

    public static InputFrame fromTag(CompoundTag tag) {
        CompoundTag sub = tag.getCompound("frame");
        return new InputFrame(sub.getFloat("t"), sub.getFloat("x"), sub.getFloat("y"), sub.getFloat("z"));
    }

    @Override
    public String toString() {
        return "[throttle: " + throttle + " pitch: " + pitch + " yaw: " + yaw + " roll: " + roll + "]";
    }
}

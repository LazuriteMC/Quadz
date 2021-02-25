package dev.lazurite.fpvracing.client.input.frame;

import dev.lazurite.fpvracing.client.input.tick.InputTick;
import dev.lazurite.fpvracing.common.util.BetaflightHelper;
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

    private float rate;
    private float superRate;
    private float expo;

    public InputFrame() {
    }

    public InputFrame(InputFrame frame) {
        set(frame.throttle, frame.pitch, frame.yaw, frame.roll, frame.rate, frame.superRate, frame.expo);
    }

    public InputFrame(float throttle, float pitch, float yaw, float roll, float rate, float superRate, float expo) {
        set(throttle, pitch, yaw, roll, rate, superRate, expo);
    }

    public void set(InputFrame frame) {
        set(frame.throttle, frame.pitch, frame.yaw, frame.roll, frame.rate, frame.superRate, frame.expo);
    }

    public void set(float throttle, float pitch, float yaw, float roll, float rate, float superRate, float expo) {
        this.throttle = throttle;
        this.pitch = pitch;
        this.yaw = yaw;
        this.roll = roll;
        this.rate = rate;
        this.superRate = superRate;
        this.expo = expo;
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

    public void setRate(float rate) {
        this.rate = rate;
    }

    public void setSuperRate(float superRate) {
        this.superRate = superRate;
    }

    public void setExpo(float expo) {
        this.expo = expo;
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

    public float getRate() {
        return this.rate;
    }

    public float getSuperRate() {
        return this.superRate;
    }

    public float getExpo() {
        return this.expo;
    }

    public float calculatePitch(float delta) {
        return (float) BetaflightHelper.calculateRates(pitch, rate, expo, superRate, delta);
    }

    public float calculateYaw(float delta) {
        return (float) BetaflightHelper.calculateRates(yaw, rate, expo, superRate, delta);
    }

    public float calculateRoll(float delta) {
        return (float) BetaflightHelper.calculateRates(roll, rate, expo, superRate, delta);
    }

    public void toTag(CompoundTag tag) {
        CompoundTag sub = new CompoundTag();
        sub.putFloat("t", throttle);
        sub.putFloat("x", pitch);
        sub.putFloat("y", yaw);
        sub.putFloat("z", roll);
        sub.putFloat("rate", rate);
        sub.putFloat("super_rate", superRate);
        sub.putFloat("expo", expo);
        tag.put("frame", sub);
    }

    public static InputFrame fromTag(CompoundTag tag) {
        CompoundTag sub = tag.getCompound("frame");
        return new InputFrame(
                sub.getFloat("t"),
                sub.getFloat("x"),
                sub.getFloat("y"),
                sub.getFloat("z"),
                sub.getFloat("rate"),
                sub.getFloat("super_rate"),
                sub.getFloat("expo"));
    }

    @Override
    public String toString() {
        return "[throttle: " + throttle + " pitch: " + pitch + " yaw: " + yaw + " roll: " + roll + "]";
    }
}

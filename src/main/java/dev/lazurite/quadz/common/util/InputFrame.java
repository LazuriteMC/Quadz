package dev.lazurite.quadz.common.util;

import dev.lazurite.quadz.client.input.Mode;
import dev.lazurite.quadz.client.input.InputTick;

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

    private float maxAngle;
    private Mode mode;

    public InputFrame() {
    }

    public InputFrame(InputFrame frame) {
        set(frame.throttle, frame.pitch, frame.yaw, frame.roll, frame.rate, frame.superRate, frame.expo, frame.maxAngle, frame.mode);
    }

    public InputFrame(float throttle, float pitch, float yaw, float roll, float rate, float superRate, float expo, float maxAngle, Mode mode) {
        set(throttle, pitch, yaw, roll, rate, superRate, expo, maxAngle, mode);
    }

    public void set(InputFrame frame) {
        set(frame.throttle, frame.pitch, frame.yaw, frame.roll, frame.rate, frame.superRate, frame.expo, frame.maxAngle, frame.mode);
    }

    public void set(float throttle, float pitch, float yaw, float roll, float rate, float superRate, float expo, float maxAngle, Mode mode) {
        this.throttle = throttle;
        this.pitch = pitch;
        this.yaw = yaw;
        this.roll = roll;
        this.rate = rate;
        this.superRate = superRate;
        this.expo = expo;
        this.maxAngle = maxAngle;
        this.mode = mode;
    }

    public boolean isEmpty() {
        return this.throttle == 0 && this.pitch == 0 && this.yaw == 0 && this.roll == 0 &&
                this.rate == 0 && this.superRate == 0 && this.expo == 0 &&
                this.maxAngle == 0 && this.mode == null;
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

    public float getRate() {
        return this.rate;
    }

    public float getSuperRate() {
        return this.superRate;
    }

    public float getExpo() {
        return this.expo;
    }

    public float getMaxAngle() {
        return this.maxAngle;
    }

    public Mode getMode() {
        return this.mode;
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

    @Override
    public String toString() {
        return "[throttle: " + throttle + " pitch: " + pitch + " yaw: " + yaw + " roll: " + roll + "]";
    }
}

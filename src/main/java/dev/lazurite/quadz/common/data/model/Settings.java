package dev.lazurite.quadz.common.data.model;

import net.minecraft.network.PacketByteBuf;

public class Settings {
    private final String id;
    private final String name;
    private final String author;
    private final float width;
    private final float height;
    private final float mass;
    private final float dragCoefficient;
    private final float thrust;
    private final float thrustCurve;
    private final int cameraAngle;

    public Settings(String id, String name, String author, float width, float height, float mass, float dragCoefficient, float thrust, float thrustCurve, int cameraAngle) {
        this.id = id;
        this.name = name;
        this.author = author;
        this.width = width;
        this.height = height;
        this.mass = mass;
        this.dragCoefficient = dragCoefficient;
        this.thrust = thrust;
        this.thrustCurve = thrustCurve;
        this.cameraAngle = cameraAngle;
    }

    public PacketByteBuf serialize(PacketByteBuf buf) {
        buf.writeString(id);
        buf.writeString(name);
        buf.writeString(author);
        buf.writeFloat(width);
        buf.writeFloat(height);
        buf.writeFloat(mass);
        buf.writeFloat(dragCoefficient);
        buf.writeFloat(thrust);
        buf.writeFloat(thrustCurve);
        buf.writeInt(cameraAngle);
        return buf;
    }

    public static Settings deserialize(PacketByteBuf buf) {
        return new Settings(
                buf.readString(32767),
                buf.readString(32767),
                buf.readString(32767),
                buf.readFloat(),
                buf.readFloat(),
                buf.readFloat(),
                buf.readFloat(),
                buf.readFloat(),
                buf.readFloat(),
                buf.readInt());
    }

    public String getId() {
        return this.id;
    }

    public String getName() {
        return this.name;
    }

    public String getAuthor() {
        return author;
    }

    public float getWidth() {
        return width;
    }

    public float getHeight() {
        return height;
    }

    public float getMass() {
        return mass;
    }

    public float getDragCoefficient() {
        return dragCoefficient;
    }

    public float getThrust() {
        return thrust;
    }

    public float getThrustCurve() {
        return thrustCurve;
    }

    public int getCameraAngle() {
        return this.cameraAngle;
    }
}

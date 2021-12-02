package dev.lazurite.quadz.common.data.model;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.Mth;

import java.util.Arrays;

public class Template {
    private final Settings settings;
    private final JsonObject geo;
    private final JsonObject animation;
    private final byte[] texture;
    private final int originDistance;

    public Template(Settings settings, JsonObject geo, JsonObject animation, byte[] texture, int originDistance) throws RuntimeException {
        if (settings == null) {
            throw new RuntimeException("Missing or currupted settings file");
        } else if (geo == null) {
            throw new RuntimeException("Missing or corrupted geo model file in " + settings.getName());
        } else if (animation == null) {
            throw new RuntimeException("Missing or currupted animation file in " + settings.getName());
        } else if (texture == null) {
            throw new RuntimeException("Missing or currupted texture file in " + settings.getName());
        }

        this.settings = settings;
        this.geo = geo;
        this.animation = animation;
        this.texture = texture;
        this.originDistance = originDistance;
    }

    public FriendlyByteBuf serialize() {
        FriendlyByteBuf buf = PacketByteBufs.create();
        settings.serialize(buf);
        buf.writeUtf(geo.toString());
        buf.writeUtf(animation.toString());
        buf.writeByteArray(texture);
        buf.writeInt(originDistance + 1);
        return buf;
    }

    public static Template deserialize(FriendlyByteBuf buf) {
        return new Template(
            Settings.deserialize(buf),
            new JsonParser().parse(buf.readUtf(32767)).getAsJsonObject(),
            new JsonParser().parse(buf.readUtf(32767)).getAsJsonObject(),
            buf.readByteArray(),
            buf.readInt());
    }

    public String getId() {
        return this.settings.getId();
    }

    public Settings getSettings() {
        return this.settings;
    }

    public JsonObject getGeo() {
        return this.geo;
    }

    public JsonObject getAnimation() {
        return this.animation;
    }

    public byte[] getTexture() {
        return this.texture;
    }

    public int getOriginDistance() {
        return this.originDistance;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Template) {
            Template template = (Template) obj;
            return template.settings.equals(settings) &&
                    template.animation.equals(animation) &&
                    template.geo.equals(geo) &&
                    Arrays.equals(template.texture, texture);
        }

        return false;
    }

    public static class Settings {
        private final String id;
        private final String name;
        private final String author;
        private final float width;
        private final float height;
        private final float cameraX;
        private final float cameraY;
        private final float mass;
        private final float dragCoefficient;
        private final float thrust;
        private final float thrustCurve;
        private final int cameraAngle;

        public Settings(String id, String name, String author, float width, float height, float cameraX, float cameraY, float mass, float dragCoefficient, float thrust, float thrustCurve, int cameraAngle) {
            this.id = id.toLowerCase();
            this.name = name;
            this.author = author;
            this.width = Mth.clamp(width, 0.0f, 3.0f);
            this.height = Mth.clamp(height, 0.0f, 3.0f);
            this.cameraX = Mth.clamp(cameraX, -1.5f, 1.5f);
            this.cameraY = Mth.clamp(cameraY, -1.5f, 1.5f);
            this.mass = Mth.clamp(mass, 0.0f, 100);
            this.dragCoefficient = Mth.clamp(dragCoefficient, 0.0f, 1.0f);
            this.thrust = Mth.clamp(thrust, 0.0f, 200);
            this.thrustCurve = Mth.clamp(thrustCurve, 0.0f, 1.0f);
            this.cameraAngle = Mth.clamp(cameraAngle, -180, 180);
        }

        public void serialize(FriendlyByteBuf buf) {
            buf.writeUtf(id);
            buf.writeUtf(name);
            buf.writeUtf(author);
            buf.writeFloat(width);
            buf.writeFloat(height);
            buf.writeFloat(cameraX);
            buf.writeFloat(cameraY);
            buf.writeFloat(mass);
            buf.writeFloat(dragCoefficient);
            buf.writeFloat(thrust);
            buf.writeFloat(thrustCurve);
            buf.writeInt(cameraAngle);
        }

        public static Settings deserialize(FriendlyByteBuf buf) {
            return new Settings(
                    buf.readUtf(32767),
                    buf.readUtf(32767),
                    buf.readUtf(32767),
                    buf.readFloat(),
                    buf.readFloat(),
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

        public float getCameraX() {
            return cameraX;
        }

        public float getCameraY() {
            return cameraY;
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
            return cameraAngle;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof Settings) {
                Settings settings = (Settings) obj;
                return settings.id.equals(id) &&
                        settings.author.equals(author) &&
                        settings.name.equals(name) &&
                        settings.cameraAngle == cameraAngle &&
                        settings.cameraX == cameraX &&
                        settings.cameraY == cameraY &&
                        settings.mass == mass &&
                        settings.width == width &&
                        settings.height == height &&
                        settings.dragCoefficient == dragCoefficient &&
                        settings.thrust == thrust &&
                        settings.thrustCurve == thrustCurve;
            }

            return false;
        }
    }
}

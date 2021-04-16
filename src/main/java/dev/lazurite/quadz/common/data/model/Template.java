package dev.lazurite.quadz.common.data.model;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.network.PacketByteBuf;

import java.util.Arrays;

public class Template {
    private final Settings settings;
    private final JsonObject geo;
    private final JsonObject animation;
    private final byte[] texture;
    private final int originDistance;

    public Template(Settings settings, JsonObject geo, JsonObject animation, byte[] texture, int originDistance) throws RuntimeException {
        if (geo == null || animation == null || texture == null) {
            throw new RuntimeException("Quadcopter template is missing information.");
        }

        this.settings = settings;
        this.geo = geo;
        this.animation = animation;
        this.texture = texture;
        this.originDistance = originDistance;
    }

    public PacketByteBuf serialize() {
        PacketByteBuf buf = PacketByteBufs.create();
        settings.serialize(buf);
        buf.writeString(geo.toString());
        buf.writeString(animation.toString());
        buf.writeByteArray(texture);
        buf.writeInt(originDistance + 1);
        return buf;
    }

    public static Template deserialize(PacketByteBuf buf) {
        return new Template(
            Settings.deserialize(buf),
            new JsonParser().parse(buf.readString(32767)).getAsJsonObject(),
            new JsonParser().parse(buf.readString(32767)).getAsJsonObject(),
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
}

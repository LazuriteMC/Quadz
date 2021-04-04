package dev.lazurite.quadz.common.data.model;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.network.PacketByteBuf;

public class Template {
    private final Settings settings;
    private final JsonObject geo;
    private final JsonObject animation;
    private final byte[] texture;
    private final boolean original;

    public Template(Settings settings, JsonObject geo, JsonObject animation, byte[] texture, boolean original) throws RuntimeException {
        if (geo == null || animation == null || texture == null) {
            throw new RuntimeException("Quadcopter template is missing information.");
        }

        this.settings = settings;
        this.geo = geo;
        this.animation = animation;
        this.texture = texture;
        this.original = original;
    }

    public PacketByteBuf serialize() {
        PacketByteBuf buf = settings.serialize();
        buf.writeString(geo.toString());
        buf.writeString(animation.toString());
        buf.writeByteArray(texture);

        return buf;
    }

    public static Template deserialize(PacketByteBuf buf) {
        Settings settings = Settings.deserialize(buf);
        JsonObject geo = new JsonParser().parse(buf.readString(32767)).getAsJsonObject();
        JsonObject animation = new JsonParser().parse(buf.readString(32767)).getAsJsonObject();
        byte[] texture = buf.readByteArray();

        return new Template(settings, geo, animation, texture, false);
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

    public boolean isOriginal() {
        return this.original;
    }
}

package dev.lazurite.fpvracing.network.tracker.generic.types;

import dev.lazurite.fpvracing.server.entity.FlyableEntity;
import dev.lazurite.fpvracing.util.Frequency;
import dev.lazurite.fpvracing.util.PacketHelper;
import dev.lazurite.fpvracing.network.tracker.Config;
import dev.lazurite.fpvracing.network.tracker.generic.GenericType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.PacketByteBuf;

public class FrequencyType implements GenericType<Frequency> {
    public void write(PacketByteBuf buf, Frequency value) {
        PacketHelper.serializeFrequency(buf, value);
    }

    public Frequency read(PacketByteBuf buf) {
        return PacketHelper.deserializeFrequency(buf);
    }

    public Frequency copy(Frequency frequency) {
        return frequency;
    }

    public void toTag(CompoundTag tag, String key, Frequency value) {
        value.toTag(tag);
    }

    public Frequency fromTag(CompoundTag tag, String key) {
        return Frequency.fromTag(tag);
    }

    public void toConfig(Config config, String key, Frequency value) {
        config.setProperty("band", String.valueOf(value.getBand()));
        config.setProperty("channel", String.valueOf(value.getChannel()));
    }

    public Frequency fromConfig(Config config, String key) {
        Frequency fallback = FlyableEntity.FREQUENCY.getFallback();

        return new Frequency(
                config.getProperty("band", String.valueOf(fallback.getBand())).charAt(0),
                Integer.parseInt(config.getProperty("channel", String.valueOf(fallback.getChannel())))
        );
    }

    public Class<Frequency> getClassType() {
        return Frequency.class;
    }
}

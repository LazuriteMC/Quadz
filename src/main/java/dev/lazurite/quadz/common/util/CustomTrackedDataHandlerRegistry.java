package dev.lazurite.quadz.common.util;

import net.minecraft.entity.data.TrackedDataHandler;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.network.PacketByteBuf;

public class CustomTrackedDataHandlerRegistry {
    public static final TrackedDataHandler<Frequency> FREQUENCY = new TrackedDataHandler<Frequency>() {
        public void write(PacketByteBuf buf, Frequency frequency) {
            buf.writeInt(frequency.getBand());
            buf.writeInt(frequency.getChannel());
        }

        public Frequency read(PacketByteBuf buf) {
            int band = buf.readInt();
            int channel = buf.readInt();
            return new Frequency((char) band, channel);
        }

        public Frequency copy(Frequency frequency) {
            return new Frequency(frequency);
        }
    };

    static {
        TrackedDataHandlerRegistry.register(FREQUENCY);
    }
}

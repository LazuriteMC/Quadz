package dev.lazurite.fpvracing.common.util;

import dev.lazurite.fpvracing.client.input.InputFrame;
import net.minecraft.entity.data.TrackedDataHandler;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.network.PacketByteBuf;

public class CustomTrackedDataHandlerRegistry {
    public static final TrackedDataHandler<Frequency> FREQUENCY = new TrackedDataHandler<Frequency>() {
        public void write(PacketByteBuf buf, Frequency frequency) {
            buf.writeChar(frequency.getBand());
            buf.writeInt(frequency.getChannel());
        }

        public Frequency read(PacketByteBuf buf) {
            char band = buf.readChar();
            int channel = buf.readInt();
            return new Frequency(band, channel);
        }

        public Frequency copy(Frequency frequency) {
            return new Frequency(frequency);
        }
    };

    public static final TrackedDataHandler<InputFrame> INPUT_FRAME = new TrackedDataHandler<InputFrame>() {
        public void write(PacketByteBuf buf, InputFrame frame) {
            buf.writeFloat(frame.getT());
            buf.writeFloat(frame.getX());
            buf.writeFloat(frame.getY());
            buf.writeFloat(frame.getZ());
        }

        public InputFrame read(PacketByteBuf buf) {
            return new InputFrame(buf.readFloat(), buf.readFloat(), buf.readFloat(), buf.readFloat());
        }

        public InputFrame copy(InputFrame frame) {
            return new InputFrame(frame);
        }
    };

    static {
        TrackedDataHandlerRegistry.register(FREQUENCY);
        TrackedDataHandlerRegistry.register(INPUT_FRAME);
    }
}

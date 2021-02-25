package dev.lazurite.fpvracing.common.util;

import dev.lazurite.fpvracing.common.entity.QuadcopterEntity;
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

    public static final TrackedDataHandler<QuadcopterEntity.State> STATE = new TrackedDataHandler<QuadcopterEntity.State>() {
        public void write(PacketByteBuf buf, QuadcopterEntity.State state) {
            buf.writeEnumConstant(state);
        }

        public QuadcopterEntity.State read(PacketByteBuf buf) {
            return buf.readEnumConstant(QuadcopterEntity.State.class);
        }

        public QuadcopterEntity.State copy(QuadcopterEntity.State state) {
            return state;
        }
    };

    static {
        TrackedDataHandlerRegistry.register(FREQUENCY);
        TrackedDataHandlerRegistry.register(STATE);
    }
}

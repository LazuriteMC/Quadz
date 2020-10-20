package bluevista.fpvracing.network;

import bluevista.fpvracing.util.Frequency;
import net.minecraft.entity.data.TrackedDataHandler;
import net.minecraft.network.PacketByteBuf;

public class FlyableDataHandlerRegistry {
    public static final TrackedDataHandler<Frequency> FREQUENCY = new TrackedDataHandler<Frequency>() {
        public void write(PacketByteBuf packetByteBuf, Frequency frequency) {
            packetByteBuf.writeChar(frequency.getBand());
            packetByteBuf.writeInt(frequency.getChannel());
        }

        public Frequency read(PacketByteBuf packetByteBuf) {
            return new Frequency(
                    packetByteBuf.readChar(),
                    packetByteBuf.readInt()
            );
        }

        public Frequency copy(Frequency frequency) {
            return frequency;
        }
    };
}

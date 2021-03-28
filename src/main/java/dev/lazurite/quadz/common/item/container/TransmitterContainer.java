package dev.lazurite.quadz.common.item.container;

import dev.lazurite.quadz.common.util.type.Bindable;
import dev.onyxstudios.cca.api.v3.item.ItemComponent;
import net.fabricmc.fabric.api.util.NbtType;
import net.minecraft.item.ItemStack;

/**
 * Stores the bind id in the transmitter through the {@link Bindable} interface.
 * @see Bindable
 */
public class TransmitterContainer extends ItemComponent implements Bindable {
    public TransmitterContainer(ItemStack stack) {
        super(stack);
    }

    @Override
    public void setBindId(int bindId) {
        this.putInt("bind_id", bindId);
    }

    @Override
    public int getBindId() {
        if (!this.hasTag("bind_id", NbtType.INT)) {
            this.setBindId(-1);
        }

        return this.getInt("bind_id");
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof TransmitterContainer) {
            return getBindId() == ((TransmitterContainer) obj).getBindId();
        }

        return false;
    }
}

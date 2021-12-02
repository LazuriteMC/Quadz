package dev.lazurite.quadz.common.state;

import dev.lazurite.quadz.Quadz;
import dev.lazurite.quadz.common.state.item.StackBindableState;
import net.minecraft.world.item.ItemStack;

import java.util.Optional;
import java.util.Random;

public interface Bindable {
    static void bind(Bindable b1, Bindable b2) {
        Random random = new Random();
        int bindId = random.nextInt(10000);
        b1.setBindId(bindId);
        b2.setBindId(bindId);
    }

    static Optional<Bindable> get(ItemStack stack) {
        Bindable state = null;

        if (stack.getItem().equals(Quadz.TRANSMITTER_ITEM)) {
            state = new StackBindableState(stack);
        }

        return Optional.ofNullable(state);
    }

    default boolean isBoundTo(Bindable bindable) {
        return isBoundTo(bindable.getBindId());
    }

    default boolean isBoundTo(int bindId) {
        return getBindId() != -1 && getBindId() == bindId;
    }

    void setBindId(int bindId);
    int getBindId();
}

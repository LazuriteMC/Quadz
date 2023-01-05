package dev.lazurite.quadz.common.util;

import dev.lazurite.quadz.common.item.QuadcopterItem;
import dev.lazurite.quadz.common.item.RemoteItem;
import net.minecraft.world.item.ItemStack;

import java.util.Optional;
import java.util.Random;

/**
 * @since 2.0.0
 */
public interface Bindable {

    static void bind(Bindable b1, Bindable b2) {
        final var random = new Random();
        final var bindId = random.nextInt(10000);
        b1.setBindId(bindId);
        b2.setBindId(bindId);
    }

    static Optional<Bindable> get(ItemStack stack) {
        return Optional.ofNullable(stack.getItem() instanceof RemoteItem || stack.getItem() instanceof QuadcopterItem ? new BindableItemStack(stack) : null);
    }

    default boolean isBoundTo(Bindable bindable) {
        return isBoundTo(bindable.getBindId());
    }

    default boolean isBoundTo(int bindId) {
        return getBindId() != -1 && getBindId() == bindId;
    }

    default void copyFrom(Bindable bindable) {
        this.setBindId(bindable.getBindId());
    }

    void setBindId(int bindId);
    int getBindId();

}

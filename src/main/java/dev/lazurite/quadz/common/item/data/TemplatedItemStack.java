package dev.lazurite.quadz.common.item.data;

import dev.lazurite.quadz.common.data.model.Templated;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;

/**
 * Stores all relevant template information available via {@link Templated}
 * @see Templated
 */
public class TemplatedItemStack implements Templated {
    private final ItemStack stack;
    private final CompoundTag tag;

    public TemplatedItemStack(ItemStack stack) {
        this.stack = stack;
        this.tag = this.stack.getOrCreateTagElement("templated");
    }

    @Override
    public void setTemplate(String template) {
        tag.putString("template", template);
    }

    @Override
    public String getTemplate() {
        return tag.getString("template");
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof final TemplatedItemStack container) {
            return getTemplate() == container.getTemplate();
        }

        return false;
    }
}

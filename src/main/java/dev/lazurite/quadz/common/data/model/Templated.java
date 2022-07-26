package dev.lazurite.quadz.common.data.model;

import dev.lazurite.quadz.Quadz;
import dev.lazurite.quadz.common.data.template.TemplateLoader;
import dev.lazurite.quadz.common.data.template.model.Template;
import dev.lazurite.quadz.common.item.data.TemplatedItemStack;
import net.minecraft.world.item.ItemStack;

import java.util.Optional;

public interface Templated {
    static Optional<Template> find(String template) {
        return Optional.ofNullable(TemplateLoader.getTemplate(template));
    }

    static Optional<Templated> get(ItemStack stack) {
        return Optional.ofNullable(
                stack.getItem().equals(Quadz.QUADCOPTER_ITEM) ?
                        new TemplatedItemStack(stack) : null);
    }

    default void copyFrom(Templated templated) {
        this.setTemplate(templated.getTemplate());
    }

    String getTemplate();
    void setTemplate(String template);
}

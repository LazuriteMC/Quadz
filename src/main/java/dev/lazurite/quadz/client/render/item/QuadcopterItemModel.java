package dev.lazurite.quadz.client.render.item;

import dev.lazurite.form.api.Templated;
import dev.lazurite.form.impl.common.Form;
import dev.lazurite.quadz.common.item.QuadcopterItem;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import software.bernie.geckolib.model.GeoModel;

public class QuadcopterItemModel extends GeoModel<QuadcopterItem> {

    private ItemStack stack;

    public void using(ItemStack stack) {
        this.stack = stack;
    }

    @Override
    public ResourceLocation getModelResource(QuadcopterItem item) {
        return new ResourceLocation(Form.MODID, Templated.get(stack).getTemplate());
    }

    @Override
    public ResourceLocation getTextureResource(QuadcopterItem item) {
        return new ResourceLocation(Form.MODID, Templated.get(stack).getTemplate());
    }

    @Override
    public ResourceLocation getAnimationResource(QuadcopterItem item) {
        return new ResourceLocation(Form.MODID, Templated.get(stack).getTemplate());
    }

}

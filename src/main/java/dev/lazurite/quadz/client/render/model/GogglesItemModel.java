package dev.lazurite.quadz.client.render.model;

import dev.lazurite.quadz.Quadz;
import dev.lazurite.quadz.common.item.GogglesItem;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib3.model.AnimatedGeoModel;

public class GogglesItemModel extends AnimatedGeoModel<GogglesItem> {
    @Override
    public ResourceLocation getModelResource(GogglesItem object) {
        return new ResourceLocation(Quadz.MODID, "geo/goggles_item.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(GogglesItem object) {
        return new ResourceLocation(Quadz.MODID, "textures/armor/goggles_item.png");
    }

    @Override
    public ResourceLocation getAnimationResource(GogglesItem animatable) {
        return new ResourceLocation(Quadz.MODID, "animations/goggles_item.animation.json");
    }
}

package dev.lazurite.quadz.client.render.model;

import dev.lazurite.quadz.Quadz;
import dev.lazurite.quadz.common.item.GogglesItem;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib3.model.AnimatedGeoModel;

public class GogglesItemModel extends AnimatedGeoModel<GogglesItem> {
    @Override
    public ResourceLocation getModelLocation(GogglesItem object) {
        return new ResourceLocation(Quadz.MODID, "geo/goggles_item.geo.json");
    }

    @Override
    public ResourceLocation getAnimationFileLocation(GogglesItem animatable) {
        return new ResourceLocation(Quadz.MODID, "animations/goggles_item.animation.json");
    }

    @Override
    public ResourceLocation getTextureLocation(GogglesItem object) {
        return new ResourceLocation(Quadz.MODID, "textures/armor/goggles_item.png");
    }
}

package dev.lazurite.quadz.client.render.model;

import dev.lazurite.quadz.Quadz;
import dev.lazurite.quadz.common.item.GogglesItem;
import net.minecraft.util.Identifier;
import software.bernie.geckolib3.model.AnimatedGeoModel;

public class GogglesItemModel extends AnimatedGeoModel<GogglesItem> {
    @Override
    public Identifier getModelLocation(GogglesItem object) {
        return new Identifier(Quadz.MODID, "geo/goggles_item.geo.json");
    }

    @Override
    public Identifier getAnimationFileLocation(GogglesItem animatable) {
        return new Identifier(Quadz.MODID, "animations/goggles_item.animation.json");
    }

    @Override
    public Identifier getTextureLocation(GogglesItem object) {
        return new Identifier(Quadz.MODID, "textures/armor/goggles_item.png");
    }
}

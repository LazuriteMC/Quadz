package dev.lazurite.quadz.client.render.model;

import dev.lazurite.quadz.Quadz;
import dev.lazurite.quadz.common.item.QuadcopterItem;
import net.minecraft.util.Identifier;
import software.bernie.geckolib3.model.AnimatedGeoModel;

public class QuadcopterItemModel extends AnimatedGeoModel<QuadcopterItem> {
    @Override
    public Identifier getModelLocation(QuadcopterItem object) {
        return new Identifier(Quadz.MODID, "geo/quadcopter_item.geo.json");
    }

    @Override
    public Identifier getTextureLocation(QuadcopterItem object) {
        return new Identifier(Quadz.MODID, "textures/item/quadcopter_item.png");
    }

    @Override
    public Identifier getAnimationFileLocation(QuadcopterItem animatable) {
        return new Identifier(Quadz.MODID, "animations/quadcopter_item.animation.json");
    }
}

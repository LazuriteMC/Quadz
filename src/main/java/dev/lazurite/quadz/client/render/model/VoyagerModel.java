package dev.lazurite.quadz.client.render.model;

import dev.lazurite.quadz.Quadz;
import dev.lazurite.quadz.client.render.entity.VoyagerEntityRenderer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.util.Identifier;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.model.AnimatedGeoModel;

/**
 * The model for the voyager long range drone.
 * @see VoyagerEntityRenderer
 */
@Environment(EnvType.CLIENT)
public class VoyagerModel<T extends IAnimatable> extends AnimatedGeoModel<T> {
    @Override
    public Identifier getModelLocation(T voyager) {
        return new Identifier(Quadz.MODID, "geo/voyager.geo.json");
    }

    @Override
    public Identifier getTextureLocation(T voyager) {
        return new Identifier(Quadz.MODID, "textures/voyager.png");
    }

    @Override
    public Identifier getAnimationFileLocation(T voyager) {
        return new Identifier(Quadz.MODID, "animations/voyager.animation.json");
    }
}

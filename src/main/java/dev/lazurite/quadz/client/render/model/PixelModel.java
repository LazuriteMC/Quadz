package dev.lazurite.quadz.client.render.model;

import dev.lazurite.quadz.Quadz;
import dev.lazurite.quadz.client.render.entity.PixelEntityRenderer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.util.Identifier;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.model.AnimatedGeoModel;

/**
 * The model for the pixel micro drone.
 * @see PixelEntityRenderer
 */
@Environment(EnvType.CLIENT)
public class PixelModel<T extends IAnimatable> extends AnimatedGeoModel<T> {
    @Override
    public Identifier getModelLocation(T voyager) {
        return new Identifier(Quadz.MODID, "geo/pixel.geo.json");
    }

    @Override
    public Identifier getTextureLocation(T voyager) {
        return new Identifier(Quadz.MODID, "textures/pixel.png");
    }

    @Override
    public Identifier getAnimationFileLocation(T voyager) {
        return new Identifier(Quadz.MODID, "animations/pixel.animation.json");
    }
}

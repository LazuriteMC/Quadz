package dev.lazurite.quadz.client.render.model;

import dev.lazurite.quadz.Quadz;
import dev.lazurite.quadz.common.state.entity.QuadcopterEntity;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib3.geo.render.built.GeoModel;
import software.bernie.geckolib3.model.AnimatedGeoModel;

/**
 * This class dynamically provides a quadcopter model based on which
 * template it is assigned. It also caches the {@link GeoModel} object
 * since during a reload, it is possible for it to return null.
 */
@Environment(EnvType.CLIENT)
public class QuadcopterModel extends AnimatedGeoModel<QuadcopterEntity> {
    private GeoModel cachedModel;

    @Override
    public ResourceLocation getModelLocation(QuadcopterEntity quadcopter) {
        return new ResourceLocation(Quadz.MODID, quadcopter.getTemplate());
    }

    @Override
    public ResourceLocation getTextureLocation(QuadcopterEntity quadcopter) {
        return new ResourceLocation(Quadz.MODID, quadcopter.getTemplate());
    }

    @Override
    public ResourceLocation getAnimationFileLocation(QuadcopterEntity quadcopter) {
        return new ResourceLocation(Quadz.MODID, quadcopter.getTemplate());
    }

    @Override
    public GeoModel getModel(ResourceLocation resourceLocation) {
        GeoModel model = super.getModel(resourceLocation);

        if (model != null) {
            cachedModel = model;
        }

        return cachedModel;
    }
}

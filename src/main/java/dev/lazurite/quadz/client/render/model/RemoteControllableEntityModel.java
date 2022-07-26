package dev.lazurite.quadz.client.render.model;

import dev.lazurite.quadz.Quadz;
import dev.lazurite.quadz.common.entity.RemoteControllableEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib3.geo.render.built.GeoModel;
import software.bernie.geckolib3.model.AnimatedGeoModel;

/**
 * This class dynamically provides a {@link RemoteControllableEntity} model
 * based on which template it is assigned. It also caches the {@link GeoModel} object.
 */
public class RemoteControllableEntityModel<T extends RemoteControllableEntity> extends AnimatedGeoModel<T> {
    private GeoModel cachedModel;

    @Override
    public ResourceLocation getModelResource(T remoteControllableEntity) {
        return new ResourceLocation(Quadz.MODID, remoteControllableEntity.getTemplate());
    }

    @Override
    public ResourceLocation getTextureResource(T remoteControllableEntity) {
        return new ResourceLocation(Quadz.MODID, remoteControllableEntity.getTemplate());
    }

    @Override
    public ResourceLocation getAnimationResource(T remoteControllableEntity) {
        return new ResourceLocation(Quadz.MODID, remoteControllableEntity.getTemplate());
    }

    @Override
    public GeoModel getModel(ResourceLocation resourceLocation) {
        final var model = super.getModel(resourceLocation);

        if (model != null) {
            cachedModel = model;
        }

        return cachedModel;
    }
}

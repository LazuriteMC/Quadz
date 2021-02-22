package dev.lazurite.fpvracing.client.render.model;

import dev.lazurite.fpvracing.FPVRacing;
import dev.lazurite.fpvracing.client.render.entity.VoxelRacerOneEntityRenderer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.util.Identifier;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.model.AnimatedGeoModel;

/**
 * The model for the voxel racer 1 drone.
 * @see VoxelRacerOneEntityRenderer
 */
@Environment(EnvType.CLIENT)
public class VoxelRacerOneModel<T extends IAnimatable> extends AnimatedGeoModel<T> {
    @Override
    public Identifier getModelLocation(T voxelRacer) {
        return new Identifier(FPVRacing.MODID, "geo/voyager.geo.json");
    }

    @Override
    public Identifier getTextureLocation(T voxelRacer) {
        return new Identifier(FPVRacing.MODID, "textures/voyager.png");
    }

    @Override
    public Identifier getAnimationFileLocation(T voxelRacer) {
        return new Identifier(FPVRacing.MODID, "animations/voyager.animation.json");
    }
}

package dev.lazurite.quadz.client.render.model;

import dev.lazurite.quadz.Quadz;
import dev.lazurite.quadz.client.render.entity.VoxelRacerOneEntityRenderer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.util.Identifier;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.model.AnimatedGeoModel;

/**
 * The model for the voxel racer 1 racing drone.
 * @see VoxelRacerOneEntityRenderer
 */
@Environment(EnvType.CLIENT)
public class VoxelRacerOneModel<T extends IAnimatable> extends AnimatedGeoModel<T> {
    @Override
    public Identifier getModelLocation(T voxelRacer) {
        return new Identifier(Quadz.MODID, "geo/voxel_racer_one.geo.json");
    }

    @Override
    public Identifier getTextureLocation(T voxelRacer) {
        return new Identifier(Quadz.MODID, "textures/voxel_racer_one.png");
    }

    @Override
    public Identifier getAnimationFileLocation(T voxelRacer) {
        return new Identifier(Quadz.MODID, "animations/voxel_racer_one.animation.json");
    }
}

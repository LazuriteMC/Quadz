package dev.lazurite.fpvracing.client.render.entity.model;

import dev.lazurite.fpvracing.FPVRacing;
import dev.lazurite.fpvracing.common.entity.quads.VoyagerEntity;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.util.Identifier;
import software.bernie.geckolib3.model.AnimatedGeoModel;

@Environment(EnvType.CLIENT)
public class VoyagerModel extends AnimatedGeoModel<VoyagerEntity> {
    @Override
    public Identifier getModelLocation(VoyagerEntity voyager) {
        return new Identifier(FPVRacing.MODID, "geo/voyager.geo.json");
    }

    @Override
    public Identifier getTextureLocation(VoyagerEntity voyager) {
        return new Identifier(FPVRacing.MODID, "textures/voyager.png");
    }

    @Override
    public Identifier getAnimationFileLocation(VoyagerEntity voyager) {
        return new Identifier(FPVRacing.MODID, "animations/voyager.animation.json");
    }
}

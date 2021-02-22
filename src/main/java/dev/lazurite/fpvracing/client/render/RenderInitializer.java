package dev.lazurite.fpvracing.client.render;

import dev.lazurite.fpvracing.FPVRacing;
import dev.lazurite.fpvracing.client.render.entity.VoxelRacerOneEntityRenderer;
import dev.lazurite.fpvracing.client.render.entity.VoyagerEntityRenderer;
import dev.lazurite.fpvracing.client.render.model.VoxelRacerOneModel;
import dev.lazurite.fpvracing.client.render.model.VoyagerModel;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.rendereregistry.v1.EntityRendererRegistry;
import software.bernie.geckolib3.GeckoLib;
import software.bernie.geckolib3.renderer.geo.GeoItemRenderer;

@Environment(EnvType.CLIENT)
public class RenderInitializer implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        /* Initialize GeckoLib */
        GeckoLib.initialize();

        /* Entities */
        EntityRendererRegistry.INSTANCE.register(FPVRacing.VOXEL_RACER_ONE, (entityRenderDispatcher, context) -> new VoxelRacerOneEntityRenderer(entityRenderDispatcher));
        EntityRendererRegistry.INSTANCE.register(FPVRacing.VOYAGER, (entityRenderDispatcher, context) -> new VoyagerEntityRenderer(entityRenderDispatcher));

        /* Items */
        GeoItemRenderer.registerItemRenderer(FPVRacing.VOXEL_RACER_ONE_ITEM, new GeoItemRenderer<>(new VoxelRacerOneModel<>()));
        GeoItemRenderer.registerItemRenderer(FPVRacing.VOYAGER_ITEM, new GeoItemRenderer<>(new VoyagerModel<>()));
    }
}

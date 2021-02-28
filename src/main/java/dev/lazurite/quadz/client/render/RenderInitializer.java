package dev.lazurite.quadz.client.render;

import dev.lazurite.quadz.Quadz;
import dev.lazurite.quadz.client.render.entity.PixelEntityRenderer;
import dev.lazurite.quadz.client.render.entity.VoxelRacerOneEntityRenderer;
import dev.lazurite.quadz.client.render.entity.VoyagerEntityRenderer;
import dev.lazurite.quadz.client.render.model.PixelModel;
import dev.lazurite.quadz.client.render.model.VoxelRacerOneModel;
import dev.lazurite.quadz.client.render.model.VoyagerModel;
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
        EntityRendererRegistry.INSTANCE.register(Quadz.VOXEL_RACER_ONE, (entityRenderDispatcher, context) -> new VoxelRacerOneEntityRenderer(entityRenderDispatcher));
        EntityRendererRegistry.INSTANCE.register(Quadz.VOYAGER, (entityRenderDispatcher, context) -> new VoyagerEntityRenderer(entityRenderDispatcher));
        EntityRendererRegistry.INSTANCE.register(Quadz.PIXEL, (entityRenderDispatcher, context) -> new PixelEntityRenderer(entityRenderDispatcher));

        /* Items */
        GeoItemRenderer.registerItemRenderer(Quadz.VOXEL_RACER_ONE_ITEM, new GeoItemRenderer<>(new VoxelRacerOneModel<>()));
        GeoItemRenderer.registerItemRenderer(Quadz.VOYAGER_ITEM, new GeoItemRenderer<>(new VoyagerModel<>()));
        GeoItemRenderer.registerItemRenderer(Quadz.PIXEL_ITEM, new GeoItemRenderer<>(new PixelModel<>()));
    }
}

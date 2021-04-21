package dev.lazurite.quadz.client.render;

import dev.lazurite.quadz.Quadz;
import dev.lazurite.quadz.client.render.model.QuadcopterItemModel;
import dev.lazurite.quadz.client.render.renderer.QuadcopterEntityRenderer;
import dev.lazurite.quadz.client.render.renderer.GogglesItemRenderer;
import dev.lazurite.quadz.client.resource.SplashResourceLoader;
import dev.lazurite.quadz.common.data.util.TemplateResourceLoader;
import dev.lazurite.quadz.common.item.GogglesItem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.rendereregistry.v1.EntityRendererRegistry;
import net.minecraft.client.MinecraftClient;
import software.bernie.geckolib3.GeckoLib;
import software.bernie.geckolib3.renderer.geo.GeoArmorRenderer;
import software.bernie.geckolib3.renderer.geo.GeoItemRenderer;

@Environment(EnvType.CLIENT)
public class QuadzRendering {
    public static final TemplateResourceLoader templateLoader = new TemplateResourceLoader();
    public static final SplashResourceLoader splashLoader = new SplashResourceLoader();

    public static void initialize() {
        GeckoLib.initialize();
        GeoArmorRenderer.registerArmorRenderer(GogglesItem.class, new GogglesItemRenderer());
        GeoItemRenderer.registerItemRenderer(Quadz.QUADCOPTER_ITEM, new GeoItemRenderer<>(new QuadcopterItemModel()));
        EntityRendererRegistry.INSTANCE.register(Quadz.QUADCOPTER_ENTITY, (entityRenderDispatcher, context) -> new QuadcopterEntityRenderer(entityRenderDispatcher));
    }

    public static boolean isInThirdPerson() {
        return !MinecraftClient.getInstance().options.getPerspective().isFirstPerson();
    }
}
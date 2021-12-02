package dev.lazurite.quadz.client.render;

import dev.lazurite.quadz.Quadz;
import dev.lazurite.quadz.client.render.model.QuadcopterItemModel;
import dev.lazurite.quadz.client.render.renderer.QuadcopterEntityRenderer;
import dev.lazurite.quadz.client.resource.SplashResourceLoader;
import dev.lazurite.quadz.common.data.util.TemplateResourceLoader;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.Minecraft;
import software.bernie.geckolib3.GeckoLib;
import software.bernie.geckolib3.renderers.geo.GeoItemRenderer;

@Environment(EnvType.CLIENT)
public class QuadzRendering {
    public static final TemplateResourceLoader templateLoader = new TemplateResourceLoader();
    public static final SplashResourceLoader splashLoader = new SplashResourceLoader();

    public static void initialize() {
        GeckoLib.initialize();
//        GeoArmorRenderer.registerArmorRenderer(GogglesItem.class, context -> new GogglesItemRenderer(context));
        GeoItemRenderer.registerItemRenderer(Quadz.QUADCOPTER_ITEM, new GeoItemRenderer<>(new QuadcopterItemModel()));
        EntityRendererRegistry.register(Quadz.QUADCOPTER_ENTITY, QuadcopterEntityRenderer::new);
    }

    public static boolean isInThirdPerson() {
        return !Minecraft.getInstance().options.getCameraType().isFirstPerson();
    }
}
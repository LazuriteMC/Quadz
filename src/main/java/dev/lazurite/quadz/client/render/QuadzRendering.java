package dev.lazurite.quadz.client.render;

import dev.lazurite.quadz.Quadz;
import dev.lazurite.quadz.client.render.quadcopter.QuadcopterEntityRenderer;
import dev.lazurite.quadz.common.data.util.SplashResourceLoader;
import dev.lazurite.quadz.common.data.util.TemplateResourceLoader;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.rendereregistry.v1.EntityRendererRegistry;
import software.bernie.geckolib3.GeckoLib;

@Environment(EnvType.CLIENT)
public class QuadzRendering {
    public static final TemplateResourceLoader templateLoader = new TemplateResourceLoader();
    public static final SplashResourceLoader splashLoader = new SplashResourceLoader();

    public static void initialize() {
        GeckoLib.initialize();
        EntityRendererRegistry.INSTANCE.register(Quadz.QUADCOPTER_ENTITY, (entityRenderDispatcher, context) -> new QuadcopterEntityRenderer(entityRenderDispatcher));
    }
}
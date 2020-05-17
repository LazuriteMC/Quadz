package bluevista.fpvracingmod.client;

import com.bluevista.fpvracing.FPVRacingMod;
import com.bluevista.fpvracing.client.renderers.DroneRenderer;
import com.bluevista.fpvracing.server.entities.DroneEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@OnlyIn(Dist.CLIENT)
public class RendererRegistry {
    private static final Logger LOGGER = LogManager.getLogger(FPVRacingMod.MODID + " Renderer Registry");

    public static void registerEntityRenderers() {
        RenderingRegistry.registerEntityRenderingHandler(DroneEntity.class, DroneRenderer::new);
        LOGGER.debug("Registered entity renderers");
    }
}

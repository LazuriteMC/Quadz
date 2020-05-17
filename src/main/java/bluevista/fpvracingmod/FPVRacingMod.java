package bluevista.fpvracingmod;

import com.bluevista.fpvracing.client.RendererRegistry;
import com.bluevista.fpvracing.client.handler.RenderHandler;
import com.bluevista.fpvracing.server.EntityRegistry;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod.EventBusSubscriber(modid = com.bluevista.fpvracing.FPVRacingMod.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
@Mod(com.bluevista.fpvracing.FPVRacingMod.MODID)
public class FPVRacingMod {
    public static final String MODID = "fpvracingmod";

    public FPVRacingMod() {
        final IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        EntityRegistry.ENTITY_TYPES.register(modEventBus);
        MinecraftForge.EVENT_BUS.register(this);
        MinecraftForge.EVENT_BUS.register(RenderHandler.class);
    }

    @SubscribeEvent
    public static void onFMLClientSetupEvent(final FMLClientSetupEvent event) {
        RendererRegistry.registerEntityRenderers();
    }
}

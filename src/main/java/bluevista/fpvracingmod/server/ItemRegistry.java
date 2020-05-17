package bluevista.fpvracingmod.server;

import com.bluevista.fpvracing.FPVRacingMod;
import com.bluevista.fpvracing.server.items.ItemDroneSpawner;
import com.bluevista.fpvracing.server.items.ItemGoggles;
import com.bluevista.fpvracing.server.items.ItemTransmitter;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ObjectHolder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


@Mod.EventBusSubscriber(bus=Mod.EventBusSubscriber.Bus.MOD)
@ObjectHolder(FPVRacingMod.MODID)
public class ItemRegistry {

    private static final Logger LOGGER = LogManager.getLogger(FPVRacingMod.MODID + " Item Registry");

    @SubscribeEvent
    public static void registerItems(final RegistryEvent.Register<Item> event) {
        event.getRegistry().registerAll(
                new ItemDroneSpawner(new Item.Properties()
                        .maxStackSize(64)
                        .group(ItemGroup.MISC))
                        .setRegistryName(FPVRacingMod.MODID, "drone_spawner"),

                new ItemGoggles(new Item.Properties()
                        .maxStackSize(1)
                        .group(ItemGroup.MISC))
                        .setRegistryName(FPVRacingMod.MODID, "drone_goggles"),

                new ItemTransmitter(new Item.Properties()
                        .maxStackSize(1)
                        .group(ItemGroup.MISC))
                        .setRegistryName(FPVRacingMod.MODID, "drone_transmitter")
        );

        LOGGER.debug("Registered items");
    }
   
}
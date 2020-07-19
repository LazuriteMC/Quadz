package bluevista.fpvracingmod.server;

import bluevista.fpvracingmod.network.DroneInfoToServer;
import bluevista.fpvracingmod.network.EMPPacketHandler;
import bluevista.fpvracingmod.network.RemoveGogglesPacketHandler;
import bluevista.fpvracingmod.server.commands.FPVRacing;
import bluevista.fpvracingmod.server.entities.DroneEntity;
import bluevista.fpvracingmod.server.items.DroneSpawnerItem;
import bluevista.fpvracingmod.server.items.GogglesItem;
import bluevista.fpvracingmod.server.items.TransmitterItem;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.server.ServerStartCallback;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
public class ServerInitializer implements ModInitializer {
	public static final String MODID = "fpvracing";

	public static final GogglesItem GOGGLES_ITEM = new GogglesItem(new Item.Settings().group(ItemGroup.MISC).maxCount(1));
	public static final TransmitterItem TRANSMITTER_ITEM = new TransmitterItem(new Item.Settings().group(ItemGroup.MISC).maxCount(1));
	public static final DroneSpawnerItem DRONE_SPAWNER_ITEM = new DroneSpawnerItem(new Item.Settings().group(ItemGroup.MISC).maxCount(1));

	public static EntityType<Entity> DRONE_ENTITY;

	@Override
	public void onInitialize() {
		registerEntities();
		registerItems();
		registerNetwork();

		FPVRacing.registerCommands();
		ServerTick.register();

		ServerStartCallback.EVENT.register(ServerInitializer::start);
	}

	private void registerNetwork() {
		RemoveGogglesPacketHandler.register();
		DroneInfoToServer.register();
		EMPPacketHandler.register();
	}

	private void registerItems() {
		Registry.register(Registry.ITEM, new Identifier("fpvracing", "goggles_item"), GOGGLES_ITEM);
		Registry.register(Registry.ITEM, new Identifier("fpvracing", "transmitter_item"), TRANSMITTER_ITEM);
		Registry.register(Registry.ITEM, new Identifier("fpvracing", "drone_spawner_item"), DRONE_SPAWNER_ITEM);
	}

	private void registerEntities() {
		DRONE_ENTITY = Registry.register(
				Registry.ENTITY_TYPE,
				new Identifier(MODID, "drone_entity"),
				FabricEntityTypeBuilder.create(SpawnGroup.MISC, DroneEntity::new).dimensions(EntityDimensions.fixed(0.5F, 0.125F)).trackable(80, 3, true).build()
		);
	}

	public static void start(MinecraftServer server) {
		ServerTick.server = server;
	}
}

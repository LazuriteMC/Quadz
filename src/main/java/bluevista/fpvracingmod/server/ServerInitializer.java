package bluevista.fpvracingmod.server;

import bluevista.fpvracingmod.client.network.RemoveGogglesPacketHandler;
import bluevista.fpvracingmod.server.entities.DroneEntity;
import bluevista.fpvracingmod.server.items.DroneSpawnerItem;
import bluevista.fpvracingmod.server.items.GogglesItem;
import bluevista.fpvracingmod.server.items.TransmitterItem;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.entity.FabricEntityTypeBuilder;
import net.fabricmc.fabric.api.event.server.ServerTickCallback;
import net.fabricmc.fabric.api.network.ServerSidePacketRegistry;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class ServerInitializer implements ModInitializer {
	public static final GogglesItem GOGGLES_ITEM = new GogglesItem(new Item.Settings().group(ItemGroup.MISC).maxCount(1));
	public static final TransmitterItem TRANSMITTER_ITEM = new TransmitterItem(new Item.Settings().group(ItemGroup.MISC).maxCount(1));
	public static final DroneSpawnerItem DRONE_SPAWNER_ITEM = new DroneSpawnerItem(new Item.Settings().group(ItemGroup.MISC).maxCount(1));

	public static final Identifier REMOVE_GOGGLES_PACKET_ID = new Identifier("fpvracing", "goggles");
	public static EntityType<DroneEntity> DRONE_ENTITY;

	@Override
	public void onInitialize() {
		DRONE_ENTITY = Registry.register(
				Registry.ENTITY_TYPE,
				new Identifier("fpvracing", "drone_entity"),
				FabricEntityTypeBuilder.create(SpawnGroup.MISC, DroneEntity::new).trackable(100, 1, true).size(new EntityDimensions(0.5F, 0.125F, true)).build() // modify later if needed to fit the model
		);

		Registry.register(Registry.ITEM, new Identifier("fpvracing", "goggles_item"), GOGGLES_ITEM);
		Registry.register(Registry.ITEM, new Identifier("fpvracing", "transmitter_item"), TRANSMITTER_ITEM);
		Registry.register(Registry.ITEM, new Identifier("fpvracing", "drone_spawner_item"), DRONE_SPAWNER_ITEM);

		ServerTickCallback.EVENT.register(ServerTick::tick);
		ServerSidePacketRegistry.INSTANCE.register(REMOVE_GOGGLES_PACKET_ID, RemoveGogglesPacketHandler::accept);
	}
}

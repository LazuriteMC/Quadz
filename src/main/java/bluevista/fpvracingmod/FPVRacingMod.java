package bluevista.fpvracingmod;

import bluevista.fpvracingmod.server.entities.DroneEntity;
import bluevista.fpvracingmod.server.items.DroneSpawnerItem;
import bluevista.fpvracingmod.server.items.GogglesItem;
import bluevista.fpvracingmod.server.items.TransmitterItem;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.rendereregistry.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.entity.FabricEntityTypeBuilder;
import net.minecraft.entity.EntityCategory;
import net.minecraft.entity.EntityType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class FPVRacingMod implements ModInitializer {

	public static final GogglesItem GOGGLES_ITEM = new GogglesItem(new Item.Settings().group(ItemGroup.MISC));
	public static final TransmitterItem TRANSMITTER_ITEM = new TransmitterItem(new Item.Settings().group(ItemGroup.MISC));
	public static final DroneSpawnerItem DRONE_SPAWNER_ITEM = new DroneSpawnerItem(new Item.Settings().group(ItemGroup.MISC));

	public static final EntityType<DroneEntity> DRONE_ENTITY =
			Registry.register(
					Registry.ENTITY_TYPE,
					new Identifier("fpvracingmod", "drone_entity"),
					FabricEntityTypeBuilder.create(EntityCategory.MISC, DroneEntity::new).build()
			);

	@Override
	public void onInitialize() {
		// This code runs as soon as Minecraft is in a mod-load-ready state.
		// However, some things (like resources) may still be uninitialized.
		// Proceed with mild caution.

		Registry.register(Registry.ITEM, new Identifier("fpvracingmod", "goggles_item"), GOGGLES_ITEM);
		Registry.register(Registry.ITEM, new Identifier("fpvracingmod", "transmitter_item"), TRANSMITTER_ITEM);
		Registry.register(Registry.ITEM, new Identifier("fpvracingmod", "drone_spawner_item"), DRONE_SPAWNER_ITEM);

		System.out.println("Hello world!");
	}
}

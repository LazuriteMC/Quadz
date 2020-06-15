package bluevista.fpvracingmod.server;

import bluevista.fpvracingmod.server.entities.DroneEntity;
import bluevista.fpvracingmod.server.items.DroneSpawnerItem;
import bluevista.fpvracingmod.server.items.GogglesItem;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.entity.FabricEntityTypeBuilder;
import net.minecraft.entity.EntityCategory;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class ServerInitializer implements ModInitializer {

	public static final GogglesItem GOGGLES_ITEM = new GogglesItem(new Item.Settings().group(ItemGroup.MISC));
	public static final DroneSpawnerItem DRONE_SPAWNER_ITEM = new DroneSpawnerItem(new Item.Settings().group(ItemGroup.MISC));

	public static EntityType<DroneEntity> DRONE_ENTITY;

	@Override
	public void onInitialize() {
		DRONE_ENTITY = Registry.register(
				Registry.ENTITY_TYPE,
				new Identifier("fpvracing", "drone_entity"),
				FabricEntityTypeBuilder.create(EntityCategory.MISC, DroneEntity::new).size(new EntityDimensions(1.1F, 0.5234252623423423431234F, true)).build()
		);

		Registry.register(Registry.ITEM, new Identifier("fpvracing", "goggles_item"), GOGGLES_ITEM);
		Registry.register(Registry.ITEM, new Identifier("fpvracing", "drone_spawner_item"), DRONE_SPAWNER_ITEM);
	}
}

package bluevista.fpvracing.server;

import bluevista.fpvracing.config.Config;
import bluevista.fpvracing.network.config.ConfigC2S;
import bluevista.fpvracing.network.entity.EntityPhysicsC2S;
import bluevista.fpvracing.network.keybinds.EMPC2S;
import bluevista.fpvracing.network.keybinds.GodModeC2S;
import bluevista.fpvracing.network.keybinds.NoClipC2S;
import bluevista.fpvracing.network.keybinds.PowerGogglesC2S;
import bluevista.fpvracing.server.entities.QuadcopterEntity;
import bluevista.fpvracing.server.entities.FixedWingEntity;
import bluevista.fpvracing.server.entities.FlyableEntity;
import bluevista.fpvracing.server.items.QuadcopterItem;
import bluevista.fpvracing.server.items.GogglesItem;
import bluevista.fpvracing.server.items.TransmitterItem;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.itemgroup.FabricItemGroupBuilder;
import net.fabricmc.fabric.api.event.server.ServerStartCallback;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

import java.util.HashMap;
import java.util.UUID;

public class ServerInitializer implements ModInitializer {
	public static final String MODID = "fpvracing";

	/**The Minecraft Server for use in other classes */
	public static MinecraftServer server;

	/* Items */
	public static QuadcopterItem DRONE_SPAWNER_ITEM;
	public static GogglesItem GOGGLES_ITEM;
	public static TransmitterItem TRANSMITTER_ITEM;
	public static ItemGroup ITEM_GROUP;

	/* Entities */
	public static EntityType<QuadcopterEntity> QUADCOPTER_ENTITY;
	public static EntityType<FixedWingEntity> FIXED_WING_ENTITY;

	/*
	 * Client Information
	 *     - player configs
	 *     - player keybindings
	 */
	public static final HashMap<UUID, Config> SERVER_PLAYER_CONFIGS = new HashMap<>();
	public static final HashMap<UUID, String[]> SERVER_PLAYER_KEYS = new HashMap<>();

	@Override
	public void onInitialize() {
		registerEntities();
		registerItems();
		registerNetwork();

		Commands.registerCommands();
		ServerTick.register();

		ServerStartCallback.EVENT.register(ServerInitializer::start);
	}

	private void registerNetwork() {
		EntityPhysicsC2S.register();
		ConfigC2S.register();

		EMPC2S.register();
		NoClipC2S.register();
		GodModeC2S.register();
		PowerGogglesC2S.register();
	}

	private void registerItems() {
		DRONE_SPAWNER_ITEM = Registry.register(Registry.ITEM, new Identifier(MODID, "drone_spawner_item"), new QuadcopterItem(new Item.Settings().maxCount(1)));
		GOGGLES_ITEM = Registry.register(Registry.ITEM, new Identifier(MODID, "goggles_item"), new GogglesItem(new Item.Settings().maxCount(1)));
		TRANSMITTER_ITEM = Registry.register(Registry.ITEM, new Identifier(MODID, "transmitter_item"), new TransmitterItem(new Item.Settings().maxCount(1)));

		ITEM_GROUP = FabricItemGroupBuilder
				.create(new Identifier(MODID, "items"))
				.icon(() -> new ItemStack(GOGGLES_ITEM))
				.appendItems(stack -> {
					stack.add(new ItemStack(DRONE_SPAWNER_ITEM));
					stack.add(new ItemStack(GOGGLES_ITEM));
					stack.add(new ItemStack(TRANSMITTER_ITEM));
				})
				.build();
	}

	private void registerEntities() {
		QUADCOPTER_ENTITY = Registry.register(
				Registry.ENTITY_TYPE,
				new Identifier(MODID, "quadcopter_entity"),
				FabricEntityTypeBuilder.create(SpawnGroup.MISC, QuadcopterEntity::new).dimensions(EntityDimensions.fixed(0.5F, 0.125F)).trackable(FlyableEntity.TRACKING_RANGE, 3, true).build()
		);

		FIXED_WING_ENTITY = Registry.register(
				Registry.ENTITY_TYPE,
				new Identifier(MODID, "fixed_wing_entity"),
				FabricEntityTypeBuilder.create(SpawnGroup.MISC, FixedWingEntity::new).dimensions(EntityDimensions.fixed(0.5F, 0.125F)).trackable(FlyableEntity.TRACKING_RANGE, 3, true).build()
		);
	}

	public static void start(MinecraftServer server) {
		ServerInitializer.server = server;
	}
}

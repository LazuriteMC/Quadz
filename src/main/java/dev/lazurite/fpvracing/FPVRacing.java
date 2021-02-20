package dev.lazurite.fpvracing;

import dev.lazurite.fpvracing.api.event.JoystickEvents;
import dev.lazurite.fpvracing.client.config.Config;
import dev.lazurite.fpvracing.client.input.keybind.EMPKeybind;
import dev.lazurite.fpvracing.client.input.keybind.GodModeKeybind;
import dev.lazurite.fpvracing.client.input.keybind.GogglePowerKeybind;
import dev.lazurite.fpvracing.client.input.keybind.NoClipKeybind;
import dev.lazurite.fpvracing.client.packet.InputFrameC2S;
import dev.lazurite.fpvracing.client.packet.keybind.GodModeC2S;
import dev.lazurite.fpvracing.client.packet.keybind.NoClipC2S;
import dev.lazurite.fpvracing.client.packet.keybind.PowerGogglesC2S;
import dev.lazurite.fpvracing.client.render.entity.VoyagerRenderer;
import dev.lazurite.fpvracing.client.render.ui.toast.ControllerToast;
import dev.lazurite.fpvracing.common.tick.GogglesTick;
import dev.lazurite.fpvracing.client.input.tick.TransmitterTick;
import dev.lazurite.fpvracing.common.entity.quads.VoyagerEntity;
import dev.lazurite.fpvracing.common.item.container.GogglesContainer;
import dev.lazurite.fpvracing.common.entity.quads.VoxelRacerOneEntity;
import dev.lazurite.fpvracing.common.item.container.QuadcopterContainer;
import dev.lazurite.fpvracing.common.item.container.TransmitterContainer;
import dev.lazurite.fpvracing.client.packet.keybind.ElectromagneticPulseC2S;
import dev.lazurite.fpvracing.common.item.quads.VoyagerItem;
import dev.lazurite.fpvracing.common.packet.SelectedSlotS2C;
import dev.lazurite.fpvracing.common.packet.ShouldRenderPlayerS2C;
import dev.lazurite.fpvracing.common.item.ChannelWandItem;
import dev.lazurite.fpvracing.common.item.GogglesItem;
import dev.lazurite.fpvracing.common.item.quads.VoxelRacerOneItem;
import dev.lazurite.fpvracing.common.item.TransmitterItem;
import dev.lazurite.fpvracing.client.render.entity.VoxelRacerOneRenderer;
import dev.onyxstudios.cca.api.v3.component.ComponentKey;
import dev.onyxstudios.cca.api.v3.component.ComponentRegistryV3;
import dev.onyxstudios.cca.api.v3.item.ItemComponentFactoryRegistry;
import dev.onyxstudios.cca.api.v3.item.ItemComponentInitializer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.itemgroup.FabricItemGroupBuilder;
import net.fabricmc.fabric.api.client.rendereregistry.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import software.bernie.geckolib3.GeckoLib;

public class FPVRacing implements ModInitializer, ClientModInitializer, ItemComponentInitializer {
	public static final String MODID = "fpvracing";
	public static final Logger LOGGER = LogManager.getLogger("FPV Racing");

	/* CCA Component Keys */
	public static final ComponentKey<GogglesContainer> GOGGLES_CONTAINER = ComponentRegistryV3.INSTANCE.getOrCreate(new Identifier(MODID, "goggles_container"), GogglesContainer.class);
	public static final ComponentKey<TransmitterContainer> TRANSMITTER_CONTAINER = ComponentRegistryV3.INSTANCE.getOrCreate(new Identifier(MODID, "transmitter_container"), TransmitterContainer.class);
	public static final ComponentKey<QuadcopterContainer> QUADCOPTER_CONTAINER = ComponentRegistryV3.INSTANCE.getOrCreate(new Identifier(MODID, "quadcopter_container"), QuadcopterContainer.class);

	/* Items */
	public static GogglesItem GOGGLES_ITEM = Registry.register(Registry.ITEM, new Identifier(MODID, "goggles_item"), new GogglesItem(new Item.Settings().maxCount(1)));
	public static TransmitterItem TRANSMITTER_ITEM = Registry.register(Registry.ITEM, new Identifier(MODID, "transmitter_item"), new TransmitterItem(new Item.Settings().maxCount(1)));
	public static ChannelWandItem CHANNEL_WAND_ITEM = Registry.register(Registry.ITEM, new Identifier(MODID, "channel_wand_item"), new ChannelWandItem(new Item.Settings().maxCount(1)));
	public static ItemGroup ITEM_GROUP;

	/* Quadcopter Items */
	public static VoxelRacerOneItem VOXEL_RACER_ONE_ITEM = Registry.register(Registry.ITEM, new Identifier(MODID, "voxel_racer_one_item"), new VoxelRacerOneItem(new Item.Settings().maxCount(1)));
	public static VoyagerItem VOYAGER_ITEM = Registry.register(Registry.ITEM, new Identifier(MODID, "voyager_item"), new VoyagerItem(new Item.Settings().maxCount(1)));

	/* Quadcopter Entities */
	public static EntityType<VoxelRacerOneEntity> VOXEL_RACER_ONE;
	public static EntityType<VoyagerEntity> VOYAGER;

	@Override
	public void onInitialize() {
		/* Register Packets */
		NoClipC2S.register();
		GodModeC2S.register();
		PowerGogglesC2S.register();
		ElectromagneticPulseC2S.register();
		InputFrameC2S.register();

		/* Register Creative Tab */
		ITEM_GROUP = FabricItemGroupBuilder.create(new Identifier(MODID, "items"))
				.icon(() -> new ItemStack(GOGGLES_ITEM))
				.appendItems(stack -> {
					stack.add(new ItemStack(GOGGLES_ITEM));
					stack.add(new ItemStack(TRANSMITTER_ITEM));
					stack.add(new ItemStack(CHANNEL_WAND_ITEM));
					stack.add(new ItemStack(VOXEL_RACER_ONE_ITEM));
					stack.add(new ItemStack(VOYAGER_ITEM));
				}).build();

		/* Register Entities */
		VOXEL_RACER_ONE = Registry.register(
				Registry.ENTITY_TYPE,
				new Identifier(MODID, "voxel_racer_one"),
				FabricEntityTypeBuilder.createMob()
						.entityFactory(VoxelRacerOneEntity::new)
						.spawnGroup(SpawnGroup.MISC)
						.dimensions(EntityDimensions.fixed(0.5F, 0.125F))
						.defaultAttributes(AnimalEntity::createMobAttributes)
						.build());

		VOYAGER = Registry.register(
				Registry.ENTITY_TYPE,
				new Identifier(MODID, "voyager"),
				FabricEntityTypeBuilder.createMob()
						.entityFactory(VoyagerEntity::new)
						.spawnGroup(SpawnGroup.MISC)
						.dimensions(EntityDimensions.fixed(1.0F, 0.125F))
						.defaultAttributes(AnimalEntity::createMobAttributes)
						.build());

		ServerTickEvents.START_SERVER_TICK.register(GogglesTick::tick);
	}

	@Override
	public void onInitializeClient() {
		GeckoLib.initialize();

		/* Load the Config */
		Config.getInstance().load();

		/* Register Keybindings */
		GogglePowerKeybind.register();
		GodModeKeybind.register();
		NoClipKeybind.register();
		EMPKeybind.register();

		/* Register Packets */
		SelectedSlotS2C.register();
		ShouldRenderPlayerS2C.register();

		/* Register Renderers */
		EntityRendererRegistry.INSTANCE.register(VOXEL_RACER_ONE, (entityRenderDispatcher, context) -> new VoxelRacerOneRenderer(entityRenderDispatcher));
		EntityRendererRegistry.INSTANCE.register(VOYAGER, (entityRenderDispatcher, context) -> new VoyagerRenderer(entityRenderDispatcher));

		/* Register Toast Events */
		JoystickEvents.JOYSTICK_CONNECT.register((id, name) -> ControllerToast.add(new TranslatableText("toast.fpvracing.controller.connect"), name));
		JoystickEvents.JOYSTICK_DISCONNECT.register((id, name) -> ControllerToast.add(new TranslatableText("toast.fpvracing.controller.disconnect"), name));

		/* Register Client Tick Events */
		ClientTickEvents.START_CLIENT_TICK.register(TransmitterTick::tick);
	}

	@Override
	public void registerItemComponentFactories(ItemComponentFactoryRegistry registry) {
		registry.registerFor(new Identifier(MODID, "goggles_item"), GOGGLES_CONTAINER, GogglesContainer::new);
		registry.registerFor(new Identifier(MODID, "transmitter_item"), TRANSMITTER_CONTAINER, TransmitterContainer::new);
		registry.registerFor(new Identifier(MODID, "voxel_racer_one_item"), QUADCOPTER_CONTAINER, QuadcopterContainer::new);
		registry.registerFor(new Identifier(MODID, "voyager_item"), QUADCOPTER_CONTAINER, QuadcopterContainer::new);
	}
}

package dev.lazurite.quadz;

import dev.lazurite.quadz.api.event.JoystickEvents;
import dev.lazurite.quadz.client.Config;
import dev.lazurite.quadz.client.input.keybind.key.*;
import dev.lazurite.quadz.client.input.frame.InputFrameC2S;
import dev.lazurite.quadz.client.input.keybind.net.*;
import dev.lazurite.quadz.client.ClientTick;
import dev.lazurite.quadz.client.render.ui.toast.ControllerToast;
import dev.lazurite.quadz.common.entity.quads.PixelEntity;
import dev.lazurite.quadz.common.item.quads.PixelItem;
import dev.lazurite.quadz.common.ServerTick;
import dev.lazurite.quadz.common.entity.quads.VoyagerEntity;
import dev.lazurite.quadz.common.item.container.GogglesContainer;
import dev.lazurite.quadz.common.entity.quads.VoxelRacerOneEntity;
import dev.lazurite.quadz.common.item.container.QuadcopterContainer;
import dev.lazurite.quadz.common.item.container.TransmitterContainer;
import dev.lazurite.quadz.common.item.quads.VoyagerItem;
import dev.lazurite.quadz.common.util.net.SelectedSlotS2C;
import dev.lazurite.quadz.common.item.ChannelWandItem;
import dev.lazurite.quadz.common.item.GogglesItem;
import dev.lazurite.quadz.common.item.quads.VoxelRacerOneItem;
import dev.lazurite.quadz.common.item.TransmitterItem;
import dev.onyxstudios.cca.api.v3.component.ComponentKey;
import dev.onyxstudios.cca.api.v3.component.ComponentRegistryV3;
import dev.onyxstudios.cca.api.v3.item.ItemComponentFactoryRegistry;
import dev.onyxstudios.cca.api.v3.item.ItemComponentInitializer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.itemgroup.FabricItemGroupBuilder;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Quadz implements ModInitializer, ClientModInitializer, ItemComponentInitializer {
	public static final String MODID = "quadz";
	public static final Logger LOGGER = LogManager.getLogger("Quadz");

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
	public static PixelItem PIXEL_ITEM = Registry.register(Registry.ITEM, new Identifier(MODID, "pixel_item"), new PixelItem(new Item.Settings().maxCount(1)));

	/* Quadcopter Entities */
	public static EntityType<VoxelRacerOneEntity> VOXEL_RACER_ONE;
	public static EntityType<VoyagerEntity> VOYAGER;
	public static EntityType<PixelEntity> PIXEL;

	@Override
	public void onInitialize() {
		/* Register Packets */
		ServerPlayNetworking.registerGlobalReceiver(NoClipC2S.PACKET_ID, NoClipC2S::accept);
		ServerPlayNetworking.registerGlobalReceiver(GodModeC2S.PACKET_ID, GodModeC2S::accept);
		ServerPlayNetworking.registerGlobalReceiver(PowerGogglesC2S.PACKET_ID, PowerGogglesC2S::accept);
		ServerPlayNetworking.registerGlobalReceiver(ElectromagneticPulseC2S.PACKET_ID, ElectromagneticPulseC2S::accept);
		ServerPlayNetworking.registerGlobalReceiver(ChangeCameraAngleC2S.PACKET_ID, ChangeCameraAngleC2S::accept);
		ServerPlayNetworking.registerGlobalReceiver(InputFrameC2S.PACKET_ID, InputFrameC2S::accept);

		/* Register Creative Tab */
		ITEM_GROUP = FabricItemGroupBuilder.create(new Identifier(MODID, "items"))
				.icon(() -> new ItemStack(GOGGLES_ITEM))
				.appendItems(stack -> {
					stack.add(new ItemStack(GOGGLES_ITEM));
					stack.add(new ItemStack(TRANSMITTER_ITEM));
					stack.add(new ItemStack(CHANNEL_WAND_ITEM));
					stack.add(new ItemStack(VOXEL_RACER_ONE_ITEM));
					stack.add(new ItemStack(VOYAGER_ITEM));
					stack.add(new ItemStack(PIXEL_ITEM));
				}).build();

		/* Register Entities */
		VOXEL_RACER_ONE = Registry.register(
				Registry.ENTITY_TYPE,
				new Identifier(MODID, "voxel_racer_one"),
				FabricEntityTypeBuilder.createLiving()
						.entityFactory(VoxelRacerOneEntity::new)
						.spawnGroup(SpawnGroup.MISC)
						.dimensions(EntityDimensions.fixed(0.55F, 0.3F))
						.defaultAttributes(LivingEntity::createLivingAttributes)
						.build());

		VOYAGER = Registry.register(
				Registry.ENTITY_TYPE,
				new Identifier(MODID, "voyager"),
				FabricEntityTypeBuilder.createLiving()
						.entityFactory(VoyagerEntity::new)
						.spawnGroup(SpawnGroup.MISC)
						.dimensions(EntityDimensions.changing(1.1F, 0.25F))
						.defaultAttributes(LivingEntity::createLivingAttributes)
						.build());

		PIXEL = Registry.register(
				Registry.ENTITY_TYPE,
				new Identifier(MODID, "pixel"),
				FabricEntityTypeBuilder.createLiving()
						.entityFactory(PixelEntity::new)
						.spawnGroup(SpawnGroup.MISC)
						.dimensions(EntityDimensions.fixed(0.225F, 0.125F))
						.defaultAttributes(LivingEntity::createLivingAttributes)
						.build());

		ServerTickEvents.START_SERVER_TICK.register(ServerTick::tick);
	}

	@Override
	public void onInitializeClient() {
		/* Load the Config */
		Config.getInstance().load();

		/* Register Keybindings */
		RaiseCameraAngleKeybind.register();
		LowerCameraAngleKeybind.register();
		GogglePowerKeybind.register();
		GodModeKeybind.register();
		NoClipKeybind.register();
		EMPKeybind.register();

		/* Register Packets */
		ClientPlayNetworking.registerGlobalReceiver(SelectedSlotS2C.PACKET_ID, SelectedSlotS2C::accept);

		/* Register Toast Events */
		JoystickEvents.JOYSTICK_CONNECT.register((id, name) -> ControllerToast.add(new TranslatableText("toast.fpvracing.controller.connect"), name));
		JoystickEvents.JOYSTICK_DISCONNECT.register((id, name) -> ControllerToast.add(new TranslatableText("toast.fpvracing.controller.disconnect"), name));

		/* Register Client Tick Events */
		ClientTickEvents.START_CLIENT_TICK.register(ClientTick::tick);
	}

	@Override
	public void registerItemComponentFactories(ItemComponentFactoryRegistry registry) {
		registry.registerFor(new Identifier(MODID, "goggles_item"), GOGGLES_CONTAINER, GogglesContainer::new);
		registry.registerFor(new Identifier(MODID, "transmitter_item"), TRANSMITTER_CONTAINER, TransmitterContainer::new);
		registry.registerFor(new Identifier(MODID, "voxel_racer_one_item"), QUADCOPTER_CONTAINER, QuadcopterContainer::new);
		registry.registerFor(new Identifier(MODID, "voyager_item"), QUADCOPTER_CONTAINER, QuadcopterContainer::new);
		registry.registerFor(new Identifier(MODID, "pixel_item"), QUADCOPTER_CONTAINER, QuadcopterContainer::new);
	}
}

package dev.lazurite.quadz;

import dev.lazurite.quadz.api.event.JoystickEvents;
import dev.lazurite.quadz.client.network.ClientNetworkHandler;
import dev.lazurite.quadz.client.Config;
import dev.lazurite.quadz.client.input.keybind.*;
import dev.lazurite.quadz.client.util.ClientTick;
import dev.lazurite.quadz.client.render.QuadzRendering;
import dev.lazurite.quadz.client.render.ui.toast.ControllerConnectedToast;
import dev.lazurite.quadz.common.item.GogglesItem;
import dev.lazurite.quadz.common.network.CommonNetworkHandler;
import dev.lazurite.quadz.common.data.DataDriver;
import dev.lazurite.quadz.common.item.QuadcopterItem;
import dev.lazurite.quadz.common.util.ServerTick;
import dev.lazurite.quadz.common.item.group.ItemGroupHandler;
import dev.lazurite.quadz.common.network.KeybindNetworkHandler;
import dev.lazurite.quadz.common.state.entity.QuadcopterEntity;
import dev.lazurite.rayon.core.impl.util.event.BetterClientLifecycleEvents;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.minecraft.entity.*;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Quadz implements ModInitializer, ClientModInitializer {
	public static final String MODID = "quadz";
	public static final Logger LOGGER = LogManager.getLogger("Quadz");

	/* Packet Identifiers */
	public static final Identifier TEMPLATE = new Identifier(MODID, "template_s2c");
	public static final Identifier QUADCOPTER_SETTINGS_C2S = new Identifier(MODID, "quadcopter_settings_c2s");
	public static final Identifier PLAYER_DATA_C2S = new Identifier(MODID, "player_data_c2s");
	public static final Identifier INPUT_FRAME = new Identifier(MODID, "input_frame");

	public static final Identifier NOCLIP_C2S = new Identifier(MODID, "noclip_c2s");
	public static final Identifier CHANGE_CAMERA_ANGLE_C2S = new Identifier(MODID, "change_camera_angle_c2s");
	public static final Identifier POWER_GOGGLES_C2S = new Identifier(MODID, "power_goggles_c2s");
	public static final Identifier GOD_MODE_C2S = new Identifier(MODID, "godmode_c2s");

	/* Items */
	public static QuadcopterItem QUADCOPTER_ITEM = Registry.register(Registry.ITEM, new Identifier(MODID, "quadcopter_item"), new QuadcopterItem(new Item.Settings().maxCount(1)));
	public static GogglesItem GOGGLES_ITEM = Registry.register(Registry.ITEM, new Identifier(MODID, "goggles_item"), new GogglesItem(new Item.Settings().maxCount(1)));
	public static Item TRANSMITTER_ITEM = Registry.register(Registry.ITEM, new Identifier(MODID, "transmitter_item"), new Item(new Item.Settings().maxCount(1)));

	/* Quadcopter Entities */
	public static EntityType<LivingEntity> QUADCOPTER_ENTITY = Registry.register(
			Registry.ENTITY_TYPE,
			new Identifier(MODID, "quadcopter"),
				FabricEntityTypeBuilder.createLiving()
						.entityFactory((type, world) -> new QuadcopterEntity(world))
						.spawnGroup(SpawnGroup.MISC)
						.dimensions(EntityDimensions.changing(0.5F, 0.2F))
						.defaultAttributes(LivingEntity::createLivingAttributes)
						.build());

	@Override
	public void onInitialize() {
		/* Set up the item group */
		ItemGroupHandler.getInstance().register(
				new ItemStack(GOGGLES_ITEM),
				new ItemStack(TRANSMITTER_ITEM)
		).build();

		DataDriver.initialize();

		/* Set up events */
		ServerTickEvents.START_SERVER_TICK.register(ServerTick::tick);
		ServerPlayConnectionEvents.JOIN.register((handler, sender, server) ->
			DataDriver.getTemplates().forEach(template -> sender.sendPacket(TEMPLATE, template.serialize())));

		/* Set up networking events */
		ServerPlayNetworking.registerGlobalReceiver(QUADCOPTER_SETTINGS_C2S, CommonNetworkHandler::onQuadcopterSettingsReceived);
		ServerPlayNetworking.registerGlobalReceiver(PLAYER_DATA_C2S, CommonNetworkHandler::onPlayerDataReceived);
		ServerPlayNetworking.registerGlobalReceiver(TEMPLATE, CommonNetworkHandler::onTemplateReceived);
		ServerPlayNetworking.registerGlobalReceiver(INPUT_FRAME, CommonNetworkHandler::onInputFrame);

		ServerPlayNetworking.registerGlobalReceiver(NOCLIP_C2S, KeybindNetworkHandler::onNoClipKey);
		ServerPlayNetworking.registerGlobalReceiver(CHANGE_CAMERA_ANGLE_C2S, KeybindNetworkHandler::onChangeCameraAngleKey);
		ServerPlayNetworking.registerGlobalReceiver(GOD_MODE_C2S, KeybindNetworkHandler::onGodModeKey);
		ServerPlayNetworking.registerGlobalReceiver(POWER_GOGGLES_C2S, KeybindNetworkHandler::onPowerGogglesKey);
	}

	@Override
	public void onInitializeClient() {
		QuadzRendering.initialize();

		/* Load the Config */
		Config.getInstance().load();

		/* Register Keybindings */
		ControlKeybinds.register();
		CameraAngleKeybinds.register();
		GodModeKeybind.register();
		NoClipKeybind.register();
		FollowKeybind.register();
		QuadConfigKeybind.register();

		/* Register Packets */
		ClientPlayNetworking.registerGlobalReceiver(TEMPLATE, ClientNetworkHandler::onTemplateReceived);
		ClientPlayNetworking.registerGlobalReceiver(INPUT_FRAME, ClientNetworkHandler::onInputFrameReceived);

		/* Register Toast Events */
		JoystickEvents.JOYSTICK_CONNECT.register((id, name) -> ControllerConnectedToast.add(new TranslatableText("toast.quadz.controller.connect"), name));
		JoystickEvents.JOYSTICK_DISCONNECT.register((id, name) -> ControllerConnectedToast.add(new TranslatableText("toast.quadz.controller.disconnect"), name));

		/* Register Client Tick Events */
		ClientTickEvents.START_CLIENT_TICK.register(ClientTick::tick);

		/* Set up events */
		BetterClientLifecycleEvents.DISCONNECT.register((client, world) -> DataDriver.clearRemoteTemplates());
		ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
			Config.getInstance().sendPlayerData();
			DataDriver.getTemplates().forEach(template -> sender.sendPacket(TEMPLATE, template.serialize()));
		});
	}
}

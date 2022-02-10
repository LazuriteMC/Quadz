package dev.lazurite.quadz;

import dev.lazurite.quadz.api.event.JoystickEvents;
import dev.lazurite.quadz.client.input.InputTick;
import dev.lazurite.quadz.client.network.ClientNetworkHandler;
import dev.lazurite.quadz.client.Config;
import dev.lazurite.quadz.client.input.keybind.*;
import dev.lazurite.quadz.client.util.ClientTick;
import dev.lazurite.quadz.client.render.QuadzRendering;
import dev.lazurite.quadz.client.render.ui.toast.ControllerConnectedToast;
import dev.lazurite.quadz.common.item.GogglesItem;
import dev.lazurite.quadz.common.network.CommonNetworkHandler;
import dev.lazurite.quadz.common.data.template.TemplateLoader;
import dev.lazurite.quadz.common.item.QuadcopterItem;
import dev.lazurite.quadz.common.util.ServerTick;
import dev.lazurite.quadz.common.item.group.ItemGroupHandler;
import dev.lazurite.quadz.common.network.KeybindNetworkHandler;
import dev.lazurite.quadz.common.state.entity.QuadcopterEntity;
import dev.lazurite.rayon.api.event.collision.PhysicsSpaceEvents;
import dev.lazurite.rayon.impl.bullet.collision.body.EntityRigidBody;
import dev.lazurite.toolbox.api.event.ClientEvents;
import dev.lazurite.toolbox.api.event.ServerEvents;
import dev.lazurite.toolbox.api.network.ClientNetworking;
import dev.lazurite.toolbox.api.network.PacketRegistry;
import dev.lazurite.toolbox.api.network.ServerNetworking;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Quadz implements ModInitializer, ClientModInitializer {
	public static final String MODID = "quadz";
	public static final Logger LOGGER = LogManager.getLogger("Quadz");

	public static final ResourceLocation TEMPLATE = new ResourceLocation(MODID, "template_s2c");
	public static final ResourceLocation QUADCOPTER_SETTINGS_C2S = new ResourceLocation(MODID, "quadcopter_settings_c2s");
	public static final ResourceLocation PLAYER_DATA_C2S = new ResourceLocation(MODID, "player_data_c2s");
	public static final ResourceLocation INPUT_FRAME = new ResourceLocation(MODID, "input_frame");
	public static final ResourceLocation REQUEST_QUADCOPTER_VIEW_C2S = new ResourceLocation(MODID, "request_quadcopter_view_c2s");
	public static final ResourceLocation REQUEST_PLAYER_VIEW_C2S = new ResourceLocation(MODID, "request_player_view_c2s");

	public static final ResourceLocation NOCLIP_C2S = new ResourceLocation(MODID, "noclip_c2s");
	public static final ResourceLocation CHANGE_CAMERA_ANGLE_C2S = new ResourceLocation(MODID, "change_camera_angle_c2s");

	public static QuadcopterItem QUADCOPTER_ITEM = Registry.register(Registry.ITEM, new ResourceLocation(MODID, "quadcopter_item"), new QuadcopterItem(new Item.Properties().stacksTo(1)));
	public static GogglesItem GOGGLES_ITEM = Registry.register(Registry.ITEM, new ResourceLocation(MODID, "goggles_item"), new GogglesItem(new Item.Properties().stacksTo(1)));
	public static Item TRANSMITTER_ITEM = Registry.register(Registry.ITEM, new ResourceLocation(MODID, "transmitter_item"), new Item(new Item.Properties().stacksTo(1)));

	public static EntityType<QuadcopterEntity> QUADCOPTER_ENTITY = Registry.register(
			Registry.ENTITY_TYPE,
			new ResourceLocation(MODID, "quadcopter"),
				FabricEntityTypeBuilder.createLiving()
						.entityFactory(QuadcopterEntity::new)
						.spawnGroup(MobCategory.MISC)
						.dimensions(EntityDimensions.scalable(0.5F, 0.2F))
						.defaultAttributes(LivingEntity::createLivingAttributes)
						.build());

	@Override
	public void onInitialize() {
		/* Set up the item group */
		ItemGroupHandler.getInstance().register(
				new ItemStack(GOGGLES_ITEM),
				new ItemStack(TRANSMITTER_ITEM)
		).build();

		TemplateLoader.initialize();

		PhysicsSpaceEvents.STEP.register(space -> {
			space.getRigidBodiesByClass(EntityRigidBody.class).forEach(rigidBody -> {
				if (rigidBody.getElement() instanceof QuadcopterEntity quadcopter) {
					quadcopter.step();
				}
			});
		});

		/* Set up events */
		ServerEvents.Tick.START_SERVER_TICK.register(ServerTick::tick);
		ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> // TODO still on fabric
			TemplateLoader.getTemplates().forEach(template -> ServerNetworking.send(handler.player, TEMPLATE, template::serialize)));

		/* Set up networking events */
		PacketRegistry.registerServerbound(QUADCOPTER_SETTINGS_C2S, CommonNetworkHandler::onQuadcopterSettingsReceived);
		PacketRegistry.registerServerbound(PLAYER_DATA_C2S, CommonNetworkHandler::onPlayerDataReceived);
		PacketRegistry.registerServerbound(TEMPLATE, CommonNetworkHandler::onTemplateReceived);
		PacketRegistry.registerServerbound(INPUT_FRAME, CommonNetworkHandler::onInputFrame);
		PacketRegistry.registerServerbound(REQUEST_QUADCOPTER_VIEW_C2S, CommonNetworkHandler::onQuadcopterViewRequestReceived);
		PacketRegistry.registerServerbound(REQUEST_PLAYER_VIEW_C2S, CommonNetworkHandler::onPlayerViewRequestReceived);

		PacketRegistry.registerServerbound(NOCLIP_C2S, KeybindNetworkHandler::onNoClipKey);
		PacketRegistry.registerServerbound(CHANGE_CAMERA_ANGLE_C2S, KeybindNetworkHandler::onChangeCameraAngleKey);
	}

	@Override
	public void onInitializeClient() {
		QuadzRendering.initialize();

		/* Load the Config */
		Config.getInstance().load();

		/* Register Keybindings */
		CameraAngleKeybinds.register();
		NoClipKeybind.register();
		ControlKeybinds.register();
		FollowKeybind.register();
		QuadConfigKeybind.register();

		/* Register Packets */
		PacketRegistry.registerClientbound(TEMPLATE, ClientNetworkHandler::onTemplateReceived);
		PacketRegistry.registerClientbound(INPUT_FRAME, ClientNetworkHandler::onInputFrameReceived);

		/* Register Toast Events */
		JoystickEvents.JOYSTICK_CONNECT.register(
				(id, name) -> ControllerConnectedToast.add(new TranslatableComponent("toast.quadz.controller.connect"), name));
		JoystickEvents.JOYSTICK_DISCONNECT.register(
				(id, name) -> ControllerConnectedToast.add(new TranslatableComponent("toast.quadz.controller.disconnect"), name));

		/* Register Client Tick Events */
		ClientEvents.Tick.START_LEVEL_TICK.register(ClientTick::tickInput);

		/* Set up events */
		ClientEvents.Lifecycle.DISCONNECT.register((client, world) -> TemplateLoader.clearRemoteTemplates());
		ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> { // TODO still on fabric
			Config.getInstance().sendPlayerData();
			TemplateLoader.getTemplates().forEach(template -> ClientNetworking.send(TEMPLATE, template::serialize));
		});
		InputTick.LEFT_CLICK_EVENT.register(() -> ClientNetworking.send(REQUEST_QUADCOPTER_VIEW_C2S, buf -> buf.writeInt(-1)));
		InputTick.RIGHT_CLICK_EVENT.register(() -> ClientNetworking.send(REQUEST_QUADCOPTER_VIEW_C2S, buf -> buf.writeInt(1)));
	}
}

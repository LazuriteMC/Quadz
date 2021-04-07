package dev.lazurite.quadz;

import dev.lazurite.quadz.api.event.JoystickEvents;
import dev.lazurite.quadz.client.ClientNetworkHandler;
import dev.lazurite.quadz.client.Config;
import dev.lazurite.quadz.client.input.InputTick;
import dev.lazurite.quadz.client.input.keybind.*;
import dev.lazurite.quadz.client.ClientTick;
import dev.lazurite.quadz.client.render.QuadzRendering;
import dev.lazurite.quadz.client.render.ui.toast.ControllerConnectedToast;
import dev.lazurite.quadz.common.CommonNetworkHandler;
import dev.lazurite.quadz.common.data.DataDriver;
import dev.lazurite.quadz.common.item.QuadcopterItem;
import dev.lazurite.quadz.common.ServerTick;
import dev.lazurite.quadz.common.item.GogglesItem;
import dev.lazurite.quadz.common.item.group.ItemGroupHandler;
import dev.lazurite.quadz.common.state.entity.QuadcopterEntity;
import dev.lazurite.rayon.core.api.event.ElementCollisionEvents;
import dev.lazurite.rayon.core.impl.util.event.BetterClientLifecycleEvents;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
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
	public static final Identifier SELECTED_SLOT_S2C = new Identifier(MODID, "selected_slot_s2c");
	public static final Identifier NOCLIP_C2S = new Identifier(MODID, "noclip_c2s");
	public static final Identifier CHANGE_CAMERA_ANGLE_C2S = new Identifier(MODID, "change_camera_angle_c2s");
	public static final Identifier POWER_GOGGLES_C2S = new Identifier(MODID, "power_goggles_c2s");
	public static final Identifier GOD_MODE_C2S = new Identifier(MODID, "godmode_c2s");
	public static final Identifier INPUT_FRAME_C2S = new Identifier(MODID, "input_frame_c2s");
	public static final Identifier FREQUENCY_C2S = new Identifier(MODID, "frequency_c2s");

	/* Items */
	public static QuadcopterItem QUADCOPTER_ITEM = Registry.register(Registry.ITEM, new Identifier(MODID, "quadcopter_item"), new QuadcopterItem(new Item.Settings().maxCount(1)));
	public static GogglesItem GOGGLES_ITEM = Registry.register(Registry.ITEM, new Identifier(MODID, "goggles_item"), new GogglesItem(new Item.Settings().maxCount(1)));
	public static Item TRANSMITTER_ITEM = Registry.register(Registry.ITEM, new Identifier(MODID, "transmitter_item"), new Item(new Item.Settings().maxCount(1)));
	public static Item CHANNEL_WAND_ITEM = Registry.register(Registry.ITEM, new Identifier(MODID, "channel_wand_item"), new Item(new Item.Settings().maxCount(1)));

	/* Quadcopter Entities */
	public static EntityType<LivingEntity> QUADCOPTER_ENTITY = Registry.register(
			Registry.ENTITY_TYPE,
			new Identifier(MODID, "quadcopter"),
				FabricEntityTypeBuilder.createLiving()
						.entityFactory((type, world) -> new QuadcopterEntity(world))
						.spawnGroup(SpawnGroup.MISC)
						.dimensions(EntityDimensions.changing(0.55F, 0.3F))
						.defaultAttributes(LivingEntity::createLivingAttributes)
						.build());

	@Override
	public void onInitialize() {
		/* Set up the item group */
		ItemGroupHandler.getInstance().register(
				new ItemStack(GOGGLES_ITEM),
				new ItemStack(TRANSMITTER_ITEM),
				new ItemStack(CHANNEL_WAND_ITEM)
		).build();

		DataDriver.initialize();

		ServerTickEvents.START_SERVER_TICK.register(ServerTick::tick);
		ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
			DataDriver.getTemplates().forEach(template -> sender.sendPacket(TEMPLATE, template.serialize()));
		});

		ElementCollisionEvents.BLOCK_COLLISION.register((executor, element, block, impulse) -> {
			if (element instanceof QuadcopterEntity){
				QuadcopterEntity quadcopter = (QuadcopterEntity) element;

				if (!quadcopter.getEntityWorld().isClient()) {
					Block blockType = block.getBlockState().getBlock();

					if (impulse > 10 || blockType.equals(Blocks.CACTUS) || blockType.equals(Blocks.MAGMA_BLOCK)) {
						System.out.println("impulse: " + impulse);
						executor.execute(quadcopter::disable);
					}
				}
			}
		});

		ServerPlayNetworking.registerGlobalReceiver(TEMPLATE, CommonNetworkHandler::onTemplateReceived);
		ServerPlayNetworking.registerGlobalReceiver(NOCLIP_C2S, CommonNetworkHandler::onNoClipKey);
		ServerPlayNetworking.registerGlobalReceiver(CHANGE_CAMERA_ANGLE_C2S, CommonNetworkHandler::onChangeCameraAngleKey);
		ServerPlayNetworking.registerGlobalReceiver(GOD_MODE_C2S, CommonNetworkHandler::onGodModeKey);
		ServerPlayNetworking.registerGlobalReceiver(POWER_GOGGLES_C2S, CommonNetworkHandler::onPowerGogglesKey);
		ServerPlayNetworking.registerGlobalReceiver(INPUT_FRAME_C2S, CommonNetworkHandler::onInputFrame);
		ServerPlayNetworking.registerGlobalReceiver(FREQUENCY_C2S, CommonNetworkHandler::onFrequencyReceived);
	}

	@Override
	public void onInitializeClient() {
		QuadzRendering.initialize();

		/* Load the Config */
		Config.getInstance().load();

		/* Register Keybindings */
		ControlKeybinds.register();
		CameraAngleKeybinds.register();
		GogglePowerKeybind.register();
		GodModeKeybind.register();
		NoClipKeybind.register();
		FollowKeybind.register();

		/* Register Packets */
		ClientPlayNetworking.registerGlobalReceiver(SELECTED_SLOT_S2C, ClientNetworkHandler::onSelectSlot);
		ClientPlayNetworking.registerGlobalReceiver(TEMPLATE, ClientNetworkHandler::onTemplateReceived);

		/* Register Toast Events */
		JoystickEvents.JOYSTICK_CONNECT.register((id, name) -> ControllerConnectedToast.add(new TranslatableText("toast.quadz.controller.connect"), name));
		JoystickEvents.JOYSTICK_DISCONNECT.register((id, name) -> ControllerConnectedToast.add(new TranslatableText("toast.quadz.controller.disconnect"), name));

		/* Register Client Tick Events */
		ClientTickEvents.START_CLIENT_TICK.register(ClientTick::tick);
		ClientTickEvents.START_CLIENT_TICK.register(InputTick.getInstance()::keyboardTick);

		BetterClientLifecycleEvents.DISCONNECT.register((client, world) -> DataDriver.clearRemoteTemplates());
		ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
			PacketByteBuf buf = PacketByteBufs.create();
			buf.writeChar(Config.getInstance().band);
			buf.writeInt(Config.getInstance().channel);
			sender.sendPacket(FREQUENCY_C2S, buf);
			DataDriver.getTemplates().forEach(template -> sender.sendPacket(TEMPLATE, template.serialize()));
		});
	}
}

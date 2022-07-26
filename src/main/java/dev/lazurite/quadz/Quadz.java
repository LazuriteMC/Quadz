package dev.lazurite.quadz;

import dev.lazurite.quadz.common.data.model.Bindable;
import dev.lazurite.quadz.common.data.template.TemplateLoader;
import dev.lazurite.quadz.common.entity.RemoteControllableEntity;
import dev.lazurite.quadz.common.item.GogglesItem;
import dev.lazurite.quadz.common.item.ItemGroupHandler;
import dev.lazurite.quadz.common.entity.QuadcopterEntity;
import dev.lazurite.quadz.common.item.QuadcopterItem;
import dev.lazurite.quadz.common.util.Mode;
import dev.lazurite.quadz.api.JoystickRegistry;
import dev.lazurite.quadz.common.data.Config;
import dev.lazurite.quadz.common.data.model.JoystickAxis;
import dev.lazurite.quadz.common.util.tools.RemoteControllableSearch;
import dev.lazurite.quadz.common.util.network.NetworkResources;
import dev.lazurite.toolbox.api.event.ServerEvents;
import dev.lazurite.toolbox.api.network.ServerNetworking;
import dev.lazurite.toolbox.api.util.PlayerUtil;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.Optional;

public class Quadz implements ModInitializer {
	public static final String MODID = "quadz";
	public static final Logger LOGGER = LogManager.getLogger("Quadz");

	public static QuadcopterItem QUADCOPTER_ITEM = Registry.register(Registry.ITEM, new ResourceLocation(MODID, "remote_controllable_item"), new QuadcopterItem(new Item.Properties().stacksTo(1)));
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
		TemplateLoader.initialize();

		/* Register Events */
		ServerEvents.Tick.START_SERVER_TICK.register(this::onServerTick);
		ServerEvents.Lifecycle.JOIN.register(this::onJoin);

		/* Register Packets */
		NetworkResources.registerServerbound();

		ItemGroupHandler.getInstance().register(
				new ItemStack(GOGGLES_ITEM),
				new ItemStack(TRANSMITTER_ITEM)
		).build();

		JoystickRegistry.registerTemplateFolder(MODID);
		JoystickRegistry.getInstance().registerJoystickInput(new JoystickAxis(new ResourceLocation(MODID, "throttle"), Component.translatable("config.quadz.controller.throttle"), 0, false));
		JoystickRegistry.getInstance().registerJoystickInput(new JoystickAxis(new ResourceLocation(MODID, "pitch"), Component.translatable("config.quadz.controller.pitch"), 2, false));
		JoystickRegistry.getInstance().registerJoystickInput(new JoystickAxis(new ResourceLocation(MODID, "roll"), Component.translatable("config.quadz.controller.pitch"), 1, false));
		JoystickRegistry.getInstance().registerJoystickInput(new JoystickAxis(new ResourceLocation(MODID, "yaw"), Component.translatable("config.quadz.controller.pitch"), 3, true));

		JoystickRegistry.getInstance().registerControllableParameter(new BooleanParameter(new ResourceLocation(MODID, "throttleInCenter"), Component.translatable("config.quadz.controller.throttleInCenter"), Config.Category.CONTROLLER, false, true));
		JoystickRegistry.getInstance().registerControllableParameter(new IntegerParameter(new ResourceLocation(MODID, "maxAngle"), Component.translatable("config.quadz.controller.maxAngle"), Config.Category.CONTROLLER, 30, false, 10, 90, true));
		JoystickRegistry.getInstance().registerControllableParameter(new FloatParameter(new ResourceLocation(MODID, "rate"), Component.translatable("config.quadz.controller.rate"), Config.Category.CONTROLLER, 0.7f, false, 0.0f, 2.0f, true));
		JoystickRegistry.getInstance().registerControllableParameter(new FloatParameter(new ResourceLocation(MODID, "superRate"), Component.translatable("config.quadz.controller.superRate"), Config.Category.CONTROLLER, 0.9f, false, 0.0f, 2.0f, true));
		JoystickRegistry.getInstance().registerControllableParameter(new FloatParameter(new ResourceLocation(MODID, "expo"), Component.translatable("config.quadz.controller.expo"), Config.Category.CONTROLLER, 0.1f, false, 0.0f, 2.0f, true));
		JoystickRegistry.getInstance().registerControllableParameter(new EnumParameter<>(new ResourceLocation(MODID, "mode"), Component.translatable("config.quadz.controller.mode"), Config.Category.CONTROLLER, Mode.RATE, Mode.class, false));
	}

	protected void onJoin(ServerPlayer player) {
		TemplateLoader.getTemplates().forEach(template -> ServerNetworking.send(player, NetworkResources.TEMPLATE, template::serialize));
	}

	public void onServerTick(MinecraftServer server) {
		final var range = server.getPlayerList().getViewDistance() * 16;
		final var toActivate = new ArrayList<RemoteControllableEntity>();

		for (var player : PlayerUtil.all(server)) {
			final var pos = player.getCamera().position();
			final var level = player.getLevel();
			final var quads = RemoteControllableSearch.allInArea(level, pos, range);

			/* Don't forget the camera! */
			if (player.getCamera() instanceof RemoteControllableEntity remoteControllableEntity) {
				quads.add(remoteControllableEntity);
			}

			quads.forEach(quadcopter -> quadcopter.setActive(false));
			Bindable.get(player.getMainHandItem())
					.flatMap(transmitter -> RemoteControllableSearch.byBindId(level, pos, transmitter.getBindId(), range))
					.ifPresent(toActivate::add);

			if (Optional.of(player.getInventory().armor.get(3)).filter(stack -> stack.getItem() instanceof GogglesItem).isEmpty()) {
				if (player.getCamera() instanceof RemoteControllableEntity remoteControllableEntity) {

					// Quadz specific
					if (remoteControllableEntity instanceof QuadcopterEntity quadcopter && player.equals(quadcopter.getRigidBody().getPriorityPlayer())) {
						quadcopter.getRigidBody().prioritize(null);
					}

					player.setCamera(player);
				}
			}
		}

		toActivate.forEach(quadcopter -> quadcopter.setActive(true));
	}
}

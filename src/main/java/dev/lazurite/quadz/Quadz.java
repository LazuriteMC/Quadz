package dev.lazurite.quadz;

import dev.lazurite.form.api.event.TemplateEvents;
import dev.lazurite.form.api.loader.TemplateLoader;
import dev.lazurite.quadz.client.QuadzClient;
import dev.lazurite.quadz.common.item.TransmitterItem;
import dev.lazurite.rayon.impl.bullet.collision.body.shape.MinecraftShape;
import dev.lazurite.remote.api.Bindable;
import dev.lazurite.quadz.common.item.GogglesItem;
import dev.lazurite.quadz.common.item.ItemGroupHandler;
import dev.lazurite.quadz.common.entity.QuadcopterEntity;
import dev.lazurite.quadz.common.item.QuadcopterItem;
import dev.lazurite.remote.api.RemoteSearch;
import dev.lazurite.toolbox.api.event.ServerEvents;
import dev.lazurite.toolbox.api.network.PacketRegistry;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.*;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Optional;

public class Quadz implements ModInitializer {
	public static final String MODID = "quadz";
	public static final Logger LOGGER = LogManager.getLogger("Quadz");

	public static QuadcopterItem QUADCOPTER_ITEM = Registry.register(Registry.ITEM, new ResourceLocation(MODID, "remote_controllable_item"), new QuadcopterItem(new Item.Properties().stacksTo(1)));
	public static GogglesItem GOGGLES_ITEM = Registry.register(Registry.ITEM, new ResourceLocation(MODID, "goggles_item"), new GogglesItem(new Item.Properties().stacksTo(1)));
	public static TransmitterItem TRANSMITTER_ITEM = Registry.register(Registry.ITEM, new ResourceLocation(MODID, "transmitter_item"), new TransmitterItem(new Item.Properties().stacksTo(1)));

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
		ItemGroupHandler.getInstance().register(
				new ItemStack(GOGGLES_ITEM),
				new ItemStack(TRANSMITTER_ITEM)
		).build();

		TemplateLoader.initialize(MODID);

		TemplateLoader.getTemplateByModId(MODID).forEach(template -> {
			ItemGroupHandler.getInstance().register(
					TemplateLoader.getItemStackFor(template, QUADCOPTER_ITEM));
		});

		ServerEvents.Tick.START_SERVER_TICK.register(this::onServerTick);
		TemplateEvents.ENTITY_TEMPLATE_CHANGED.register(this::onEntityTemplateChanged);

		PacketRegistry.registerServerbound(QuadzClient.NOCLIP_C2S, this::onNoClipKey);
	}

	protected void onEntityTemplateChanged(Entity entity) {
		if (entity instanceof QuadcopterEntity quadcopter) {
			quadcopter.getRigidBody().setCollisionShape(MinecraftShape.convex(entity.getBoundingBox()));

			TemplateLoader.getTemplateById(quadcopter.getTemplate()).ifPresent(template -> {
				quadcopter.setCameraAngle(template.metadata().get("cameraAngle").getAsInt());
				quadcopter.getRigidBody().setMass(template.metadata().get("mass").getAsFloat());
				quadcopter.getRigidBody().setDragCoefficient(template.metadata().get("dragCoefficient").getAsFloat());
				quadcopter.setThrust(template.metadata().get("thrust").getAsFloat());
				quadcopter.setThrustCurve(template.metadata().get("thrustCurve").getAsFloat());
			});
		}
	}

	protected void onServerTick(MinecraftServer server) {
		server.getPlayerList().getPlayers().forEach(player -> {
			if (player.getCamera() instanceof QuadcopterEntity quadcopter && !player.equals(quadcopter.getRigidBody().getPriorityPlayer())) {
				quadcopter.getRigidBody().prioritize(player);
			}
		});
	}

	protected void onNoClipKey(PacketRegistry.ServerboundContext context) {
		final var player = context.player();

		Optional.ofNullable(player.getServer()).ifPresent(server -> {
			server.execute(() -> {
				Bindable.get(player.getMainHandItem()).ifPresent(bindable -> {
					RemoteSearch.byBindId(player.getLevel(), player.getCamera().position(), bindable.getBindId(), server.getPlayerList().getViewDistance()).ifPresent(remoteControllableEntity -> {
						if (remoteControllableEntity instanceof QuadcopterEntity quadcopter) {
							boolean lastNoClip = quadcopter.getRigidBody().terrainLoadingEnabled();
							quadcopter.getRigidBody().setTerrainLoadingEnabled(!lastNoClip);

							if (lastNoClip) {
								player.displayClientMessage(Component.translatable("message.quadz.noclip_on"), true);
							} else {
								player.displayClientMessage(Component.translatable("message.quadz.noclip_off"), true);
							}
						}
					});
				});
			});
		});
	}
}

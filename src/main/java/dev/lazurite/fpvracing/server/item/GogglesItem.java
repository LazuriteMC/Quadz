package dev.lazurite.fpvracing.server.item;

import dev.lazurite.fpvracing.server.entity.FlyableEntity;
import dev.lazurite.fpvracing.client.ClientInitializer;
import dev.lazurite.fpvracing.network.tracker.Config;
import dev.lazurite.fpvracing.network.tracker.GenericDataTrackerRegistry;
import dev.lazurite.fpvracing.server.ServerInitializer;
import dev.lazurite.fpvracing.server.item.material.ArmorMaterials;
import dev.lazurite.fpvracing.util.Frequency;
import com.google.common.collect.Multimap;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

/**
 * @author Ethan Johnson
 * @author Patrick Hofmann
 */
public class GogglesItem extends ArmorItem {

	public GogglesItem(Item.Settings settings) {
		super(ArmorMaterials.GOGGLE, EquipmentSlot.HEAD, settings);
	}

	@Override
	public Multimap<EntityAttribute, EntityAttributeModifier> getAttributeModifiers(EquipmentSlot slot) {
		return super.getAttributeModifiers(EquipmentSlot.MAINHAND); // not HEAD
	}

	@Override
	public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
		ItemStack itemStack = user.getStackInHand(hand);
		EquipmentSlot equipmentSlot = MobEntity.getPreferredEquipmentSlot(itemStack);
		ItemStack itemStack2 = user.getEquippedStack(equipmentSlot);

		if (itemStack2.isEmpty()) {
			user.equipStack(equipmentSlot, itemStack.copy());
			itemStack.setCount(0);
			itemStack = new ItemStack(Items.AIR);

			return TypedActionResult.method_29237(itemStack, world.isClient());
		} else {
			return TypedActionResult.fail(itemStack);
		}
	}

	/**
	 * Determines whether or not the client
	 * has a camera entity of type {@link FlyableEntity}.
	 * @return whether or not the player is viewing through a flyable entity
	 */
	@Environment(EnvType.CLIENT)
	public static boolean isInGoggles() {
		return ClientInitializer.client.getCameraEntity() instanceof FlyableEntity;
	}

	/**
	 * Determines whether or not the given {@link ServerPlayerEntity}
	 * has a camera entity of type {@link FlyableEntity}.
	 * @param player the given {@link ServerPlayerEntity}
	 * @return whether or not the camera entity is of type {@link FlyableEntity}
	 */
	public static boolean isInGoggles(ServerPlayerEntity player) {
		return player.getCameraEntity() instanceof FlyableEntity;
	}

	public static void setOn(ItemStack itemStack, boolean on) {
		itemStack.getOrCreateSubTag(ServerInitializer.MODID).putBoolean("on", on);
	}

	public static boolean isWearingGoggles(PlayerEntity player) {
		return player.inventory.armor.get(3).getItem() instanceof GogglesItem;
	}

	public static boolean isOn(PlayerEntity player) {
		if (isWearingGoggles(player)) {
			ItemStack hat = player.inventory.armor.get(3);

			if (hat.getSubTag(ServerInitializer.MODID) != null && hat.getSubTag(ServerInitializer.MODID).contains("on")) {
				return hat.getSubTag(ServerInitializer.MODID).getBoolean("on");
			}
		}

		return false;
	}

	public static boolean isOnSameChannel(FlyableEntity flyable, PlayerEntity player) {
		if (GogglesItem.isWearingGoggles(player)) {
			ItemStack hat = player.inventory.armor.get(3);
			CompoundTag tag = hat.getOrCreateSubTag(ServerInitializer.MODID);
			GenericDataTrackerRegistry.Entry<Frequency> entry = FlyableEntity.FREQUENCY;

			if (entry.getKey().getType().fromTag(tag, entry.getKey().getName()) != null) {
				return flyable.getValue(entry)
						.equals(entry.getKey().getType().fromTag(tag, entry.getKey().getName()));
			}
		}

		return false;
	}

	public static void writeToTag(ItemStack itemStack, PlayerEntity user) {
		Config config = ServerInitializer.SERVER_PLAYER_CONFIGS.get(user.getUuid());
		CompoundTag tag = itemStack.getOrCreateSubTag(ServerInitializer.MODID);
		GenericDataTrackerRegistry.Entry<Frequency> entry = FlyableEntity.FREQUENCY;

		if (entry.getKey().getType().fromTag(tag, entry.getKey().getName()).getFrequency() == -1) {
			Frequency freq = entry.getKey().getType().fromConfig(config, entry.getKey().getName());
			entry.getKey().getType().toTag(tag, entry.getKey().getName(), freq);
		}
	}
}

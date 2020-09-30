package bluevista.fpvracing.server.items;

import bluevista.fpvracing.config.Config;
import bluevista.fpvracing.server.ServerInitializer;
import bluevista.fpvracing.server.entities.DroneEntity;
import bluevista.fpvracing.server.items.materials.ArmorMaterials;
import com.google.common.collect.Multimap;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

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

	public static void setTagValue(ItemStack itemStack, String key, Number value) {
		if (value != null) {
			switch (key) {
				case Config.BAND:
					itemStack.getOrCreateSubTag(ServerInitializer.MODID).putInt(Config.BAND, value.intValue());
					break;
				case Config.CHANNEL:
					itemStack.getOrCreateSubTag(ServerInitializer.MODID).putInt(Config.CHANNEL, value.intValue());
					break;
				default:
					break;
			}

		} else {
			setTagValue(itemStack, key, 0);
		}
	}

	public static Number getTagValue(ItemStack itemStack, String key) {
		if (itemStack.getSubTag(ServerInitializer.MODID) != null && itemStack.getSubTag(ServerInitializer.MODID).contains(key)) {
			CompoundTag tag = itemStack.getSubTag(ServerInitializer.MODID);
			switch (key) {
				case Config.BAND:
					return tag.getInt(Config.BAND);
				case Config.CHANNEL:
					return tag.getInt(Config.CHANNEL);
				default:
					return null;
			}
		}

		return null;
	}

	public static void setOn(ItemStack itemStack, boolean on) {
		itemStack.getOrCreateSubTag(ServerInitializer.MODID).putBoolean(Config.ON, on);
	}

	public static boolean isOn(PlayerEntity player) {
		if (GogglesItem.isWearingGoggles(player)) {
			ItemStack hat = player.inventory.armor.get(3);

			if (hat.getSubTag(ServerInitializer.MODID) != null && hat.getSubTag(ServerInitializer.MODID).contains(Config.ON)) {
				return hat.getSubTag(ServerInitializer.MODID).getBoolean(Config.ON);
			}
		}
		return false;
	}

	public static boolean isWearingGoggles(PlayerEntity player) {
		return player.inventory.armor.get(3).getItem() instanceof GogglesItem;
	}

	public static boolean isOnSameChannel(DroneEntity drone, PlayerEntity player) {
		if (GogglesItem.isWearingGoggles(player)) {
			ItemStack hat = player.inventory.armor.get(3);

			if (GogglesItem.getTagValue(hat, Config.BAND) != null && GogglesItem.getTagValue(hat, Config.CHANNEL) != null) {
				return drone.getConfigValues(Config.BAND).equals(GogglesItem.getTagValue(hat, Config.BAND)) &&
						drone.getConfigValues(Config.CHANNEL).equals(GogglesItem.getTagValue(hat, Config.CHANNEL));
			}
		}
		return false;
	}

	public static void prepGogglesItem(PlayerEntity user, ItemStack itemStack) {
		Config config = ServerInitializer.SERVER_PLAYER_CONFIGS.get(user.getUuid());

		String[] keys = {Config.BAND, Config.CHANNEL};

		for (String key : keys) {
			if (GogglesItem.getTagValue(itemStack, key) == null) {
				GogglesItem.setTagValue(itemStack, key, config.getOption(key));
			}
		}
	}
}

package bluevista.fpvracingmod.server.items;

import bluevista.fpvracingmod.config.Config;
import bluevista.fpvracingmod.server.entities.DroneEntity;
import bluevista.fpvracingmod.server.items.materials.ArmorMaterials;
import com.google.common.collect.Multimap;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.*;
import net.minecraft.text.LiteralText;
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

//			Packet<?> packet = ((NetworkSyncedItem)itemStack.getItem()).createSyncPacket(itemStack, world, user);
//			ServerSidePacketRegistry.INSTANCE.sendToPlayer(user, packet);

			return TypedActionResult.method_29237(itemStack, world.isClient());
		} else {
			return TypedActionResult.fail(itemStack);
		}
	}

	public static void setValue(ItemStack stack, String key, Number value) {
		switch (key) {
			case Config.BAND:
				setBand(stack, value.intValue());
				break;
			case Config.CHANNEL:
				setChannel(stack, value.intValue());
				break;
			default:
				break;
		}
	}

	public static int getValue(ItemStack stack, String key) {
		switch (key) {
			case Config.BAND:
				return getBand(stack);
			case Config.CHANNEL:
				return getChannel(stack);
			default:
				return 0; // unknown key, default value
		}
	}

	public static void setBand(ItemStack itemStack, int band) {
		itemStack.getOrCreateSubTag(Config.FREQUENCY).putInt(Config.BAND, band);
	}

	public static int getBand(ItemStack itemStack) {
		if(itemStack.getSubTag(Config.FREQUENCY) != null)
			return itemStack.getSubTag(Config.FREQUENCY).getInt(Config.BAND);
		return 0;
	}

	public static void setChannel(ItemStack itemStack, int channel) {
		itemStack.getOrCreateSubTag(Config.FREQUENCY).putInt(Config.CHANNEL, channel);
	}

	public static int getChannel(ItemStack itemStack) {
		if(itemStack.getSubTag(Config.FREQUENCY) != null)
			return itemStack.getSubTag(Config.FREQUENCY).getInt(Config.CHANNEL);
		return 0;
	}

	public static void setOn(ItemStack itemStack, boolean on, PlayerEntity player, String[] keys) {
		if(itemStack.getSubTag(Config.MISC) != null && itemStack.getSubTag(Config.MISC).contains(Config.ON)) {
			if(on && !itemStack.getSubTag(Config.MISC).getBoolean(Config.ON)) {

				String subString = keys[0] + " or " + keys[1];

				player.sendMessage(new LiteralText("Press " + subString + " power off goggles"), true);
			}
		}
		itemStack.getOrCreateSubTag(Config.MISC).putBoolean(Config.ON, on);
	}

	public static boolean isOn(PlayerEntity player) {
		if(GogglesItem.isWearingGoggles(player)) {
			ItemStack hat = player.inventory.armor.get(3);
			if (hat.getSubTag(Config.MISC) != null && hat.getSubTag(Config.MISC).contains(Config.ON))
				return hat.getSubTag(Config.MISC).getBoolean(Config.ON);
		}
		return false;
	}

	public static boolean isWearingGoggles(PlayerEntity player) {
		return player.inventory.armor.get(3).getItem() instanceof GogglesItem;
	}

	public static boolean isOnRightChannel(DroneEntity drone, PlayerEntity player) {
		if (GogglesItem.isWearingGoggles(player)) {
			ItemStack hat = player.inventory.armor.get(3);
			if(GogglesItem.isOn(player)) {
				return drone.getBand() == GogglesItem.getBand(hat) && drone.getChannel() == GogglesItem.getChannel(hat);
			}
		}
		return false;
	}
}

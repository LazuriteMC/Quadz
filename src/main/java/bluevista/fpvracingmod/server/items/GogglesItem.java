package bluevista.fpvracingmod.server.items;

import bluevista.fpvracingmod.server.entities.DroneEntity;
import bluevista.fpvracingmod.server.items.materials.ArmorMaterials;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.TranslatableText;

public class GogglesItem extends ArmorItem {
	public GogglesItem(Item.Settings settings) {
		super(ArmorMaterials.GOGGLE, EquipmentSlot.HEAD, settings);
	}

	public static void setBand(ItemStack itemStack, int band, PlayerEntity player) {
		itemStack.getOrCreateSubTag("frequency").putInt("band", band);
	}

	public static void setChannel(ItemStack itemStack, int channel, PlayerEntity player) {
		itemStack.getOrCreateSubTag("frequency").putInt("channel", channel);
	}

	public static void setOn(ItemStack itemStack, boolean on, PlayerEntity player) {
		if(itemStack.getSubTag("misc") != null)
			if(on && !itemStack.getSubTag("misc").getBoolean("on"))
				player.sendMessage(new TranslatableText("Press SHIFT to power off goggles"), true);
		itemStack.getOrCreateSubTag("misc").putBoolean("on", on);
	}

	public static int getBand(ItemStack itemStack) {
		if(itemStack.getSubTag("frequency") != null)
			return itemStack.getSubTag("frequency").getInt("band");
		return 0;
	}

	public static int getChannel(ItemStack itemStack) {
		if(itemStack.getSubTag("frequency") != null)
			return itemStack.getSubTag("frequency").getInt("channel");
		return 0;
	}

	public static boolean isOn(PlayerEntity player) {
		if(GogglesItem.isWearingGoggles(player)) {
			ItemStack hat = player.inventory.armor.get(3);
			if (hat.getSubTag("misc") != null)
				return hat.getSubTag("misc").getBoolean("on");
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

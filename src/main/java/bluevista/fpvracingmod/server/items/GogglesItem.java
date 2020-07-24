package bluevista.fpvracingmod.server.items;

import bluevista.fpvracingmod.network.GogglesInfoS2C;
import bluevista.fpvracingmod.server.entities.DroneEntity;
import bluevista.fpvracingmod.server.items.materials.ArmorMaterials;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public class GogglesItem extends ArmorItem {
	public GogglesItem(Item.Settings settings) {
		super(ArmorMaterials.GOGGLE, EquipmentSlot.HEAD, settings);
	}

	public static void setBand(ItemStack itemStack, int band, PlayerEntity player) {
		itemStack.getOrCreateSubTag("frequency").putInt("band", band);

		if(!player.getEntityWorld().isClient())
			GogglesInfoS2C.send(itemStack, player);
	}

	public static void setChannel(ItemStack itemStack, int channel, PlayerEntity player) {
		itemStack.getOrCreateSubTag("frequency").putInt("channel", channel);

		if(!player.getEntityWorld().isClient())
			GogglesInfoS2C.send(itemStack, player);
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

	public static boolean isWearingGoggles(PlayerEntity player) {
		return player.inventory.armor.get(3).getItem() instanceof GogglesItem;
	}

	public static boolean isOnRightChannel(DroneEntity drone, PlayerEntity player) {
		if (GogglesItem.isWearingGoggles(player)) {
			ItemStack stack = player.inventory.armor.get(3);
			return drone.getBand() == GogglesItem.getBand(stack) && drone.getChannel() == GogglesItem.getChannel(stack);
		}
		return false;
	}
}

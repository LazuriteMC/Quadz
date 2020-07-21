package bluevista.fpvracingmod.server.items;

import bluevista.fpvracingmod.server.items.materials.ArmorMaterials;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public class GogglesItem extends ArmorItem {
	public GogglesItem(Item.Settings settings) {
		super(ArmorMaterials.GOGGLE, EquipmentSlot.HEAD, settings);
	}

	public static void setBand(ItemStack itemStack, int band) {
		itemStack.getOrCreateSubTag("frequency").putInt("band", band);
	}

	public static void setChannel(ItemStack itemStack, int channel) {
		itemStack.getOrCreateSubTag("frequency").putInt("channel", channel);
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
}

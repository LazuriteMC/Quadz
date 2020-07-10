package bluevista.fpvracingmod.server.items;

import bluevista.fpvracingmod.server.items.materials.ArmorMaterials;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.Item;

public class GogglesItem extends ArmorItem {
	public static int band;
	public static int channel;

	public GogglesItem(Item.Settings settings) {
		super(ArmorMaterials.GOGGLE, EquipmentSlot.HEAD, settings);
	}

	public static void setBand(int band) {
		GogglesItem.band = band;
	}

	public static int getBand() {
		return band;
	}

	public static void setChannel(int channel) {
		GogglesItem.channel = channel;
	}

	public static int getChannel() {
		return channel;
	}
}


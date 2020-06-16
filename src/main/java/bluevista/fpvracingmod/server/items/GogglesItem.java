package bluevista.fpvracingmod.server.items;

import bluevista.fpvracingmod.server.items.armor.FPVArmorMaterials;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.Item;

public class GogglesItem extends ArmorItem {
	public GogglesItem(Item.Settings settings) {
		super(FPVArmorMaterials.GOGGLE, EquipmentSlot.HEAD, settings);
	}
}


package dev.lazurite.fpvracing.common.item;

import dev.lazurite.fpvracing.common.item.material.ArmorMaterials;
import com.google.common.collect.Multimap;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.*;
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

			return TypedActionResult.success(itemStack, world.isClient());
		} else {
			return TypedActionResult.fail(itemStack);
		}
	}
}

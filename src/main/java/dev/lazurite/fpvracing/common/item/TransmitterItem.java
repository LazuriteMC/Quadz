package dev.lazurite.fpvracing.common.item;

import dev.lazurite.fpvracing.FPVRacing;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;

public class TransmitterItem extends Item {
    public TransmitterItem(Settings settings) {
        super(settings);
    }

    public static FlyableEntity flyableEntityFromTransmitter(ItemStack itemStack, PlayerEntity player) {
        if (itemStack.getItem() instanceof TransmitterItem) {
            CompoundTag tag = itemStack.getOrCreateSubTag(FPVRacing.MODID);
            GenericType<?> type = FlyableEntity.BIND_ID.getKey().getType();
            String name = FlyableEntity.BIND_ID.getKey().getName();

            if (type.fromTag(tag, name) != null) {
                List<FlyableEntity> entities = FlyableEntity.getList(player, FlyableEntity.class, 500);

                for (FlyableEntity entity : entities) {
                    if (entity.getValue(FlyableEntity.BIND_ID).equals(type.fromTag(tag, name))) {
                        return entity;
                    }
                }
            }
        }

        return null;
    }

    public static boolean isBoundTransmitter(ItemStack itemStack, FlyableEntity flyable) {
        CompoundTag tag = itemStack.getOrCreateSubTag(FPVRacing.MODID);
        GenericType<?> type = FlyableEntity.BIND_ID.getKey().getType();
        String name = FlyableEntity.BIND_ID.getKey().getName();

        if (type.fromTag(tag, name) != null) {
            return flyable.getValue(FlyableEntity.BIND_ID).equals(type.fromTag(tag, name));
        }

        return false;
    }
}


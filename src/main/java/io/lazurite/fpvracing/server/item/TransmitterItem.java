package io.lazurite.fpvracing.server.item;

import io.lazurite.fpvracing.network.tracker.GenericDataTrackerRegistry;
import io.lazurite.fpvracing.server.ServerInitializer;
import io.lazurite.fpvracing.server.entity.FlyableEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;

import java.util.List;

/**
 * @author Ethan Johnson
 * @author Patrick Hofmann
 */
public class TransmitterItem extends Item {

    public TransmitterItem(Settings settings) {
        super(settings);
    }

    public static FlyableEntity flyableEntityFromTransmitter(ItemStack itemStack, PlayerEntity player) {
        if (itemStack.getItem() instanceof TransmitterItem) {
            CompoundTag tag = itemStack.getOrCreateSubTag(ServerInitializer.MODID);
            GenericDataTrackerRegistry.Entry<Integer> entry = FlyableEntity.BIND_ID;

            if (entry.getKey().getType().fromTag(tag, entry.getKey().getName()) != null) {
                List<FlyableEntity> entities = FlyableEntity.getList(player, 500);

                for (FlyableEntity entity : entities) {
                    if (entity.getValue(entry)
                            .equals(entry.getKey().getType().fromTag(tag, entry.getKey().getName()))) {
                        return entity;
                    }
                }
            }
        }

        return null;
    }

    public static boolean isBoundTransmitter(ItemStack itemStack, FlyableEntity flyable) {
        CompoundTag tag = itemStack.getOrCreateSubTag(ServerInitializer.MODID);
        GenericDataTrackerRegistry.Entry<Integer> entry = FlyableEntity.BIND_ID;

        if (entry.getKey().getType().fromTag(tag, entry.getKey().getName()) != null) {
            return flyable.getValue(entry)
                    .equals(entry.getKey().getType().fromTag(tag, entry.getKey().getName()));
        }

        return false;
    }
}


package bluevista.fpvracing.server.items;

import bluevista.fpvracing.config.Config;
import bluevista.fpvracing.server.ServerInitializer;
import bluevista.fpvracing.server.entities.FlyableEntity;
import bluevista.fpvracing.server.entities.QuadcopterEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.server.world.ServerWorld;

import java.util.List;

public class TransmitterItem extends Item {

    public TransmitterItem(Settings settings) {
        super(settings);
    }

    public static void setTagValue(ItemStack itemStack, String key, Number value) {
        if (value != null) {
            switch (key) {
                case Config.BIND:
                    itemStack.getOrCreateSubTag(ServerInitializer.MODID).putInt(Config.BIND, value.intValue());
                    break;
                default:
                    break;
            }
        }
    }

    public static Number getTagValue(ItemStack itemStack, String key) {
        if (itemStack.getSubTag(ServerInitializer.MODID) != null && itemStack.getSubTag(ServerInitializer.MODID).contains(key)) {
            CompoundTag tag = itemStack.getSubTag(ServerInitializer.MODID);
            switch (key) {
                case Config.BIND:
                    return tag.getInt(Config.BIND);
                default:
                    return null;
            }
        }

        return null;
    }

    public static FlyableEntity flyableFromTransmitter(ItemStack itemStack, PlayerEntity player) {
        if (itemStack.getItem() instanceof TransmitterItem) {
            if (TransmitterItem.getTagValue(itemStack, Config.BIND) != null) {
                List<FlyableEntity> entities = FlyableEntity.getList(player, 500);

                for (FlyableEntity entity : entities) {
                    if (entity.getConfigValues(Config.BIND).equals(getTagValue(itemStack, Config.BIND))) {
                        return entity;
                    }
                }
            }
        }

        return null;
    }

    public static boolean isBoundTransmitter(ItemStack itemStack, FlyableEntity flyable) {
        if (TransmitterItem.getTagValue(itemStack, Config.BIND) != null) {
            return flyable.getConfigValues(Config.BIND).equals(TransmitterItem.getTagValue(itemStack, Config.BIND));
        }

        return false;
    }
}


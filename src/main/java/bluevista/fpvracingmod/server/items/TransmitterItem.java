package bluevista.fpvracingmod.server.items;

import bluevista.fpvracingmod.config.Config;
import bluevista.fpvracingmod.server.ServerInitializer;
import bluevista.fpvracingmod.server.entities.DroneEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;

import java.util.UUID;

public class TransmitterItem extends Item {

    public TransmitterItem(Settings settings) {
        super(settings);
    }

    public static void setTagValue(ItemStack itemStack, String key, UUID value) {
        if (value != null) {
            switch (key) {
                case Config.BIND:
                    itemStack.getOrCreateSubTag(ServerInitializer.MODID).putUuid(Config.BIND, value);
                    break;
                default:
                    break;
            }
        }
    }

    public static UUID getTagValue(ItemStack itemStack, String key) {
        if (itemStack.getSubTag(ServerInitializer.MODID) != null && itemStack.getSubTag(ServerInitializer.MODID).contains(key)) {
            CompoundTag tag = itemStack.getSubTag(ServerInitializer.MODID);
            switch (key) {
                case Config.BIND:
                    return tag.getUuid(Config.BIND);
                default:
                    return null;
            }
        }

        return null;
    }

    public static DroneEntity droneFromTransmitter(ItemStack itemStack, PlayerEntity player) {
        DroneEntity drone = null;

        if (itemStack.getItem() instanceof TransmitterItem) {
            if (TransmitterItem.getTagValue(itemStack, Config.BIND) != null) {
                drone = DroneEntity.getByUuid(player, TransmitterItem.getTagValue(itemStack, Config.BIND));
            }
        }

        return drone;
    }

    public static boolean isBoundTransmitter(ItemStack itemStack, DroneEntity drone) {
        if (TransmitterItem.getTagValue(itemStack, Config.BIND) != null) {
            return drone.getUuid().equals(TransmitterItem.getTagValue(itemStack, Config.BIND));
        }
        return false;
    }

    public static boolean isHoldingTransmitter(PlayerEntity player) {
        return player.inventory.getMainHandStack().getItem() instanceof TransmitterItem;
    }
}


package bluevista.fpvracing.server.items;

import bluevista.fpvracing.network.datatracker.FlyableTrackerRegistry;
import bluevista.fpvracing.server.ServerInitializer;
import bluevista.fpvracing.server.entities.FlyableEntity;
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
            FlyableTrackerRegistry.Entry<Integer> entry = FlyableTrackerRegistry.BIND_ID;

            if (entry.getDataType().fromTag(tag, entry.getName()) != null) {
                List<FlyableEntity> entities = FlyableEntity.getList(player, 500);

                for (FlyableEntity entity : entities) {
                    if (entity.getValue(entry)
                            .equals(entry.getDataType().fromTag(tag, entry.getName()))) {
                        return entity;
                    }
                }
            }
        }

        return null;
    }

    public static boolean isBoundTransmitter(ItemStack itemStack, FlyableEntity flyable) {
        CompoundTag tag = itemStack.getOrCreateSubTag(ServerInitializer.MODID);
        FlyableTrackerRegistry.Entry<Integer> entry = FlyableTrackerRegistry.BIND_ID;

        if (entry.getDataType().fromTag(tag, entry.getName()) != null) {
            return flyable.getValue(entry)
                    .equals(entry.getDataType().fromTag(tag, entry.getName()));
        }

        return false;
    }
}


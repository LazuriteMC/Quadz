package bluevista.fpvracingmod.server.items;

import bluevista.fpvracingmod.server.entities.DroneEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public class TransmitterItem extends Item {
    public TransmitterItem(Settings settings) {
        super(settings);
    }

    public static DroneEntity droneFromTransmitter(ItemStack stack, PlayerEntity player) {
        DroneEntity drone = null;

        if (stack.getItem() instanceof TransmitterItem)
            if (stack.getSubTag("bind") != null)
                drone = DroneEntity.getByUuid(player, stack.getSubTag("bind").getUuid("bind"));
        return drone;
    }

    public static boolean isBoundTransmitter(ItemStack item, DroneEntity drone) {
        try {
            if (item.getSubTag("bind") != null)
                return (drone.getUuid().equals(item.getSubTag("bind").getUuid("bind")));
            else return false;
        } catch (Exception e) {
            return false;
        }
    }

    public static boolean isHoldingTransmitter(PlayerEntity player) {
        return player.inventory.getMainHandStack().getItem() instanceof TransmitterItem;
    }
}


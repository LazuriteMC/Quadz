package bluevista.fpvracing.mixin;

import bluevista.fpvracing.config.Config;
import bluevista.fpvracing.server.entities.DroneEntity;
import bluevista.fpvracing.server.items.DroneSpawnerItem;
import bluevista.fpvracing.server.items.GogglesItem;
import bluevista.fpvracing.server.items.TransmitterItem;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerInventory.class)
public class PlayerInventoryMixin {
    @Shadow @Final PlayerEntity player;

    // used when picking a stack off the ground (including /give)
    @Inject(at = @At("HEAD"), method = "addStack(ILnet/minecraft/item/ItemStack;)I")
    private void addStack(int slot, ItemStack itemStack, CallbackInfoReturnable info) {
        if (itemStack.getItem() instanceof GogglesItem && !player.world.isClient()) {
            GogglesItem.prepGogglesItem(player, itemStack);
        }

        if (itemStack.getItem() instanceof DroneSpawnerItem && !player.world.isClient()) {
            DroneSpawnerItem.prepDroneSpawnerItem(player, itemStack);
        }

        if (itemStack.getItem() instanceof TransmitterItem && !player.world.isClient()) {
            DroneEntity drone = TransmitterItem.droneFromTransmitter(itemStack, player);

            if (drone != null) {
                drone.setConfigValues(Config.PLAYER_ID, player.getEntityId());
            }
        }
    }

    // used when adding a stack to your inventory from the creative tab
    @Inject(at = @At("HEAD"), method = "setStack(ILnet/minecraft/item/ItemStack;)V")
    public void setStack(int slot, ItemStack itemStack, CallbackInfo info) {
        if (itemStack.getItem() instanceof GogglesItem && !player.world.isClient) {
            GogglesItem.prepGogglesItem(player, itemStack);
        }

        if (itemStack.getItem() instanceof DroneSpawnerItem && !player.world.isClient) {
            DroneSpawnerItem.prepDroneSpawnerItem(player, itemStack);
        }
    }
}

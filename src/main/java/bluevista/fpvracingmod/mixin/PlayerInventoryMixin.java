package bluevista.fpvracingmod.mixin;

import bluevista.fpvracingmod.server.items.DroneSpawnerItem;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerInventory.class)
public class PlayerInventoryMixin {

    @Shadow @Final PlayerEntity player;

    // used when picking a stack off the ground
//    @Inject(at = @At("HEAD"), method = "addStack(ILnet/minecraft/item/ItemStack;)I")
//    private void addStack(int slot, ItemStack itemStack, CallbackInfoReturnable info) {
//        if (itemStack.getItem() instanceof DroneSpawnerItem) {
//        }
//    }

    @Inject(at = @At("HEAD"), method = "setStack(ILnet/minecraft/item/ItemStack;)V")
    public void setStack(int slot, ItemStack itemStack, CallbackInfo info) {
        if (itemStack.getItem() instanceof DroneSpawnerItem && !player.world.isClient) {
            DroneSpawnerItem.prepDroneSpawnerItem(player, itemStack);
        }
    }
}

package dev.lazurite.fpvracing.mixin;

import dev.lazurite.fpvracing.server.entity.FlyableEntity;
import dev.lazurite.fpvracing.server.item.GogglesItem;
import dev.lazurite.fpvracing.server.item.QuadcopterItem;
import dev.lazurite.fpvracing.server.item.TransmitterItem;
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
    private void addStack(int slot, ItemStack itemStack, CallbackInfoReturnable<Integer> info) {
        if (!player.world.isClient()) {
            if (itemStack.getItem() instanceof GogglesItem) {
                GogglesItem.writeToTag(itemStack, player);
            }

            if (itemStack.getItem() instanceof QuadcopterItem) {
                QuadcopterItem.writeToTag(itemStack, player);
            }

            if (itemStack.getItem() instanceof TransmitterItem) {
                FlyableEntity flyable = TransmitterItem.flyableEntityFromTransmitter(itemStack, player);

                if (flyable != null) {
                    flyable.setValue(FlyableEntity.PLAYER_ID, player.getEntityId());
                }
            }
        }
    }

    // used when adding a stack to your inventory from the creative tab
    @Inject(at = @At("HEAD"), method = "setStack(ILnet/minecraft/item/ItemStack;)V")
    public void setStack(int slot, ItemStack itemStack, CallbackInfo info) {
        if (!player.world.isClient()) {
            if (itemStack.getItem() instanceof GogglesItem) {
                GogglesItem.writeToTag(itemStack, player);
            }

            if (itemStack.getItem() instanceof QuadcopterItem) {
                QuadcopterItem.writeToTag(itemStack, player);
            }
        }
    }
}

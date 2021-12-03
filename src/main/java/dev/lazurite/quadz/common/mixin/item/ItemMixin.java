package dev.lazurite.quadz.common.mixin.item;

import dev.lazurite.quadz.common.item.QuadcopterItem;
import dev.lazurite.quadz.common.state.QuadcopterState;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Item.class)
public class ItemMixin {
    @Inject(method = "getDescriptionId(Lnet/minecraft/world/item/ItemStack;)Ljava/lang/String;", at = @At("RETURN"), cancellable = true)
    public void getDescriptionId_RETURN(ItemStack itemStack, CallbackInfoReturnable<String> info) {
        if ((Item) (Object) this instanceof QuadcopterItem) {
            QuadcopterState.fromStack(itemStack).ifPresent(state ->
                info.setReturnValue("template.quadz." + state.getTemplate()));
        }
    }
}

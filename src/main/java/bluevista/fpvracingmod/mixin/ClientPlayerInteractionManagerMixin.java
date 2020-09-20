package bluevista.fpvracingmod.mixin;

import bluevista.fpvracingmod.server.items.DroneSpawnerItem;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ClientPlayerInteractionManager.class)
public class ClientPlayerInteractionManagerMixin {
    @Redirect(
            method = "interactBlock",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/item/ItemStack;setCount(I)V"
            )
    )
    public void setCount(ItemStack itemStack, int count) {
        int realCount = itemStack.getCount();

        if(realCount < 1) {
            itemStack.setCount(1);
        }

        if(itemStack.getItem() instanceof DroneSpawnerItem) {
            System.out.println("SETTINGS: " + realCount);
            itemStack.setCount(realCount);
        } else {
            itemStack.setCount(count);
        }
    }
}

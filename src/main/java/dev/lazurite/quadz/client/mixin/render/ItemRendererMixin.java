package dev.lazurite.quadz.client.mixin.render;

import dev.lazurite.quadz.Quadz;
import net.minecraft.client.render.item.ItemModels;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.util.ModelIdentifier;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemRenderer.class)
public class ItemRendererMixin {
    @Shadow @Final private ItemModels models;

    @Inject(method = "getHeldItemModel", at = @At("HEAD"), cancellable = true)
    public void getHeldItemModel(ItemStack stack, @Nullable World world, @Nullable LivingEntity entity, CallbackInfoReturnable<BakedModel> info) {
//        if (stack.getItem().equals(Quadz.QUADCOPTER_ITEM)) {
//            info.setReturnValue(models.getModelManager().getModel(new ModelIdentifier("quadz:goggles_item#inventory")));
//        }
    }
}

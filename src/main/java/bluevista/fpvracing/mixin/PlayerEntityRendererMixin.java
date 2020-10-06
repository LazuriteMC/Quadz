package bluevista.fpvracing.mixin;

import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerEntityRenderer.class)
public abstract class PlayerEntityRendererMixin extends LivingEntityRenderer<AbstractClientPlayerEntity, PlayerEntityModel<AbstractClientPlayerEntity>> {
    public PlayerEntityRendererMixin(EntityRenderDispatcher dispatcher, PlayerEntityModel<AbstractClientPlayerEntity> model, float shadowRadius) {
        super(dispatcher, model, shadowRadius);
    }

//    @Inject(at = @At("HEAD"), method = "setModelPose", cancellable = true)
//    private void setModelPose(AbstractClientPlayerEntity abstractClientPlayerEntity, CallbackInfo info) {
//        PlayerEntityModel<AbstractClientPlayerEntity> playerEntityModel = (PlayerEntityModel) this.getModel();
//
//        if (ClientTick.isFlyingClientSideDrone(ClientInitializer.client) && abstractClientPlayerEntity == ClientInitializer.client.player) {
//            playerEntityModel.head.visible = true;
//            playerEntityModel.helmet.visible = true;
//            playerEntityModel.leftArm.visible = false;
//            playerEntityModel.rightArm.visible = false;
//            playerEntityModel.leftLeg.visible = false;
//            playerEntityModel.rightLeg.visible = false;
//            playerEntityModel.torso.visible = false;
//            info.cancel();
//        }
//    }
}

package dev.lazurite.chonker.mixin.client;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(MinecraftClient.class)
public abstract class MinecraftClientMixin {

    @Shadow @Nullable public abstract Entity getCameraEntity();
    @Shadow @Nullable public ClientPlayerEntity player;

    @Unique
    private double getSafeCameraEntityPosX() {
        return this.getCameraEntity() != null ? this.getCameraEntity().getX() : this.player.getX();
    }

    @Unique
    private double getSafeCameraEntityPosY() {
        return this.getCameraEntity() != null ? this.getCameraEntity().getY() : this.player.getY();
    }

    @Unique
    private double getSafeCameraEntityPosZ() {
        return this.getCameraEntity() != null ? this.getCameraEntity().getZ() : this.player.getZ();
    }

    @Redirect(
            method = "tick",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/network/ClientPlayerEntity;getX()D"
            )
    )
    public double tick_getX(ClientPlayerEntity clientPlayerEntity) {
        return this.getSafeCameraEntityPosX();
    }

    @Redirect(
            method = "tick",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/network/ClientPlayerEntity;getY()D"
            )
    )
    public double tick_getY(ClientPlayerEntity clientPlayerEntity) {
        return this.getSafeCameraEntityPosY();
    }

    @Redirect(
            method = "tick",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/network/ClientPlayerEntity;getZ()D"
            )
    )
    public double tick_getZ(ClientPlayerEntity clientPlayerEntity) {
        return this.getSafeCameraEntityPosZ();
    }

}

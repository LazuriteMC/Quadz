package dev.lazurite.chonker.mixin.client;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.resource.SynchronousResourceReloadListener;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.*;

@Mixin(WorldRenderer.class)
public abstract class WorldRendererMixin implements SynchronousResourceReloadListener, AutoCloseable {

    @Shadow @Final private MinecraftClient client;

    @Unique
    private double getSafeCameraEntityPosX() {
        return this.client.getCameraEntity() != null ? this.client.getCameraEntity().getX() : this.client.player.getX();
    }

    @Unique
    private double getSafeCameraEntityPosY() {
        return this.client.getCameraEntity() != null ? this.client.getCameraEntity().getY() : this.client.player.getY();
    }

    @Unique
    private double getSafeCameraEntityPosZ() {
        return this.client.getCameraEntity() != null ? this.client.getCameraEntity().getZ() : this.client.player.getZ();
    }

    @Unique
    private int getSafeCameraChunkPosX() {
        return this.client.getCameraEntity() != null ? this.client.getCameraEntity().chunkX : this.client.player.chunkX;
    }

    @Unique
    private int getSafeCameraChunkPosY() {
        return this.client.getCameraEntity() != null ? this.client.getCameraEntity().chunkY : this.client.player.chunkY;
    }

    @Unique
    private int getSafeCameraChunkPosZ() {
        return this.client.getCameraEntity() != null ? this.client.getCameraEntity().chunkZ : this.client.player.chunkZ;
    }

    @Redirect(
            method = "setupTerrain",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/network/ClientPlayerEntity;getX()D",
                    ordinal = 0
            )
    )
    private double setupTerrain_getX0(ClientPlayerEntity ignored) {
        return this.getSafeCameraEntityPosX();
    }

    @Redirect(
            method = "setupTerrain",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/network/ClientPlayerEntity;getY()D",
                    ordinal = 0
            )
    )
    private double setupTerrain_getY0(ClientPlayerEntity ignored) {
        return this.getSafeCameraEntityPosY();
    }

    @Redirect(
            method = "setupTerrain",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/network/ClientPlayerEntity;getZ()D",
                    ordinal = 0
            )
    )
    private double setupTerrain_getZ0(ClientPlayerEntity ignored) {
        return this.getSafeCameraEntityPosZ();
    }

    @Redirect(
            method = "setupTerrain",
            at = @At(
                    value = "FIELD",
                    target = "Lnet/minecraft/client/network/ClientPlayerEntity;chunkX:I",
                    ordinal = 0
            )
    )
    private int setupTerrain_chunkX0(ClientPlayerEntity ignored) {
        return this.getSafeCameraChunkPosX();
    }

    @Redirect(
            method = "setupTerrain",
            at = @At(
                    value = "FIELD",
                    target = "Lnet/minecraft/client/network/ClientPlayerEntity;chunkY:I",
                    ordinal = 0
            )
    )
    private int setupTerrain_chunkY0(ClientPlayerEntity ignored) {
        return this.getSafeCameraChunkPosY();
    }

    @Redirect(
            method = "setupTerrain",
            at = @At(
                    value = "FIELD",
                    target = "Lnet/minecraft/client/network/ClientPlayerEntity;chunkZ:I",
                    ordinal = 0
            )
    )
    private int setupTerrain_chunkZ0(ClientPlayerEntity ignored) {
        return this.getSafeCameraChunkPosZ();
    }

    @Redirect(
            method = "setupTerrain",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/network/ClientPlayerEntity;getX()D",
                    ordinal = 1
            )
    )
    private double setupTerrain_getX1(ClientPlayerEntity ignored) {
        return this.getSafeCameraEntityPosX();
    }

    @Redirect(
            method = "setupTerrain",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/network/ClientPlayerEntity;getY()D",
                    ordinal = 1
            )
    )
    private double setupTerrain_getY1(ClientPlayerEntity ignored) {
        return this.getSafeCameraEntityPosY();
    }

    @Redirect(
            method = "setupTerrain",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/network/ClientPlayerEntity;getZ()D",
                    ordinal = 1
            )
    )
    private double setupTerrain_getZ1(ClientPlayerEntity ignored) {
        return this.getSafeCameraEntityPosZ();
    }

    @Redirect(
            method = "setupTerrain",
            at = @At(
                    value = "FIELD",
                    target = "Lnet/minecraft/client/network/ClientPlayerEntity;chunkX:I",
                    ordinal = 1
            )
    )
    private int setupTerrain_chunkX1(ClientPlayerEntity ignored) {
        return this.getSafeCameraChunkPosX();
    }

    @Redirect(
            method = "setupTerrain",
            at = @At(
                    value = "FIELD",
                    target = "Lnet/minecraft/client/network/ClientPlayerEntity;chunkY:I",
                    ordinal = 1
            )
    )
    private int setupTerrain_chunkY1(ClientPlayerEntity ignored) {
        return this.getSafeCameraChunkPosY();
    }

    @Redirect(
            method = "setupTerrain",
            at = @At(
                    value = "FIELD",
                    target = "Lnet/minecraft/client/network/ClientPlayerEntity;chunkZ:I",
                    ordinal = 1
            )
    )
    private int setupTerrain_chunkZ1(ClientPlayerEntity ignored) {
        return this.getSafeCameraChunkPosZ();
    }

    @Redirect(
            method = "setupTerrain",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/network/ClientPlayerEntity;getX()D",
                    ordinal = 2
            )
    )
    private double setupTerrain_getX2(ClientPlayerEntity ignored) {
        return this.getSafeCameraEntityPosX();
    }

    @Redirect(
            method = "setupTerrain",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/network/ClientPlayerEntity;getZ()D",
                    ordinal = 2
            )
    )
    private double setupTerrain_getZ2(ClientPlayerEntity ignored) {
        return this.getSafeCameraEntityPosZ();
    }
}

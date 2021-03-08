package dev.lazurite.chonker.mixin.common;

import com.mojang.authlib.GameProfile;
import dev.lazurite.chonker.common.IServerPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.screen.ScreenHandlerListener;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ServerPlayerEntity.class)
public abstract class ServerPlayerEntityMixin extends PlayerEntity implements ScreenHandlerListener, IServerPlayerEntity {

    @Unique private ChunkSectionPos prevCameraChunkSectionPos = ChunkSectionPos.from(0, 0, 0);

    @Shadow @Final public MinecraftServer server;
    @Shadow public abstract Entity getCameraEntity();

    public ServerPlayerEntityMixin(World world, BlockPos pos, float yaw, GameProfile profile) {
        super(world, pos, yaw, profile);
    }

    @Redirect(
            method = "setCameraEntity",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/network/ServerPlayerEntity;requestTeleport(DDD)V"
            )
    )
    public void requestTeleport(ServerPlayerEntity serverPlayerEntity, double destX, double destY, double destZ) { }

    @Redirect(
            method = "tick",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/network/ServerPlayerEntity;updatePositionAndAngles(DDDFF)V"
            )
    )
    public void updatePositionAndAngles(ServerPlayerEntity serverPlayerEntity, double x, double y, double z, float yaw, float pitch) { }

    @Unique
    @Override
    public void setPrevCameraChunkSectionPos(ChunkSectionPos prevCameraChunkSectionPos) {
        this.prevCameraChunkSectionPos = prevCameraChunkSectionPos;
    }

    @Unique
    @Override
    public ChunkSectionPos getPrevCameraChunkSectionPos() {
        return this.prevCameraChunkSectionPos;
    }
}

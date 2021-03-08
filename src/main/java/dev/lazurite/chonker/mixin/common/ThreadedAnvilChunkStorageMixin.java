package dev.lazurite.chonker.mixin.common;

import com.mojang.datafixers.DataFixer;
import dev.lazurite.chonker.common.IServerPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ChunkHolder;
import net.minecraft.server.world.ThreadedAnvilChunkStorage;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.world.storage.VersionedChunkStorage;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.io.File;

@Mixin(ThreadedAnvilChunkStorage.class)
public abstract class ThreadedAnvilChunkStorageMixin extends VersionedChunkStorage implements ChunkHolder.PlayersWatchingChunkProvider {

    @Unique private ServerPlayerEntity serverPlayerEntity;

    public ThreadedAnvilChunkStorageMixin(File file, DataFixer dataFixer, boolean bl) {
        super(file, dataFixer, bl);
    }

    // region getChebyshevDistance

    @Redirect(
            method = "getChebyshevDistance(Lnet/minecraft/util/math/ChunkPos;Lnet/minecraft/server/network/ServerPlayerEntity;Z)I",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/network/ServerPlayerEntity;getCameraPosition()Lnet/minecraft/util/math/ChunkSectionPos;"
            )
    )
    private static ChunkSectionPos getChebyshevDistance_getCameraPosition(ServerPlayerEntity player) {
        ChunkSectionPos prevCameraChunkSectionPos = ((IServerPlayerEntity) player).getPrevCameraChunkSectionPos();
        return prevCameraChunkSectionPos != null ? prevCameraChunkSectionPos : player.getCameraPosition();
    }

    @Redirect(
            method = "getChebyshevDistance(Lnet/minecraft/util/math/ChunkPos;Lnet/minecraft/server/network/ServerPlayerEntity;Z)I",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/network/ServerPlayerEntity;getX()D"
            )
    )
    private static double getChebyshevDistance_getX(ServerPlayerEntity player) {
        return player.getCameraEntity().getX();
    }

    @Redirect(
            method = "getChebyshevDistance(Lnet/minecraft/util/math/ChunkPos;Lnet/minecraft/server/network/ServerPlayerEntity;Z)I",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/network/ServerPlayerEntity;getZ()D"
            )
    )
    private static double getChebyshevDistance_getZ(ServerPlayerEntity player) {
        return player.getCameraEntity().getZ();
    }

    // endregion getChebyshevDistance

    // region method_20726

    @Inject(
            method = "method_20726",
            at = @At("HEAD")
    )
    private void method_20726_HEAD(ServerPlayerEntity serverPlayerEntity, CallbackInfoReturnable<ChunkSectionPos> cir) {
        this.serverPlayerEntity = serverPlayerEntity;
        ((IServerPlayerEntity) serverPlayerEntity).setPrevCameraChunkSectionPos(ChunkSectionPos.from(serverPlayerEntity.getCameraEntity()));
    }

    @Redirect(
            method = "method_20726",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/util/math/ChunkSectionPos;getSectionX()I"
            )
    )
    private int method_20726_getSectionX(ChunkSectionPos chunkSectionPos) {
        ChunkSectionPos prevCameraChunkSectionPos = ((IServerPlayerEntity) this.serverPlayerEntity).getPrevCameraChunkSectionPos();
        return prevCameraChunkSectionPos != null ? prevCameraChunkSectionPos.getSectionX() : chunkSectionPos.getSectionX();
    }

    @Redirect(
            method = "method_20726",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/util/math/ChunkSectionPos;getSectionZ()I"
            )
    )
    private int method_20726_getSectionZ(ChunkSectionPos chunkSectionPos) {
        ChunkSectionPos prevCameraChunkSectionPos = ((IServerPlayerEntity) this.serverPlayerEntity).getPrevCameraChunkSectionPos();
        return prevCameraChunkSectionPos != null ? prevCameraChunkSectionPos.getSectionZ() : chunkSectionPos.getSectionZ();
    }

    // endregion method_20726

    // region handlePlayerAddedOrRemoved

    @Inject(
            method = "handlePlayerAddedOrRemoved",
            at = @At("HEAD")
    )
    void handlePlayerAddedOrRemoved_HEAD(ServerPlayerEntity player, boolean added, CallbackInfo ci) {
        this.serverPlayerEntity = player;
    }

    @ModifyVariable(
            method = "handlePlayerAddedOrRemoved",
            at = @At(
                    value = "JUMP",
                    opcode = Opcodes.IFEQ
            ),
            ordinal = 0
    )
    int handlePlayerAddedOrRemoved_JUMP0(int i) {
        return ChunkSectionPos.from(this.serverPlayerEntity.getCameraEntity()).getSectionX();
    }

    @ModifyVariable(
            method = "handlePlayerAddedOrRemoved",
            at = @At(
                    value = "JUMP",
                    opcode = Opcodes.IFEQ
            ),
            ordinal = 1
    )
    int handlePlayerAddedOrRemoved_JUMP1(int j) {
        return ChunkSectionPos.from(this.serverPlayerEntity.getCameraEntity()).getSectionZ();
    }

    @ModifyArg(
            method = "handlePlayerAddedOrRemoved",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/util/math/ChunkSectionPos;from(Lnet/minecraft/entity/Entity;)Lnet/minecraft/util/math/ChunkSectionPos;"
            )
    )
    Entity handlePlayerAddedOrRemoved_from(Entity entity) {
        return ((ServerPlayerEntity) entity).getCameraEntity();
    }

    @Redirect(
            method = "handlePlayerAddedOrRemoved",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/network/ServerPlayerEntity;getCameraPosition()Lnet/minecraft/util/math/ChunkSectionPos;"
            )
    )
    ChunkSectionPos handlePlayerAddedOrRemoved_getCameraPosition(ServerPlayerEntity playerEntity) {
        ChunkSectionPos prevCameraChunkSectionPos = ((IServerPlayerEntity) playerEntity).getPrevCameraChunkSectionPos();
        return prevCameraChunkSectionPos != null ? prevCameraChunkSectionPos : playerEntity.getCameraPosition();
    }

    // endregion handlePlayerAddedOrRemoved

    // region updateCameraPosition

    @Inject(
            method = "updateCameraPosition",
            at = @At("HEAD")
    )
    public void updateCameraPosition_HEAD(ServerPlayerEntity player, CallbackInfo ci) {
        this.serverPlayerEntity = player;
    }

    @ModifyVariable(
            method = "updateCameraPosition",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/network/ServerPlayerEntity;getCameraPosition()Lnet/minecraft/util/math/ChunkSectionPos;"
            ),
            ordinal = 0
    )
    public int updateCameraPosition_getCameraPosition0(int i) {
        return ChunkSectionPos.from(this.serverPlayerEntity.getCameraEntity()).getSectionX();
    }

    @ModifyVariable(
            method = "updateCameraPosition",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/network/ServerPlayerEntity;getCameraPosition()Lnet/minecraft/util/math/ChunkSectionPos;"
            ),
            ordinal = 1
    )
    public int updateCameraPosition_getCameraPosition1(int j) {
        return ChunkSectionPos.from(this.serverPlayerEntity.getCameraEntity()).getSectionZ();
    }

    @Redirect(
            method = "updateCameraPosition",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/network/ServerPlayerEntity;getCameraPosition()Lnet/minecraft/util/math/ChunkSectionPos;"
            )
    )
    public ChunkSectionPos updateCameraPosition_getCameraPosition(ServerPlayerEntity player) {
        ChunkSectionPos prevCameraChunkSectionPos = ((IServerPlayerEntity) player).getPrevCameraChunkSectionPos();
        return prevCameraChunkSectionPos != null ? prevCameraChunkSectionPos : player.getCameraPosition();
    }

    @ModifyArg(
            method = "updateCameraPosition",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/util/math/ChunkSectionPos;from(Lnet/minecraft/entity/Entity;)Lnet/minecraft/util/math/ChunkSectionPos;"
            )
    )
    public Entity updateCameraPosition_from(Entity entity) {
        return ((ServerPlayerEntity) entity).getCameraEntity();
    }

    // endregion updateCameraPosition
}

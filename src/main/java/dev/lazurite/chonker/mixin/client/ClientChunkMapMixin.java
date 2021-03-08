package dev.lazurite.chonker.mixin.client;

import dev.lazurite.chonker.client.IClientChunkMap;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.world.chunk.WorldChunk;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Environment(EnvType.CLIENT)
@Mixin(targets = "net/minecraft/client/world/ClientChunkManager$ClientChunkMap")
public abstract class ClientChunkMapMixin implements IClientChunkMap {
    @Unique private WorldChunk playerChunk;

    @Unique private int chunkX;
    @Unique private int chunkZ;

    @Inject(
            method = "getIndex",
            at = @At("HEAD")
    )
    private void getIndex(int chunkX, int chunkZ, CallbackInfoReturnable<Integer> cir) {
        this.chunkX = chunkX;
        this.chunkZ = chunkZ;
    }

    @Inject(
            method = "set",
            at = @At("HEAD")
    )
    protected void set(int index, WorldChunk chunk, CallbackInfo ci) {
        ClientPlayerEntity player = MinecraftClient.getInstance().player;

        if (this.chunkX == player.chunkX && this.chunkZ == player.chunkZ) {
            this.setPlayerChunk(null);
        }
    }

    @Inject(
            method = "compareAndSet",
            at = @At("HEAD")
    )
    protected void compareAndSet(int index, WorldChunk expect, @Nullable WorldChunk update, CallbackInfoReturnable<WorldChunk> cir) {
        ClientPlayerEntity player = MinecraftClient.getInstance().player;

        if (this.chunkX == player.chunkX && this.chunkZ == player.chunkZ) {
            this.setPlayerChunk(expect);
        }
    }

    @Inject(
            method = "isInRadius",
            at = @At("RETURN"),
            cancellable = true
    )
    private void isInRadius(int chunkX, int chunkZ, CallbackInfoReturnable<Boolean> cir) {
            ClientPlayerEntity player = MinecraftClient.getInstance().player;

            if (chunkX == player.chunkX && chunkZ == player.chunkZ) {
                if (this.getPlayerChunk() != null) {
                    cir.setReturnValue(true);
                }
            }
    }

    @Inject(
            method = "getChunk",
            at = @At("RETURN"),
            cancellable = true
    )
    protected void getChunk(int index, CallbackInfoReturnable<WorldChunk> cir) {
        ClientPlayerEntity player = MinecraftClient.getInstance().player;

        if (this.chunkX == player.chunkX && this.chunkZ == player.chunkZ) {
            if (this.getPlayerChunk() != null) {
                cir.setReturnValue(this.getPlayerChunk());
            }
        }
    }

    @Unique
    @Override
    public void setPlayerChunk(WorldChunk playerChunk) {
        this.playerChunk = playerChunk;
    }

    @Unique
    @Override
    public WorldChunk getPlayerChunk() {
        return this.playerChunk;
    }
}

package dev.lazurite.chonker.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.world.chunk.WorldChunk;

@Environment(EnvType.CLIENT)
public interface IClientChunkMap {
    void setPlayerChunk(WorldChunk playerChunk);
    WorldChunk getPlayerChunk();
}

package dev.lazurite.fpvracing.access;

import net.minecraft.entity.Entity;
import net.minecraft.server.network.ServerPlayerEntity;

public interface ServerPlayerAccess extends PlayerAccess {
    void setView(Entity entity);
    boolean isInGoggles(ServerPlayerEntity player);
}

package dev.lazurite.fpvracing.mixin.client;

import dev.lazurite.fpvracing.access.PlayerAccess;
import dev.lazurite.fpvracing.common.entity.QuadcopterEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

@Mixin(ClientPlayerEntity.class)
public class ClientPlayerEntityMixin implements PlayerAccess {
    @Shadow @Final protected MinecraftClient client;

    @Unique @Override
    public boolean isInGoggles() {
        return client.getCameraEntity() instanceof QuadcopterEntity;
    }
}

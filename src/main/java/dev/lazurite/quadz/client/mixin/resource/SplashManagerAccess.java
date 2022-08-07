package dev.lazurite.quadz.client.mixin.resource;

import net.minecraft.client.resources.SplashManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;

@Mixin(SplashManager.class)
public interface SplashManagerAccess {
    @Accessor List<String> getSplashes();
}

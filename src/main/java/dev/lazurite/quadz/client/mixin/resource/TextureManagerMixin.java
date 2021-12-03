package dev.lazurite.quadz.client.mixin.resource;

import dev.lazurite.quadz.Quadz;
import dev.lazurite.quadz.client.resource.TemplateTextureManager;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(TextureManager.class)
public abstract class TextureManagerMixin {
    @Shadow @Final private ResourceManager resourceManager;

    @Redirect(
            method = "loadTexture",
            at = @At(
                    value = "FIELD",
                    target = "Lnet/minecraft/client/renderer/texture/TextureManager;resourceManager:Lnet/minecraft/server/packs/resources/ResourceManager;"
            )
    )
    public ResourceManager loadTexture_FIELD(TextureManager textureManager, ResourceLocation resourceLocation) {
        if (resourceLocation.getNamespace().equals(Quadz.MODID)) {
            return new TemplateTextureManager(resourceManager);
        }

        return resourceManager;
    }
}

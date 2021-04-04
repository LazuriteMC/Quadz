package dev.lazurite.quadz.client.mixin.resource;

import dev.lazurite.quadz.Quadz;
import dev.lazurite.quadz.common.data.util.TemplateTextureManager;
import net.minecraft.client.texture.AbstractTexture;
import net.minecraft.client.texture.TextureManager;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.io.IOException;

@Mixin(TextureManager.class)
public class TextureManagerMixin {
    @Redirect(
            method = "method_24303",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/texture/AbstractTexture;load(Lnet/minecraft/resource/ResourceManager;)V"
            )
    )
    private void method_24303(AbstractTexture abstractTexture, ResourceManager manager, Identifier identifier) throws IOException {
        if (identifier.getNamespace().equals(Quadz.MODID)) {
            abstractTexture.load(new TemplateTextureManager());
        } else {
            abstractTexture.load(manager);
        }
    }
}

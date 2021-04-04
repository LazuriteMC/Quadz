package dev.lazurite.quadz.client.mixin.resource;

import net.minecraft.client.resource.language.LanguageDefinition;
import net.minecraft.client.resource.language.TranslationStorage;
import net.minecraft.resource.ResourceManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.List;
import java.util.Map;

@Mixin(TranslationStorage.class)
public class TranslationStorageMixin {
    @Inject(
            method = "load(Lnet/minecraft/resource/ResourceManager;Ljava/util/List;)Lnet/minecraft/client/resource/language/TranslationStorage;",
            at = @At("RETURN"),
            locals = LocalCapture.CAPTURE_FAILHARD,
            cancellable = true)
    private static void load(ResourceManager resourceManager, List<LanguageDefinition> definitions, CallbackInfoReturnable<TranslationStorage> info, Map<String, String> map, boolean bl) {
        info.setReturnValue(new TranslationStorage(map, bl));
    }
}

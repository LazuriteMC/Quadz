package dev.lazurite.quadz.client.mixin.resource;

import net.minecraft.client.resources.language.ClientLanguage;
import net.minecraft.client.resources.language.LanguageInfo;
import net.minecraft.server.packs.resources.ResourceManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.List;
import java.util.Map;

@Mixin(ClientLanguage.class)
public class TranslationStorageMixin {
    @Inject(
            method = "loadFrom",
            at = @At("RETURN"),
            locals = LocalCapture.CAPTURE_FAILHARD,
            cancellable = true)
    private static void load(ResourceManager resourceManager, List<LanguageInfo> list, CallbackInfoReturnable<ClientLanguage> info, Map<String, String> map, boolean bl) {
        info.setReturnValue(new ClientLanguage(map, bl));
    }
}

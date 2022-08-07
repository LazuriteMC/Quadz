package dev.lazurite.quadz.client.resource;

import dev.lazurite.quadz.Quadz;
import dev.lazurite.quadz.client.mixin.resource.SplashManagerAccess;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

/**
 * This is simply for loading custom splash-screen messages into the existing list.
 */
public class SplashResourceLoader implements SimpleSynchronousResourceReloadListener {
    public static final ResourceLocation location = new ResourceLocation(Quadz.MODID, "texts/splashes.txt");

    @Override
    public ResourceLocation getFabricId() {
        return new ResourceLocation(Quadz.MODID, "splash");
    }

    @Override
    public void onResourceManagerReload(ResourceManager manager) {
        manager.getResource(location).ifPresent(resource -> {
            try {
                final var bufferedReader = new BufferedReader(new InputStreamReader(resource.open(), StandardCharsets.UTF_8));
                final var strings = bufferedReader.lines().map(String::trim).filter((string) -> string.hashCode() != 125780783).toList();
                ((SplashManagerAccess) Minecraft.getInstance().getSplashManager()).getSplashes().addAll(strings);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }
}

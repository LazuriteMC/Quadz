package dev.lazurite.quadz.client.resource;

import dev.lazurite.quadz.Quadz;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

public class SplashResourceLoader implements SimpleSynchronousResourceReloadListener {
    public static final ResourceLocation location = new ResourceLocation(Quadz.MODID, "texts/splashes.txt");

    public SplashResourceLoader() {
        ResourceManagerHelper.get(PackType.CLIENT_RESOURCES).registerReloadListener(this);
    }

    @Override
    public ResourceLocation getFabricId() {
        return new ResourceLocation(Quadz.MODID, "splash");
    }

    @Override
    public void onResourceManagerReload(ResourceManager manager) {
        try {
            Resource resource = manager.getResource(location);
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8));
            List<String> strings = bufferedReader.lines().map(String::trim).filter((string) -> string.hashCode() != 125780783).collect(Collectors.toList());
            Minecraft.getInstance().getSplashManager().splashes.addAll(strings); // TODO: Access restricted :(
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

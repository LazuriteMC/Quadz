package dev.lazurite.quadz.client.resource;

import dev.lazurite.quadz.Quadz;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.client.MinecraftClient;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceType;
import net.minecraft.util.Identifier;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

public class SplashResourceLoader implements SimpleSynchronousResourceReloadListener {
    public static final Identifier location = new Identifier(Quadz.MODID, "texts/splashes.txt");

    public SplashResourceLoader() {
        ResourceManagerHelper.get(ResourceType.CLIENT_RESOURCES).registerReloadListener(this);
    }

    @Override
    public void apply(ResourceManager manager) {
        try {
            Resource resource = manager.getResource(location);
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8));
            List<String> strings = bufferedReader.lines().map(String::trim).filter((string) -> string.hashCode() != 125780783).collect(Collectors.toList());
            MinecraftClient.getInstance().getSplashTextLoader().splashTexts.addAll(strings);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Identifier getFabricId() {
        return new Identifier(Quadz.MODID, "splash");
    }
}

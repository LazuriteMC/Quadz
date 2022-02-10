package dev.lazurite.quadz.client.resource;

import dev.lazurite.quadz.common.data.template.TemplateLoader;
import dev.lazurite.quadz.common.data.template.model.Template;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleResource;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class TemplateTextureManager implements ResourceManager {
    private final ResourceManager original;

    public TemplateTextureManager(ResourceManager original) {
        this.original = original;
    }

    @Override
    public Set<String> getNamespaces() {
        return new HashSet<>();
    }

    @Override
    public Resource getResource(ResourceLocation resourceLocation) throws IOException {
        if (resourceLocation.getPath().endsWith("_n.png") || resourceLocation.getPath().endsWith("_s.png")) {
            return getOriginal().getResource(resourceLocation);
        }

        Template template = TemplateLoader.getTemplate(resourceLocation.getPath());

        if (template == null) {
            return original.getResource(resourceLocation);
        } else {
            return new SimpleResource(template.getId(), resourceLocation, new ByteArrayInputStream(template.getTexture()), null);
        }
    }

    @Override
    public boolean hasResource(ResourceLocation resourceLocation) {
        return false;
    }

    @Override
    public List<Resource> getResources(ResourceLocation resourceLocation) {
        return new ArrayList<>();
    }

    @Override
    public Collection<ResourceLocation> listResources(String resourceType, Predicate<String> pathPredicate) {
        return new ArrayList<>();
    }

    @Override
    public Stream<PackResources> listPacks() {
        return null;
    }

    public ResourceManager getOriginal() {
        return this.original;
    }
}

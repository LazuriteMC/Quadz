package dev.lazurite.quadz.client.resource;

import dev.lazurite.quadz.common.data.DataDriver;
import dev.lazurite.quadz.common.data.model.Template;
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
    public Resource getResource(ResourceLocation id) throws IOException {
        if (id.getPath().endsWith("_n.png") || id.getPath().endsWith("_s.png")) {
            return getOriginal().getResource(id);
        }

        Template template = DataDriver.getTemplate(id.getPath());

        if (template == null) {
            return original.getResource(id);
        } else {
            return new SimpleResource(template.getId(), id, new ByteArrayInputStream(template.getTexture()), null);
        }
    }

    @Override
    public boolean hasResource(ResourceLocation id) {
        return false;
    }

    @Override
    public List<Resource> getResources(ResourceLocation id) {
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

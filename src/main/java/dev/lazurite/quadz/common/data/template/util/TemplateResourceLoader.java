package dev.lazurite.quadz.common.data.template.util;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import dev.lazurite.quadz.Quadz;
import dev.lazurite.quadz.common.data.template.TemplateLoader;
import dev.lazurite.quadz.common.data.template.model.Template;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.SimpleTexture;
import net.minecraft.client.resources.language.ClientLanguage;
import net.minecraft.locale.Language;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.ChainedJsonException;
import net.minecraft.server.packs.resources.ResourceManager;
import software.bernie.geckolib3.core.builder.Animation;
import software.bernie.geckolib3.file.AnimationFile;
import software.bernie.geckolib3.geo.raw.pojo.Converter;
import software.bernie.geckolib3.geo.raw.pojo.FormatVersion;
import software.bernie.geckolib3.geo.raw.pojo.RawGeoModel;
import software.bernie.geckolib3.geo.raw.tree.RawGeometryTree;
import software.bernie.geckolib3.geo.render.GeoBuilder;
import software.bernie.geckolib3.resource.GeckoLibCache;
import software.bernie.geckolib3.util.json.JsonAnimationUtils;
import software.bernie.shadowed.eliotlash.molang.MolangParser;

import java.io.IOException;
import java.util.Map;

public class TemplateResourceLoader implements SimpleSynchronousResourceReloadListener {
    @Override
    public ResourceLocation getFabricId() {
        return new ResourceLocation(Quadz.MODID, "templates");
    }

    /**
     * For loading non-synchronously.
     * @param template the template to load
     */
    public void load(Template template) {
        ResourceLocation identifier = new ResourceLocation(Quadz.MODID, template.getId());

        loadGeo(identifier, template.getGeo());
        loadAnimation(identifier, template.getAnimation());
        loadLang(template.getId(), template.getSettings().getName());
        Minecraft.getInstance().getTextureManager().register(identifier, new SimpleTexture(identifier));
    }

    /**
     * Loads the goe model from json into geckolib.
     * @param resourceLocation identifies the model
     * @param geo the geo model json string
     */
    private void loadGeo(ResourceLocation resourceLocation, JsonObject geo) {
        try {
            RawGeoModel rawModel = Converter.fromJsonString(geo.toString());
            if (rawModel.getFormatVersion() != FormatVersion.VERSION_1_12_0) {
                throw new RuntimeException(resourceLocation.getPath() + " : Wrong geometry json version, expected 1.12.0");
            }

            RawGeometryTree rawGeometryTree = RawGeometryTree.parseHierarchy(rawModel);
            GeckoLibCache.getInstance().getGeoModels().put(resourceLocation, GeoBuilder.getGeoBuilder(Quadz.MODID).constructGeoModel(rawGeometryTree));
        } catch (IOException e) {
            throw new RuntimeException(resourceLocation.getPath() + " : Problem reading geo model.");
        }
    }

    /**
     * Loads the animation from json into geckolib.
     * @param resourceLocation identifies the animation
     * @param animation the animation json string
     */
    private void loadAnimation(ResourceLocation resourceLocation, JsonObject animation) {
        AnimationFile animationFile = new AnimationFile();

        for (Map.Entry<String, JsonElement> entry : JsonAnimationUtils.getAnimations(animation)) {
            String animationName = entry.getKey();
            Animation anim;

            try {
                anim = JsonAnimationUtils.deserializeJsonToAnimation(JsonAnimationUtils.getAnimation(animation, animationName), new MolangParser());
                animationFile.putAnimation(animationName, anim);
            } catch (ChainedJsonException e) {
                throw new RuntimeException("Could not load animation from quadcopter template: " + animationName);
            }
        }

        GeckoLibCache.getInstance().getAnimations().put(resourceLocation, animationFile);
    }

    /**
     * Loads the name of the template into translation storage.
     * @param id the id of the template
     * @param name the actual name
     */
    private void loadLang(String id, String name) {
        if (Language.getInstance() instanceof ClientLanguage) {
            ((ClientLanguage) Language.getInstance()).storage.put("template." + Quadz.MODID + "." + id, name);
        }
    }

    @Override
    public void onResourceManagerReload(ResourceManager manager) {
        TemplateLoader.getTemplates().forEach(this::load);
    }
}

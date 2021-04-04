package dev.lazurite.quadz.common.data.util;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import dev.lazurite.quadz.Quadz;
import dev.lazurite.quadz.common.data.DataDriver;
import dev.lazurite.quadz.common.data.model.Template;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.ShaderParseException;
import net.minecraft.client.resource.language.TranslationStorage;
import net.minecraft.client.texture.ResourceTexture;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceType;
import net.minecraft.util.Identifier;
import net.minecraft.util.Language;
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

    public TemplateResourceLoader() {
        ResourceManagerHelper.get(ResourceType.CLIENT_RESOURCES).registerReloadListener(this);
    }

    @Override
    public Identifier getFabricId() {
        return new Identifier(Quadz.MODID, "templates");
    }

    @Override
    public void apply(ResourceManager manager) {
        DataDriver.getTemplates().forEach(this::load);
    }

    /**
     * For loading non-synchronously.
     * @param template the template to load
     */
    public void load(Template template) {
        Identifier identifier = new Identifier(Quadz.MODID, template.getId());

        loadGeo(identifier, template.getGeo());
        loadAnimation(identifier, template.getAnimation());
        loadLang(template.getId(), template.getSettings().getName());
        MinecraftClient.getInstance().getTextureManager().registerTexture(identifier, new ResourceTexture(identifier));
    }

    /**
     * Loads the goe model from json into geckolib.
     * @param identifier identifies the model
     * @param geo the geo model json string
     */
    private void loadGeo(Identifier identifier, JsonObject geo) {
        try {
            RawGeoModel rawModel = Converter.fromJsonString(geo.toString());
            if (rawModel.getFormatVersion() != FormatVersion.VERSION_1_12_0) {
                throw new RuntimeException(identifier.getPath() + " : Wrong geometry json version, expected 1.12.0");
            }

            RawGeometryTree rawGeometryTree = RawGeometryTree.parseHierarchy(rawModel);
            GeckoLibCache.getInstance().getGeoModels().put(identifier, GeoBuilder.constructGeoModel(rawGeometryTree));
        } catch (IOException e) {
            throw new RuntimeException(identifier.getPath() + " : Problem reading geo model.");
        }
    }

    /**
     * Loads the animation from json into geckolib.
     * @param identifier identifies the animation
     * @param animation the animation json string
     */
    private void loadAnimation(Identifier identifier, JsonObject animation) {
        AnimationFile animationFile = new AnimationFile();

        for (Map.Entry<String, JsonElement> entry : JsonAnimationUtils.getAnimations(animation)) {
            String animationName = entry.getKey();
            Animation anim;

            try {
                anim = JsonAnimationUtils.deserializeJsonToAnimation(JsonAnimationUtils.getAnimation(animation, animationName), new MolangParser());
                animationFile.putAnimation(animationName, anim);
            } catch (ShaderParseException e) {
                throw new RuntimeException("Could not load animation from quadcopter template: " + animationName);
            }
        }

        GeckoLibCache.getInstance().getAnimations().put(identifier, animationFile);
    }

    /**
     * Loads the name of the template into translation storage.
     * @param id the id of the template
     * @param name the actual name
     */
    private void loadLang(String id, String name) {
        if (Language.getInstance() instanceof TranslationStorage) {
            ((TranslationStorage) Language.getInstance()).translations.put("template." + Quadz.MODID + "." + id, name);
        }
    }
}

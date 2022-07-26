package dev.lazurite.quadz.api;

import dev.lazurite.quadz.common.data.JoystickRegistryImpl;
import dev.lazurite.quadz.common.data.model.JoystickAxis;
import dev.lazurite.quadz.common.data.template.TemplateLoader;
import net.minecraft.resources.ResourceLocation;

import java.util.List;
import java.util.Optional;

public interface JoystickRegistry {
    static JoystickRegistry getInstance() {
        return JoystickRegistryImpl.INSTANCE;
    }

    static void registerTemplateFolder(String folderName) {
        TemplateLoader.addTemplateFolder(folderName);
    }

    void registerJoystickInput(JoystickAxis joystickAxis);

    List<JoystickAxis> getJoystickAxes();
    Optional<JoystickAxis> getJoystickAxis(ResourceLocation resourceLocation);
}

package dev.lazurite.quadz.common.data;

import dev.lazurite.quadz.api.JoystickRegistry;
import dev.lazurite.quadz.common.data.model.JoystickAxis;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class JoystickRegistryImpl implements JoystickRegistry {
    public static JoystickRegistry INSTANCE = new JoystickRegistryImpl();
    private final List<JoystickAxis> joystickAxes = new ArrayList<>();

    @Override
    public void registerJoystickInput(JoystickAxis joystickAxis) {
        this.joystickAxes.add(joystickAxis);
    }

    @Override
    public List<JoystickAxis> getJoystickAxes() {
        return new ArrayList<>(this.joystickAxes);
    }

    @Override
    public Optional<JoystickAxis> getJoystickAxis(ResourceLocation resourceLocation) {
        for (var joystickAxis : this.joystickAxes) {
            if (joystickAxis.getResourceLocation().equals(resourceLocation)) {
                return Optional.of(joystickAxis);
            }
        }

        return Optional.empty();
    }
}

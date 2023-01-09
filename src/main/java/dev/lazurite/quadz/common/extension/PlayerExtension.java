package dev.lazurite.quadz.common.extension;

import net.minecraft.resources.ResourceLocation;

import java.util.HashMap;
import java.util.Map;

public interface PlayerExtension {

    default float getJoystickValue(ResourceLocation axis) {
        return 0.0f;
    }

    default void setJoystickValue(ResourceLocation axis, float value) {
    }

    default Map<ResourceLocation, Float> getAllAxes() {
        return new HashMap<>();
    }

    default void syncJoystick() {
    }

}

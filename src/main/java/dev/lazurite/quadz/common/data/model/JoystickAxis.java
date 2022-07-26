package dev.lazurite.quadz.common.data.model;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public class JoystickAxis {
    private final ResourceLocation resourceLocation;
    private final Component name;
    private int axis;
    private boolean inverted;

    public JoystickAxis(ResourceLocation resourceLocation, Component name, int axis, boolean inverted) {
        this.resourceLocation = resourceLocation;
        this.name = name;
        this.axis = axis;
        this.inverted = inverted;
    }

    public void setAxis(int axis) {
        this.axis = axis;
    }

    public void setInverted(boolean inverted) {
        this.inverted = inverted;
    }

    public ResourceLocation getResourceLocation() {
        return this.resourceLocation;
    }

    public Component getName() {
        return this.name;
    }

    public int getAxis() {
        return this.axis;
    }

    public boolean isInverted() {
        return this.inverted;
    }
}

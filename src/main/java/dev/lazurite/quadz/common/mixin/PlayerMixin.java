package dev.lazurite.quadz.common.mixin;

import dev.lazurite.quadz.common.extension.PlayerExtension;
import dev.lazurite.quadz.common.hooks.PlayerHooks;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;

import java.util.Map;

@Mixin(Player.class)
public class PlayerMixin implements PlayerExtension {

    @Override
    public float getJoystickValue(ResourceLocation axis) {
        return PlayerHooks.onGetJoystickValues(axis);
    }

    @Override
    public void setJoystickValue(ResourceLocation axis, float value) {
        PlayerHooks.onSetJoystickValue(axis, value);
    }

    @Override
    public Map<ResourceLocation, Float> getAllAxes() {
        return PlayerHooks.onGetAllAxes();
    }

    @Override
    public void sync() {
        PlayerHooks.onSync((Player) (Object) this);
    }

}

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
    public float quadz$getJoystickValue(ResourceLocation axis) {
        return PlayerHooks.onGetJoystickValues(axis);
    }

    @Override
    public void quadz$setJoystickValue(ResourceLocation axis, float value) {
        PlayerHooks.onSetJoystickValue(axis, value);
    }

    @Override
    public Map<ResourceLocation, Float> quadz$getAllAxes() {
        return PlayerHooks.onGetAllAxes();
    }

    @Override
    public void quadz$syncJoystick() {
        PlayerHooks.onSyncJoystick((Player) (Object) this);
    }

}

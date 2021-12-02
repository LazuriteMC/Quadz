package dev.lazurite.quadz.client.mixin.input;

import com.mojang.blaze3d.platform.InputConstants;
import dev.lazurite.quadz.common.state.entity.QuadcopterEntity;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHandler;
import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(MouseHandler.class)
public class MouseMixin {
    @Shadow @Final private Minecraft minecraft;

    /**
     * This mixin redirects the {@link ClientPlayerEntity#changeLookDirection(double, double)} method
     * so that when the mouse is moved while flying a drone, nothing happens.
     * @param player the client player
     * @param cursorDeltaX the x cursor position
     * @param cursorDeltaY the y cursor position
     */
    @Redirect(
            method = "updateMouse",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/network/ClientPlayerEntity;changeLookDirection(DD)V"
            )
    )
    public void changeLookDirection(LocalPlayer player, double cursorDeltaX, double cursorDeltaY) {
        if (!(minecraft.getCameraEntity() instanceof QuadcopterEntity)) {
            player.turn(cursorDeltaX, cursorDeltaY);
        }
    }

    /**
     * This mixin redirects the {@link KeyBinding#setKeyPressed(InputUtil.Key, boolean)} method
     * so that when the player is flying a drone, it is not called.
     * @param key the key being pressed
     * @param pressed whether or not the key is pressed
     */
    @Redirect(
            method = "onMouseButton",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/option/KeyBinding;setKeyPressed(Lnet/minecraft/client/util/InputUtil$Key;Z)V"
            )
    )
    public void setKeyPressed(InputConstants.Key key, boolean pressed) {
        KeyMapping.set(key, !(minecraft.getCameraEntity() instanceof QuadcopterEntity) && pressed);
    }

    /**
     * This mixin redirects the {@link KeyBinding#onKeyPressed(InputUtil.Key)} method
     * so that when the player is flying a drone, it is not called.
     * @param key the key being pressed
     */
    @Redirect(
            method = "onMouseButton",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/option/KeyBinding;onKeyPressed(Lnet/minecraft/client/util/InputUtil$Key;)V"
            )
    )
    public void onKeyPressed(InputConstants.Key key) {
        if (!(minecraft.getCameraEntity() instanceof QuadcopterEntity)) {
            KeyMapping.click(key);
        }
    }
}
package dev.lazurite.quadz.client.mixin.input;

import com.mojang.blaze3d.platform.InputConstants;
import dev.lazurite.quadz.client.input.InputTick;
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
public class MouseHandlerMixin {
    @Shadow @Final private Minecraft minecraft;

    /**
     * This mixin redirects the {@link LocalPlayer#turn(double, double)} method
     * so that when the mouse is moved while flying a drone, nothing happens.
     * @param player the client player
     * @param cursorDeltaX the x cursor position
     * @param cursorDeltaY the y cursor position
     */
    @Redirect(
            method = "turnPlayer",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/player/LocalPlayer;turn(DD)V"
            )
    )
    public void turnPlayer_turn(LocalPlayer player, double cursorDeltaX, double cursorDeltaY) {
        if (!(minecraft.getCameraEntity() instanceof QuadcopterEntity)) {
            player.turn(cursorDeltaX, cursorDeltaY);
        }
    }

    /**
     * This mixin redirects the {@link KeyMapping#set(InputConstants.Key, boolean)} method
     * so that when the player is flying a drone, it is not called.
     * @param key the key being pressed
     * @param pressed whether or not the key is pressed
     */
    @Redirect(
            method = "onPress",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/KeyMapping;set(Lcom/mojang/blaze3d/platform/InputConstants$Key;Z)V"
            )
    )
    public void onPress_set(InputConstants.Key key, boolean pressed) {
        KeyMapping.set(key, !(minecraft.getCameraEntity() instanceof QuadcopterEntity) && pressed);
    }

    /**
     * This mixin redirects the {@link KeyMapping#click(InputConstants.Key)} method
     * so that when the player is flying a drone, it is not called.
     * @param key the key being pressed
     */
    @Redirect(
            method = "onPress",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/KeyMapping;click(Lcom/mojang/blaze3d/platform/InputConstants$Key;)V"
            )
    )
    public void onPress_click(InputConstants.Key key) {
        if (minecraft.getCameraEntity() instanceof QuadcopterEntity) {
            if (key.getName().equals("key.mouse.left")) {
                InputTick.LEFT_CLICK_EVENT.invoke();
            } else if (key.getName().equals("key.mouse.right")) {
                InputTick.RIGHT_CLICK_EVENT.invoke();
            }
        } else {
            KeyMapping.click(key);
        }
    }
}
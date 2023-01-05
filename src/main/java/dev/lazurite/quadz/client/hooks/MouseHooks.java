package dev.lazurite.quadz.client.hooks;

import com.mojang.blaze3d.platform.InputConstants;
import dev.lazurite.quadz.common.util.event.ClickEvents;
import dev.lazurite.quadz.client.Config;
import dev.lazurite.quadz.client.QuadzClient;
import dev.lazurite.quadz.common.entity.Quadcopter;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;

public class MouseHooks {

    /**
     * Controls whether the {@link KeyMapping#click} method is called based on
     * whether the player is flying a {@link Quadcopter}.
     */
    public static void onClick(InputConstants.Key key) {
        if (Minecraft.getInstance().getCameraEntity() instanceof Quadcopter) {
            if (key.getName().equals("key.mouse.left")) {
                ClickEvents.LEFT_CLICK_EVENT.invoke();
            } else if (key.getName().equals("key.mouse.right")) {
                ClickEvents.RIGHT_CLICK_EVENT.invoke();
            }
        } else {
            KeyMapping.click(key);
        }
    }

    /**
     * Controls whether the {@link KeyMapping#set} method is called based on
     * whether the player is flying a {@link Quadcopter}.
     */
    public static void onSet(InputConstants.Key key, boolean pressed) {
        KeyMapping.set(key, !(Minecraft.getInstance().getCameraEntity() instanceof Quadcopter) && pressed);
    }

    /**
     * Prevents the player from turning using their mouse under the following conditions.
     */
    public static boolean cancelTurnPlayer() {
        return Minecraft.getInstance().getCameraEntity() instanceof Quadcopter || (QuadzClient.getQuadcopter().isPresent() && Config.followLOS);
    }

}

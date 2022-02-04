package dev.lazurite.quadz.client.input.keybind;

import com.mojang.blaze3d.platform.InputConstants;
import dev.lazurite.quadz.Quadz;
import dev.lazurite.quadz.client.render.ui.screen.QuadcopterScreen;
import dev.lazurite.quadz.common.state.Bindable;
import dev.lazurite.quadz.common.state.QuadcopterState;
import dev.lazurite.quadz.common.state.entity.QuadcopterEntity;
import dev.lazurite.toolbox.api.event.ClientEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.KeyMapping;
import org.lwjgl.glfw.GLFW;

public class QuadConfigKeybind {
    public static void register() {
        final var key = new KeyMapping(
                "key." + Quadz.MODID + ".quadconfig",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_Z,
                "key." + Quadz.MODID + ".category"
        );

        KeyBindingHelper.registerKeyBinding(key);

        ClientEvents.Tick.END_CLIENT_TICK.register(client -> {
            if (key.consumeClick() && client.player != null && client.level != null) {
                if (client.getCameraEntity() instanceof QuadcopterEntity) {
                    QuadcopterScreen.show((QuadcopterEntity) client.getCameraEntity());
                } else {
                    Bindable.get(client.player.getMainHandItem()).flatMap(bindable -> QuadcopterState.getQuadcopterByBindId(
                            client.level,
                            client.player.position(),
                            bindable.getBindId(),
                            (int) client.gameRenderer.getRenderDistance()
                    )).ifPresent(QuadcopterScreen::show);
                }
            }
        });
    }
}

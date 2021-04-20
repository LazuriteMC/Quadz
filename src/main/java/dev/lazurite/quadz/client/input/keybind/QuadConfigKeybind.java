package dev.lazurite.quadz.client.input.keybind;

import dev.lazurite.quadz.Quadz;
import dev.lazurite.quadz.client.render.ui.screen.QuadcopterScreen;
import dev.lazurite.quadz.common.state.Bindable;
import dev.lazurite.quadz.common.state.QuadcopterState;
import dev.lazurite.quadz.common.state.entity.QuadcopterEntity;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.options.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

@Environment(EnvType.CLIENT)
public class QuadConfigKeybind {
    public static void register() {
        KeyBinding key = new KeyBinding(
                "key." + Quadz.MODID + ".quadconfig",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_Z,
                "key." + Quadz.MODID + ".category"
        );

        KeyBindingHelper.registerKeyBinding(key);
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (key.wasPressed() && client.player != null && client.world != null) {
                if (client.getCameraEntity() instanceof QuadcopterEntity) {
                    QuadcopterScreen.show((QuadcopterEntity) client.getCameraEntity());
                } else {
                    Bindable.get(client.player.getMainHandStack()).ifPresent(bindable -> {
                        QuadcopterEntity quadcopter = QuadcopterState.getQuadcopterByBindId(client.world, client.player.getPos(), bindable.getBindId(), (int) client.gameRenderer.getViewDistance());

                        if (quadcopter != null) {
                            QuadcopterScreen.show(quadcopter);
                        }
                    });
                }
            }
        });
    }
}

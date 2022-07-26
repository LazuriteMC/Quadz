package dev.lazurite.quadz.client.mixin;

import com.google.common.collect.Maps;
import dev.lazurite.quadz.api.InputHandler;
import dev.lazurite.quadz.api.event.JoystickEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.util.profiling.ProfilerFiller;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;

import static org.lwjgl.glfw.GLFW.glfwJoystickPresent;

@Mixin(Minecraft.class)
public class MinecraftMixin {
    @Shadow private ProfilerFiller profiler;
    @Unique private long next;
    @Unique private boolean loaded;

    @Inject(
            method = "runTick",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/components/toasts/ToastComponent;render(Lcom/mojang/blaze3d/vertex/PoseStack;)V",
                    shift = At.Shift.AFTER
            )
    )
    public void runTick_render(boolean bl, CallbackInfo info) {
        this.profiler.popPush("gamepadInput");

        if (System.currentTimeMillis() > next) {
            Map<Integer, String> lastJoysticks = Maps.newHashMap(InputHandler.JOYSTICKS);
            InputHandler.JOYSTICKS.clear();

            for (int i = 0; i < 16; i++) {
                if (glfwJoystickPresent(i)) {
                    InputHandler.JOYSTICKS.put(i, InputHandler.getJoystickName(i));

                    if (!lastJoysticks.containsKey(i) && loaded) {
                        JoystickEvents.JOYSTICK_CONNECT.invoke(i, InputHandler.getJoystickName(i));
                    }
                } else if (lastJoysticks.containsKey(i) && loaded) {
                    JoystickEvents.JOYSTICK_DISCONNECT.invoke(i, lastJoysticks.get(i));
                }
            }

            next = System.currentTimeMillis() + 500;
            loaded = true;
        }
    }
}

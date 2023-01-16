package dev.lazurite.quadz.client.render;

import com.google.common.collect.Maps;
import com.mojang.blaze3d.vertex.PoseStack;
import dev.lazurite.quadz.client.event.ClientEventHooks;
import dev.lazurite.quadz.client.render.screen.osd.OnScreenDisplay;
import dev.lazurite.quadz.common.util.event.JoystickEvents;
import dev.lazurite.quadz.common.util.JoystickOutput;
import dev.lazurite.quadz.client.Config;
import dev.lazurite.quadz.client.QuadzClient;
import dev.lazurite.quadz.common.entity.Quadcopter;
import dev.lazurite.rayon.impl.bullet.thread.util.Clock;
import dev.lazurite.toolbox.api.math.QuaternionHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.OptionInstance;
import net.minecraft.client.gui.Font;
import net.minecraft.util.Mth;
import net.minecraft.util.profiling.ProfilerFiller;
import org.joml.Quaternionf;

import java.util.Map;

import static org.lwjgl.glfw.GLFW.glfwJoystickPresent;

public class RenderHooks {

    private static final Clock clock = new Clock();

    public static void onRenderShaders(float tickDelta) {
        if (QuadzClient.getQuadcopterFromCamera().isPresent() && Minecraft.getInstance().options.getCameraType().isFirstPerson()) {
            if (QuadzClient.STATIC_SHADER.isInitialized() && Config.videoInterferenceEnabled) {
                QuadzClient.STATIC_AMOUNT.set(Mth.clamp(ClientEventHooks.quadDistanceFromPlayer / Quadcopter.MAX_RANGE, 0.0f, 3.0f));
                QuadzClient.STATIC_TIMER.set(clock.get());
                QuadzClient.STATIC_SHADER.render(tickDelta);
            }

            if (QuadzClient.FISHEYE_SHADER.isInitialized() && Config.fisheyeEnabled) {
                QuadzClient.FISHEYE_AMOUNT.set(Config.fisheyeAmount);
                QuadzClient.FISHEYE_SHADER.render(tickDelta);
            }
        }
    }

    /**
     * Rotates the camera according to the quadcopter's perspective.
     */
    public static void onRenderLevel(float tickDelta) {
        /* Rotate the player's yaw and pitch to follow the quadcopter */
        if (Config.followLOS) {
            QuadzClient.getQuadcopterFromRemote().ifPresent(quadcopter -> {
                if (Minecraft.getInstance().player.hasLineOfSight(quadcopter)) {
                    /* Get the difference in position between the player and the quadcopter */
                    var delta = Minecraft.getInstance().player.getEyePosition().subtract(quadcopter.getPosition(tickDelta));

                    /* Set new pitch and yaw */
					Minecraft.getInstance().player.setYRot((float) Math.toDegrees(Math.atan2(delta.z, delta.x)) + 90);
					Minecraft.getInstance().player.setXRot(20 + (float) Math.toDegrees(Math.atan2(delta.y, Math.sqrt(delta.x * delta.x + delta.z * delta.z))));
                }
            });
        }
    }

    private static boolean loaded;
    private static long next;

    public static void onRenderMinecraft(ProfilerFiller profiler) {
        profiler.popPush("gamepadInput");

        if (System.currentTimeMillis() > next) {
            Map<Integer, String> lastJoysticks = Maps.newHashMap(JoystickOutput.JOYSTICKS);
            JoystickOutput.JOYSTICKS.clear();

            for (int i = 0; i < 16; i++) {
                if (glfwJoystickPresent(i)) {
                    JoystickOutput.JOYSTICKS.put(i, JoystickOutput.getJoystickName(i));

                    if (!lastJoysticks.containsKey(i) && loaded) {
                        JoystickEvents.JOYSTICK_CONNECT.invoke(i, JoystickOutput.getJoystickName(i));
                    }
                } else if (lastJoysticks.containsKey(i) && loaded) {
                    JoystickEvents.JOYSTICK_DISCONNECT.invoke(i, lastJoysticks.get(i));
                }
            }

            next = System.currentTimeMillis() + 500;
            loaded = true;
        }
    }

    public static Quaternionf onMultiplyYaw(Quaternionf quaternion) {
        if (QuadzClient.getQuadcopterFromCamera().isPresent()) {
            return QuaternionHelper.rotateY(new Quaternionf(0, 0, 0, 1), 180);
        }

        return quaternion;
    }

    public static Quaternionf onMultiplyPitch(Quaternionf quaternion) {
        if (QuadzClient.getQuadcopterFromCamera().isPresent()) {
            return new Quaternionf(0, 0, 0, 1);
        }

        return quaternion;
    }

    public static Object onGetFOV(OptionInstance<Integer> optionInstance) {
        if (QuadzClient.getQuadcopterFromCamera().isPresent() && Config.firstPersonFOV > 30) {
            return Config.firstPersonFOV;
        }

        return optionInstance.get();
    }

    public static void onRenderGui(Font font, PoseStack poseStack, float tickDelta) {
        if (Config.osdEnabled) {
            QuadzClient.getQuadcopterFromCamera().ifPresent(quadcopter ->
                OnScreenDisplay.render(quadcopter, font, poseStack, tickDelta)
            );
        }
    }

}

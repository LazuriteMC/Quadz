package dev.lazurite.fpvracing.client.input.keybind;

import dev.lazurite.fpvracing.FPVRacing;
import dev.lazurite.fpvracing.common.item.container.GogglesContainer;
import dev.lazurite.fpvracing.common.item.GogglesItem;
import dev.lazurite.fpvracing.client.packet.keybind.PowerGogglesC2S;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.options.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.item.ItemStack;
import net.minecraft.text.TranslatableText;
import org.lwjgl.glfw.GLFW;

@Environment(EnvType.CLIENT)
public class GogglePowerKeybind {
    private static KeyBinding key;

    public static void callback(MinecraftClient client) {
        if (client.player != null) {
            ItemStack hat = client.player.inventory.armor.get(3);
            ItemStack hand = client.player.getMainHandStack();
            GogglesContainer goggles;
            boolean sentPowerOn = false;

            if (hand.getItem() instanceof GogglesItem) {
                goggles = FPVRacing.GOGGLES_CONTAINER.get(hand);
            } else if (hat.getItem() instanceof GogglesItem) {
                goggles = FPVRacing.GOGGLES_CONTAINER.get(hat);
            } else return;

            if (key.wasPressed()) {
                PowerGogglesC2S.send(!goggles.isEnabled());
                sentPowerOn = !goggles.isEnabled();
            } else if (client.options.keySneak.wasPressed()) {
                PowerGogglesC2S.send(false);
            }

            if (sentPowerOn) {
                String sneakKey = KeyBindingHelper.getBoundKeyOf(MinecraftClient.getInstance().options.keySneak).getLocalizedText().getString().toUpperCase();
                String enableKey = key.getBoundKeyLocalizedText().getString().toUpperCase();
                client.player.sendMessage(new TranslatableText("message.fpvracing.goggles_on", sneakKey, enableKey), true);
            }
        }
    }

    public static void register() {
        key = new KeyBinding(
                "key." + FPVRacing.MODID + ".powergoggles",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_G,
                "key." + FPVRacing.MODID + ".category"
        );

        KeyBindingHelper.registerKeyBinding(key);
        ClientTickEvents.END_CLIENT_TICK.register(GogglePowerKeybind::callback);
    }
}



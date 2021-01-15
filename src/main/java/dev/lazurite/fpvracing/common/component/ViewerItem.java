package dev.lazurite.fpvracing.common.component;

import dev.lazurite.fpvracing.FPVRacing;
import dev.lazurite.fpvracing.access.PlayerAccess;
import dev.lazurite.fpvracing.common.entity.FlyableEntity;
import dev.onyxstudios.cca.api.v3.component.ComponentV3;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;

public class ViewerItem implements ComponentV3 {
    private final ItemStack stack;

    public ViewerItem(ItemStack stack) {
        this.stack = stack;
    }

    /**
     * Determines whether or not the client
     * has a camera entity of type {@link dev.lazurite.fpvracing.common.entity.FlyableEntity}.
     * @return whether or not the player is viewing through a flyable entity
     */
    @Environment(EnvType.CLIENT)
    public static boolean isInGoggles() {
        return MinecraftClient.getInstance().getCameraEntity() instanceof FlyableEntity;
    }

    public static void setOn(ItemStack itemStack, boolean on) {
        itemStack.getOrCreateSubTag(FPVRacing.MODID).putBoolean("on", on);
    }

    public static boolean isOn(PlayerEntity player) {
        if (((PlayerAccess) player).isInGoggles()) {
            ItemStack hat = player.inventory.armor.get(3);

            if (hat.getSubTag(FPVRacing.MODID) != null && hat.getSubTag(FPVRacing.MODID).contains("on")) {
                return hat.getSubTag(FPVRacing.MODID).getBoolean("on");
            }
        }

        return false;
    }

    @Override
    public void readFromNbt(CompoundTag compoundTag) {

    }

    @Override
    public void writeToNbt(CompoundTag compoundTag) {

    }
}

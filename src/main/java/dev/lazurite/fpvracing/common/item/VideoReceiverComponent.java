package dev.lazurite.fpvracing.common.item;

import dev.lazurite.fpvracing.FPVRacing;
import dev.lazurite.fpvracing.access.PlayerAccess;
import dev.onyxstudios.cca.api.v3.component.ComponentV3;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;

public class VideoReceiverComponent implements ComponentV3 {
    private final ItemStack stack;

    public VideoReceiverComponent(ItemStack stack) {
        this.stack = stack;
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

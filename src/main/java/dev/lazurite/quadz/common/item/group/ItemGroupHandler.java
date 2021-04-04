package dev.lazurite.quadz.common.item.group;

import dev.lazurite.quadz.Quadz;
import net.fabricmc.fabric.impl.item.group.ItemGroupExtensions;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.util.collection.DefaultedList;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class ItemGroupHandler {
    private static final ItemGroupHandler instance = new ItemGroupHandler();
    private final List<ItemStack> stacks = new ArrayList<>();

    public static ItemGroupHandler getInstance() {
        return instance;
    }

    private ItemGroupHandler() { }

    public ItemGroupHandler register(ItemStack... items) {
        this.stacks.addAll(Arrays.asList(items));
        return this;
    }

    public void build() {
        ((ItemGroupExtensions) ItemGroup.BUILDING_BLOCKS).fabric_expandArray();
        this.build(ItemGroup.GROUPS.length - 1);
    }

    private void build(int index) {
        new ItemGroup(index, String.format("%s.%s", Quadz.MODID, "items")) {
            @Override
            public ItemStack createIcon() {
                return new ItemStack(Quadz.GOGGLES_ITEM);
            }

            @Override
            public void appendStacks(DefaultedList<ItemStack> stacks) {
                stacks.addAll(ItemGroupHandler.getInstance().getStacks());
                super.appendStacks(stacks);
            }
        };
    }

    public List<ItemStack> getStacks() {
        return new ArrayList<>(stacks);
    }
}

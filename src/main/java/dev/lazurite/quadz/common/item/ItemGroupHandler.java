package dev.lazurite.quadz.common.item;

import dev.lazurite.quadz.Quadz;
import net.fabricmc.fabric.impl.item.group.ItemGroupExtensions;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;

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
        ((ItemGroupExtensions) CreativeModeTab.TAB_BUILDING_BLOCKS).fabric_expandArray();
        this.build(CreativeModeTab.TABS.length - 1);
    }

    private void build(int index) {
        new CreativeModeTab(index, String.format("%s.%s", Quadz.MODID, "items")) {
            @Override
            public ItemStack makeIcon() {
                return new ItemStack(Quadz.GOGGLES_ITEM);
            }

            @Override
            public void fillItemList(NonNullList<ItemStack> stacks) {
                stacks.addAll(ItemGroupHandler.getInstance().getStacks());
                super.fillItemList(stacks);
            }
        };
    }

    public List<ItemStack> getStacks() {
        return new ArrayList<>(stacks);
    }
}

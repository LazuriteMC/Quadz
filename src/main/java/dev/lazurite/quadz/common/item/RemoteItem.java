package dev.lazurite.quadz.common.item;

import net.minecraft.world.item.Item;

/**
 * Represents a held "Transmitter" or remote for controlling a quadcopter.
 * However, this class is not likely to contain much of the logic.
 */
public class RemoteItem extends Item {

    public RemoteItem() {
        super(new Properties().stacksTo(1));
    }

}

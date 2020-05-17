package bluevista.fpvracingmod.server.items;

import com.bluevista.fpvracing.server.EntityRegistry;
import com.bluevista.fpvracing.server.entities.DroneEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemUseContext;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class ItemDroneSpawner extends Item {
	public ItemDroneSpawner(Item.Properties builder) {
		super(builder);
	}

	/**
	 * Called when this item is used while targeting a Block
	 */
	public ActionResultType onItemUse(ItemUseContext context) {
		World world = context.getWorld();
		if (!world.isRemote) {
			BlockPos pos = context.getPos().add(0, 1, 0);
			DroneEntity d = new DroneEntity(com.bluevista.fpvracing.server.EntityRegistry.DRONE.get(), world);
			d.setPosition(pos.getX(), pos.getY(), pos.getZ());
			world.addEntity(d);
//			EntityRegistry.DRONE.spawn(world, context.getItem(), context.getPlayer(), pos, SpawnReason.SPAWNER, false, false);
		}

		return ActionResultType.SUCCESS;
	}
	
}


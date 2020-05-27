package bluevista.fpvracingmod.server.items;

import bluevista.fpvracingmod.server.entities.DroneEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.world.RayTraceContext;
import net.minecraft.world.World;

public class DroneSpawnerItem extends Item {
	public DroneSpawnerItem(Settings settings) {
		super(settings);
	}

	/**
	 * Called when this item is used while targeting a Block
	 */
	public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
		ItemStack itemStack = user.getStackInHand(hand);
		HitResult hitResult = rayTrace(world, user, RayTraceContext.FluidHandling.ANY);

		if (!world.isClient) {
			if (hitResult.getType() == net.minecraft.util.hit.HitResult.Type.MISS)
				return TypedActionResult.pass(itemStack);

			if (hitResult.getType() == net.minecraft.util.hit.HitResult.Type.BLOCK) {
				DroneEntity d = new DroneEntity(world);
				d.setPos(hitResult.getPos().x, hitResult.getPos().y, hitResult.getPos().z);
				System.out.println("X is: " + hitResult.getPos().x);
				System.out.println("Y is: " + hitResult.getPos().y);
				System.out.println("Z is: " + hitResult.getPos().z);
				world.spawnEntity(d);
			}
		}

		return TypedActionResult.success(itemStack);
	}
	
}


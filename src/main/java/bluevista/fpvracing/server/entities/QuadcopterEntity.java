package bluevista.fpvracing.server.entities;

import bluevista.fpvracing.client.ClientInitializer;
import bluevista.fpvracing.client.ClientTick;
import bluevista.fpvracing.client.input.InputTick;
import bluevista.fpvracing.network.datatracker.FlyableTrackerRegistry;
import bluevista.fpvracing.physics.thrust.QuadcopterThrust;
import bluevista.fpvracing.physics.entity.ClientEntityPhysics;
import bluevista.fpvracing.server.ServerInitializer;
import bluevista.fpvracing.server.items.QuadcopterItem;
import bluevista.fpvracing.server.items.TransmitterItem;
import bluevista.fpvracing.util.math.BetaflightHelper;
import bluevista.fpvracing.util.math.QuaternionHelper;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.ProjectileDamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;

import javax.vecmath.Quat4f;

/**
 * @author Ethan Johnson
 * @author Patrick Hofmann
 */
public class QuadcopterEntity extends FlyableEntity {
	/**
	 * The constructor called by the Fabric API in {@link ServerInitializer}. Invokes the main constructor.
	 * @param type the {@link EntityType}
	 * @param world the {@link World} that the {@link QuadcopterEntity} will be spawned in
	 */
	public QuadcopterEntity(EntityType<?> type, World world) {
		super(type, world);
	}

	@Environment(EnvType.CLIENT)
	public void step(float delta) {
		super.step(delta);

		ClientEntityPhysics physics = (ClientEntityPhysics) this.physics;
		QuadcopterThrust thrust = new QuadcopterThrust(this);

		physics.decreaseAngularVelocity();
		physics.calculateBlockDamage();

		if (!ClientTick.isServerModded || TransmitterItem.isBoundTransmitter(ClientInitializer.client.player.getMainHandStack(), this)) {
			float deltaX = (float) BetaflightHelper.calculateRates(InputTick.axisValues.currX, getValue(FlyableTrackerRegistry.RATE), getValue(FlyableTrackerRegistry.EXPO), getValue(FlyableTrackerRegistry.SUPER_RATE), delta);
			float deltaY = (float) BetaflightHelper.calculateRates(InputTick.axisValues.currY, getValue(FlyableTrackerRegistry.RATE), getValue(FlyableTrackerRegistry.EXPO), getValue(FlyableTrackerRegistry.SUPER_RATE), delta);
			float deltaZ = (float) BetaflightHelper.calculateRates(InputTick.axisValues.currZ, getValue(FlyableTrackerRegistry.RATE), getValue(FlyableTrackerRegistry.EXPO), getValue(FlyableTrackerRegistry.SUPER_RATE), delta);

			physics.rotateX(deltaX);
			physics.rotateY(deltaY);
			physics.rotateZ(deltaZ);

			physics.applyForce(thrust.getForce());
		}
	}

	@Override
	public void updateEulerRotations() {
		Quat4f cameraPitch = physics.getOrientation();
		QuaternionHelper.rotateX(cameraPitch, -getValue(FlyableTrackerRegistry.CAMERA_ANGLE));
		pitch = QuaternionHelper.getPitch(cameraPitch);

		super.updateEulerRotations();
	}

	/**
	 * Gets whether the {@link QuadcopterEntity} can be killed
	 * by conventional means (e.g. punched, rained on, set on fire, etc.)
	 * @return whether or not the {@link QuadcopterEntity} is killable
	 */
	@Override
	public boolean isKillable() {
		return !(getValue(FlyableTrackerRegistry.GOD_MODE) || noClip);
	}

//	/**
//	 * Called whenever a world is saved. Contains {@link CompoundTag} information
//	 * for the {@link QuadcopterEntity} which should persist across restarts of the game.
//	 * @param tag the tag to save to
//	 */
//	@Override
//	protected void writeCustomDataToTag(CompoundTag tag) {
//		super.writeCustomDataToTag(tag);
//		FlyableDataRegistry.getAll(QuadcopterEntity.class, Integer.class).forEach(entry -> tag.putInt(entry.getName(), getValue(entry)));
//		FlyableDataRegistry.getAll(QuadcopterEntity.class, Float.class).forEach(entry -> tag.putFloat(entry.getName(), getValue(entry)));
//	}
//
//	/**
//	 * Called whenever a world is loaded. Contains {@link CompoundTag} information
//	 * for the {@link QuadcopterEntity} which should persist across restarts of the game.
//	 * @param tag the tag to load from
//	 */
//	@Override
//	protected void readCustomDataFromTag(CompoundTag tag) {
//		super.readCustomDataFromTag(tag);
//		FlyableDataRegistry.getAll(QuadcopterEntity.class, Integer.class).forEach(entry -> setValue(entry, tag.getInt(entry.getName())));
//		FlyableDataRegistry.getAll(QuadcopterEntity.class, Float.class).forEach(entry -> setValue(entry, tag.getFloat(entry.getName())));
//	}
//
//	@Override
//	public void writeTagToSpawner(ItemStack itemStack) {
//		super.writeTagToSpawner(itemStack);
//		FlyableDataRegistry.getAll(QuadcopterEntity.class).forEach(entry -> TagHelper.setTagValue(itemStack, entry, getValue(entry.getName())));
//	}
//
//	@Override
//	public void readTagFromSpawner(ItemStack itemStack, PlayerEntity user) {
//		super.readTagFromSpawner(itemStack, user);
//		FlyableDataRegistry.getAll(QuadcopterEntity.class, Integer.class).forEach(entry -> setValue(entry, TagHelper.getTagValue(itemStack, entry)));
//		FlyableDataRegistry.getAll(QuadcopterEntity.class, Float.class).forEach(entry -> setValue(entry, TagHelper.getTagValue(itemStack, entry)));
//	}

	/**
	 * Break the {@link QuadcopterEntity} when it's shot or otherwise damaged in some way.
	 * @param source the source of the damage
	 * @param amount the amount of damage taken
	 * @return
	 */
	@Override
	public boolean damage(DamageSource source, float amount) {
		if (source.getAttacker() instanceof PlayerEntity || (isKillable() && source instanceof ProjectileDamageSource)) {
			this.kill();
			return true;
		}
		return false;
	}

	/**
	 * Called whenever the {@link QuadcopterEntity} is killed. Drops {@link QuadcopterItem} containing tag info.
	 */
	@Override
	public void kill() {
		if (world.getGameRules().getBoolean(GameRules.DO_ENTITY_DROPS)) {
			ItemStack itemStack = new ItemStack(ServerInitializer.DRONE_SPAWNER_ITEM);
			writeTagToSpawner(itemStack);
			dropStack(itemStack);
		}

		remove();
	}

//	/**
//	 * Whenever the {@link DroneEntity} is killed or
//	 * otherwise not supposed to be there, this is called.
//	 */
//	@Override
//	public void remove() {
//		super.remove();
//
//		if (world.isClient()) {
//			if (physics.isActive() && ClientTick.isServerModded) {
//				EntityPhysicsC2S.send(this);
//			}
//		}
//	}

//	@Override
//	protected void initDataTracker() {
//		super.initDataTracker();
//
//		FlyableDataRegistry.getAll(QuadcopterEntity.class, Integer.class).forEach(entry -> this.dataTracker.startTracking(entry.getTrackedData(), entry.getFallback()));
//		FlyableDataRegistry.getAll(QuadcopterEntity.class, Float.class).forEach(entry -> this.dataTracker.startTracking(entry.getTrackedData(), entry.getFallback()));
//		FlyableDataRegistry.getAll(QuadcopterEntity.class, Boolean.class).forEach(entry -> this.dataTracker.startTracking(entry.getTrackedData(), entry.getFallback()));
//	}
}

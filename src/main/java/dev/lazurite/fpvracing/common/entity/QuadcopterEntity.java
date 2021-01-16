package dev.lazurite.fpvracing.common.entity;

import dev.lazurite.fpvracing.client.input.InputFrame;
import dev.lazurite.fpvracing.client.input.InputTick;
import dev.lazurite.fpvracing.FPVRacing;
import dev.lazurite.fpvracing.common.entity.component.VideoTransmitterComponent;
import dev.lazurite.fpvracing.common.item.ChannelWandItem;
import dev.lazurite.fpvracing.common.item.QuadcopterItem;
import dev.lazurite.fpvracing.common.item.TransmitterItem;
import dev.lazurite.fpvracing.common.util.BetaflightHelper;
import dev.lazurite.fpvracing.common.util.Frequency;
import dev.lazurite.rayon.api.packet.RayonSpawnS2CPacket;
import dev.lazurite.rayon.physics.body.EntityRigidBody;
import dev.lazurite.rayon.physics.helper.math.QuaternionHelper;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.ProjectileDamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Packet;
import net.minecraft.text.LiteralText;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import physics.javax.vecmath.Quat4f;

import java.util.Random;

public abstract class QuadcopterEntity extends Entity {
	private static final TrackedData<Boolean> GOD_MODE = DataTracker.registerData(QuadcopterEntity.class, TrackedDataHandlerRegistry.BOOLEAN);

	private float thrust;
	private float thrustCurve;

	public QuadcopterEntity(EntityType<?> type, World world) {
		super(type, world);
	}

	public void step(float delta) {
		EntityRigidBody body = EntityRigidBody.get(entity);
		rotateX((float) BetaflightHelper.calculateRates(getFrame().getX(), getRate(), getExpo(), getSuperRate(), delta));
		rotateY((float) BetaflightHelper.calculateRates(getFrame().getY(), getRate(), getExpo(), getSuperRate(), delta));
		rotateZ((float) BetaflightHelper.calculateRates(getFrame().getZ(), getRate(), getExpo(), getSuperRate(), delta));
//        body.applyForce(thrust.getForce());


	}

	@Override
	public boolean damage(DamageSource source, float amount) {
		if (source.getAttacker() instanceof PlayerEntity || (isKillable() && source instanceof ProjectileDamageSource)) {
			this.kill();
			return true;
		}
		return false;
	}

	@Override
	protected void readCustomDataFromTag(CompoundTag tag) {

	}

	@Override
	protected void writeCustomDataToTag(CompoundTag tag) {

	}

	@Override
	public ActionResult interact(PlayerEntity player, Hand hand) {
		ItemStack stack = player.inventory.getMainHandStack();

		if (stack.getItem() instanceof TransmitterItem) {
			Random rand = new Random();

			// TODO
			bindId = rand.nextInt(10000);

			player.sendMessage(new LiteralText("Transmitter bound"), false);
		} else if (stack.getItem() instanceof ChannelWandItem) {
			Frequency frequency = VideoTransmitterComponent.get(this).getFrequency();
			player.sendMessage(new LiteralText("Frequency: " + frequency.getFrequency() + " (Band: " + frequency.getBand() + " Channel: " + frequency.getChannel() + ")"), false);
		}

		if (world.isClient()) {
			if (!InputTick.controllerExists()) {
				player.sendMessage(new LiteralText("Controller not found"), false);
			}
		}

		return ActionResult.SUCCESS;
	}

	/**
	 * Allows the entity to be seen from far away.
	 * @param distance the distance away from the entity
	 * @return whether or not the entity is outside of the view distance
	 */
	@Override
	@Environment(EnvType.CLIENT)
	public boolean shouldRender(double distance) {
		return true;
	}

	/**
	 * Called whenever the {@link QuadcopterEntity} is killed. Drops {@link QuadcopterItem} containing tag info.
	 */
	@Override
	public void kill() {
		super.kill();

		if (world.getGameRules().getBoolean(GameRules.DO_ENTITY_DROPS)) {
			ItemStack itemStack = new ItemStack(FPVRacing.DRONE_SPAWNER_ITEM);
			dropStack(itemStack);
		}
	}

	@Override
	protected void initDataTracker() {

	}

	@Override
	public Packet<?> createSpawnPacket() {
		return RayonSpawnS2CPacket.get(this);
	}


}

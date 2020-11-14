package dev.lazurite.fpvracing.server.entity;

import com.bulletphysics.dynamics.RigidBody;
import dev.lazurite.fpvracing.client.ClientInitializer;
import dev.lazurite.fpvracing.client.input.InputTick;
import dev.lazurite.fpvracing.network.packet.AmDeadC2S;
import dev.lazurite.fpvracing.network.tracker.Config;
import dev.lazurite.fpvracing.network.tracker.GenericDataTrackerRegistry;
import dev.lazurite.fpvracing.physics.collision.BlockCollisions;
import dev.lazurite.fpvracing.physics.entity.ClientPhysicsHandler;
import dev.lazurite.fpvracing.physics.thrust.Thrust;
import dev.lazurite.fpvracing.server.ServerInitializer;
import dev.lazurite.fpvracing.server.item.ChannelWandItem;
import dev.lazurite.fpvracing.server.item.TransmitterItem;
import dev.lazurite.fpvracing.util.Frequency;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Packet;
import net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.LiteralText;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

import javax.vecmath.Vector3f;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

/**
 * This class is the base class for any flying entity in the FPV racing mod.
 * @author Ethan Johnson
 */
public abstract class FlyableEntity extends PhysicsEntity {
    public static final GenericDataTrackerRegistry.Entry<Integer> BIND_ID = GenericDataTrackerRegistry.register(new Config.Key<>("bindID", ServerInitializer.INTEGER_TYPE), -1, FlyableEntity.class);
    public static final GenericDataTrackerRegistry.Entry<Boolean> GOD_MODE = GenericDataTrackerRegistry.register(new Config.Key<>("godMode", ServerInitializer.BOOLEAN_TYPE), false, FlyableEntity.class);
    public static final GenericDataTrackerRegistry.Entry<Integer> FIELD_OF_VIEW = GenericDataTrackerRegistry.register(new Config.Key<>("fieldOfView", ServerInitializer.INTEGER_TYPE),  120, FlyableEntity.class);
    public static final GenericDataTrackerRegistry.Entry<Frequency> FREQUENCY = GenericDataTrackerRegistry.register(new Config.Key<>("frequency", ServerInitializer.FREQUENCY_TYPE), new Frequency('R', 1), FlyableEntity.class);

    public static final int TRACKING_RANGE = 80;
    public static final int PSEUDO_TRACKING_RANGE = TRACKING_RANGE / 2;

    protected Thrust thrust;

    /**
     * The main constructor. Doesn't do a whole lot.
     * @param type
     * @param world
     */
    public FlyableEntity(EntityType<?> type, World world) {
        super(type, world);
        this.ignoreCameraFrustum = true;
    }

    /**
     * This method is directly related to updating the physics side of things.
     * @param delta delta time
     */
    @Override
    @Environment(EnvType.CLIENT)
    public void step(float delta) {
        super.step(delta);
        calculateBlockDamage();
        decreaseAngularVelocity();

        PlayerEntity player = ClientInitializer.client.player;
        if (player != null) {
            if (TransmitterItem.isBoundTransmitter(player.getMainHandStack(), this)) {
                stepInput(delta);
            }
        }
    }

    /**
     * Used by any subclass of FlyableEntity, this method is for updating the entity
     * based on the user's controller input.
     * @param delta delta time
     */
    @Environment(EnvType.CLIENT)
    public abstract void stepInput(float delta);

    /**
     * Finds all instances of {@link FlyableEntity} within range of the given {@link Entity}.
     * @param origin the {@link Entity} as the origin
     * @return a {@link List} of type {@link FlyableEntity}
     */
    public static List<FlyableEntity> getList(Entity origin, Class<? extends FlyableEntity> type, int r) {
        ServerWorld world = (ServerWorld) origin.getEntityWorld();
        return world.getEntitiesByClass(type, new Box(origin.getBlockPos()).expand(r), EntityPredicates.VALID_ENTITY);
    }

    /**
     * If the {@link PlayerEntity} is holding a {@link TransmitterItem} when they right
     * click on the {@link FlyableEntity}, bind it using a new random ID.
     * @param player the {@link PlayerEntity} who is interacting
     * @param hand the hand of the {@link PlayerEntity}
     * @return
     */
    @Override
    public ActionResult interact(PlayerEntity player, Hand hand) {
        if (!player.world.isClient()) {
            Item handItem = player.inventory.getMainHandStack().getItem();

            if (handItem instanceof TransmitterItem) {
                Random rand = new Random();
                String name = FlyableEntity.BIND_ID.getKey().getName();

                setValue(BIND_ID, rand.nextInt(10000));
                setValue(PLAYER_ID, player.getEntityId());
                System.out.println(getValue(BIND_ID));
                player.getMainHandStack().getOrCreateSubTag(ServerInitializer.MODID)
                        .putInt(name, getValue(BIND_ID));

                player.sendMessage(new LiteralText("Transmitter bound"), false);
            } else if (handItem instanceof ChannelWandItem) {
                int frequency = getValue(FREQUENCY).getFrequency();
                char band = getValue(FREQUENCY).getBand();
                int channel = getValue(FREQUENCY).getChannel();
                player.sendMessage(new LiteralText("Frequency: " + frequency + " (Band: " + band + " Channel: " + channel + ")"), false);
            }
        } else if (!InputTick.controllerExists()) {
            player.sendMessage(new LiteralText("Controller not found"), false);
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
        return distance < Math.pow(ClientInitializer.client.options.viewDistance * 16, 2);
    }

    /**
     * Controls if the flyable entity can be killed (used for god mode).
     * @return whether or not the flyable entity is killable
     */
    public boolean isKillable() {
        return !(getValue(FlyableEntity.GOD_MODE) || noClip);
    }

    /**
     * Writes the tag to the spawner item.
     * @param itemStack the spawner item
     */
    public void writeTagToSpawner(ItemStack itemStack) {
        CompoundTag tag = itemStack.getOrCreateSubTag(ServerInitializer.MODID);
        writeCustomDataToTag(tag);
    }

    /**
     * Reads the tag from the spawner item.
     * @param itemStack the spawner item
     * @param user the player to set the flyable's name to
     */
    public void readTagFromSpawner(ItemStack itemStack, PlayerEntity user) {
        CompoundTag tag = itemStack.getOrCreateSubTag(ServerInitializer.MODID);
        readCustomDataFromTag(tag);
        setValue(PLAYER_ID, user.getEntityId());
    }

    @Override
    public Packet<?> createSpawnPacket() {
        return new EntitySpawnS2CPacket(this);
    }

    @Override
    public void kill() {
        super.kill();

        if (getEntityWorld().isClient()) {
            AmDeadC2S.send(this);
        }
    }

    /**
     * Calculates when block collisions damage the {@link Entity}
     */
    @Environment(EnvType.CLIENT)
    public void calculateBlockDamage() {
        List<Block> damagingBlocks = Arrays.asList(
                Blocks.WATER,
                Blocks.BUBBLE_COLUMN,
                Blocks.LAVA,
                Blocks.FIRE,
                Blocks.SOUL_FIRE,
                Blocks.CAMPFIRE,
                Blocks.SOUL_CAMPFIRE,
                Blocks.CACTUS
        );

        if (isKillable()) {
            // if it's raining
            if (world.hasRain(getBlockPos())) {
                kill();
                return;
            }

            // inside collisions for all damaging blocks
            if (damagingBlocks.contains(world.getBlockState(getBlockPos()).getBlock())) {
                kill();
                return;
            }

            // all direction collisions for certain damaging blocks
            for (Block block : BlockCollisions.getTouchingBlocks(this, Direction.DOWN, Direction.UP, Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST)) {
                if (block.is(Blocks.CACTUS)) {
                    kill();
                    return;
                }
            }

            // downward collisions for certain damaging blocks
            for (Block block : BlockCollisions.getTouchingBlocks(this, Direction.DOWN)) {
                if (block.is(Blocks.MAGMA_BLOCK)) {
                    kill();
                    return;
                }
            }
        }
    }

    /**
     * Decrease the angular velocity of the flyable when it isn't near the ground.
     */
    @Environment(EnvType.CLIENT)
    public void decreaseAngularVelocity() {
        List<RigidBody> bodies = ClientInitializer.physicsWorld.getRigidBodies();
        RigidBody rigidBody = ((ClientPhysicsHandler) getPhysics()).getRigidBody();
        boolean mightCollide = false;
        float t = 0.25f;

        for (RigidBody body : bodies) {
            if (body != rigidBody) {
                Vector3f dist = body.getCenterOfMassPosition(new Vector3f());
                dist.sub(rigidBody.getCenterOfMassPosition(new Vector3f()));

                if (dist.length() < 1.0f) {
                    mightCollide = true;
                    break;
                }
            }
        }

        if (!mightCollide) {
            rigidBody.setAngularVelocity(new Vector3f(0, 0, 0));
        } else {
            float it = 1 - InputTick.axisValues.currT;

            if (Math.abs(InputTick.axisValues.currX) * it > t ||
                    Math.abs(InputTick.axisValues.currY) * it > t ||
                    Math.abs(InputTick.axisValues.currZ) * it > t) {
                rigidBody.setAngularVelocity(new Vector3f(0, 0, 0));
            }
        }
    }
}

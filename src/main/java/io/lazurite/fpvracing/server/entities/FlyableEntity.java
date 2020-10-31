package io.lazurite.fpvracing.server.entities;

import io.lazurite.fpvracing.client.ClientInitializer;
import io.lazurite.fpvracing.client.input.InputTick;
import io.lazurite.fpvracing.network.tracker.GenericDataTrackerRegistry;
import io.lazurite.fpvracing.physics.collisions.BlockCollisions;
import io.lazurite.fpvracing.physics.entity.ClientPhysicsHandler;
import io.lazurite.fpvracing.physics.entity.PhysicsEntity;
import io.lazurite.fpvracing.server.ServerInitializer;
import io.lazurite.fpvracing.server.items.TransmitterItem;
import io.lazurite.fpvracing.util.Frequency;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
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

import java.util.Arrays;
import java.util.List;
import java.util.Random;

public abstract class FlyableEntity extends PhysicsEntity {
    public static final GenericDataTrackerRegistry.Entry<Integer> BIND_ID = GenericDataTrackerRegistry.register("bindID",   -1, ServerInitializer.INTEGER_TYPE, FlyableEntity.class);
    public static final GenericDataTrackerRegistry.Entry<Boolean> GOD_MODE = GenericDataTrackerRegistry.register("godMode", false, ServerInitializer.BOOLEAN_TYPE, FlyableEntity.class);
    public static final GenericDataTrackerRegistry.Entry<Boolean> NO_CLIP = GenericDataTrackerRegistry.register("noClip", false, ServerInitializer.BOOLEAN_TYPE, FlyableEntity.class);
    public static final GenericDataTrackerRegistry.Entry<Integer> FIELD_OF_VIEW = GenericDataTrackerRegistry.register("fieldOfView", 120, ServerInitializer.INTEGER_TYPE, FlyableEntity.class);
    public static final GenericDataTrackerRegistry.Entry<Frequency> FREQUENCY = GenericDataTrackerRegistry.register("frequency", new Frequency('R', 1), ServerInitializer.FREQUENCY_TYPE, FlyableEntity.class);

    public static final int TRACKING_RANGE = 80;
    public static final int PSEUDO_TRACKING_RANGE = TRACKING_RANGE / 2;

    public FlyableEntity(EntityType<?> type, World world) {
        super(type, world);
        this.ignoreCameraFrustum = true;
    }

    @Override
    public void tick() {
        super.tick();

//        updatePosition(getPosition().x, getPosition().y, getPosition().z);
        if (world.isClient()) {
            updateEulerRotations();
        }

//        setSize(getValue(FlyableDataRegistry.SIZE));
//        setMass(getValue(FlyableDataRegistry.MASS));

//        if (playerID != -1 && entity.world.getEntityById(playerID) == null) {
//            entity.kill();
//        }
    }

    @Override
    @Environment(EnvType.CLIENT)
    public void step(ClientPhysicsHandler physics, float delta) {
        super.step(physics, delta);
        calculateBlockDamage();
    }

    /**
     * Finds all instances of {@link FlyableEntity} within range of the given {@link Entity}.
     * @param entity the {@link Entity} as the origin
     * @return a {@link List} of type {@link FlyableEntity}
     */
    public static List<FlyableEntity> getList(Entity entity, int r) {
        ServerWorld world = (ServerWorld) entity.getEntityWorld();
        return world.getEntitiesByClass(FlyableEntity.class, new Box(entity.getBlockPos()).expand(r), EntityPredicates.VALID_ENTITY);
    }

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    protected void writeCustomDataToTag(CompoundTag tag) {
        GenericDataTrackerRegistry.getAll(FlyableEntity.class).forEach(entry -> GenericDataTrackerRegistry.writeToTag(tag, (GenericDataTrackerRegistry.Entry) entry, getValue(entry)));
        GenericDataTrackerRegistry.getAll(getClass()).forEach(entry -> GenericDataTrackerRegistry.writeToTag(tag, (GenericDataTrackerRegistry.Entry) entry, getValue(entry)));
    }

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    protected void readCustomDataFromTag(CompoundTag tag) {
        GenericDataTrackerRegistry.getAll(FlyableEntity.class).forEach(entry -> setValue((GenericDataTrackerRegistry.Entry) entry, GenericDataTrackerRegistry.readFromTag(tag, entry)));
        GenericDataTrackerRegistry.getAll(getClass()).forEach(entry -> setValue((GenericDataTrackerRegistry.Entry) entry, GenericDataTrackerRegistry.readFromTag(tag, entry)));
    }

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    protected void initDataTracker() {
        GenericDataTrackerRegistry.getAll().forEach(entry -> getDataTracker().startTracking(((GenericDataTrackerRegistry.Entry) entry).getTrackedData(), entry.getFallback()));
    }

    public void writeTagToSpawner(ItemStack itemStack) {
        CompoundTag tag = itemStack.getSubTag(ServerInitializer.MODID);
        writeCustomDataToTag(tag);
    }

    public void readTagFromSpawner(ItemStack itemStack, PlayerEntity user) {
        CompoundTag tag = itemStack.getSubTag(ServerInitializer.MODID);
        readCustomDataFromTag(tag);
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
            if (player.inventory.getMainHandStack().getItem() instanceof TransmitterItem) {
                Random rand = new Random();
                setValue(BIND_ID, rand.nextInt());
                setPlayerID(player.getEntityId());
                player.getMainHandStack().getOrCreateSubTag(ServerInitializer.MODID)
                        .putInt(BIND_ID.getName(), getValue(BIND_ID));
                player.sendMessage(new LiteralText("Transmitter bound"), false);
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

    @Override
    public Packet<?> createSpawnPacket() {
        return new EntitySpawnS2CPacket(this);
    }

//    @Override
//    public boolean isGlowing() {
//        return false;
//    }

    public boolean isKillable() {
        return false;
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
}

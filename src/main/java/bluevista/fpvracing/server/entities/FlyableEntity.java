package bluevista.fpvracing.server.entities;

import bluevista.fpvracing.client.ClientInitializer;
import bluevista.fpvracing.client.input.InputTick;
import bluevista.fpvracing.config.Config;
import bluevista.fpvracing.network.FlyableDataHandlerRegistry;
import bluevista.fpvracing.network.entity.EntityPhysicsS2C;
import bluevista.fpvracing.physics.Air;
import bluevista.fpvracing.physics.entity.ClientEntityPhysics;
import bluevista.fpvracing.physics.entity.IEntityPhysics;
import bluevista.fpvracing.physics.entity.ServerEntityPhysics;
import bluevista.fpvracing.server.items.TransmitterItem;
import bluevista.fpvracing.util.Frequency;
import bluevista.fpvracing.util.math.QuaternionHelper;
import com.bulletphysics.dynamics.RigidBody;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Packet;
import net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.LiteralText;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Box;
import net.minecraft.world.World;

import javax.vecmath.Vector3f;
import java.util.List;
import java.util.Random;

public abstract class FlyableEntity extends Entity {
    public static final int TRACKING_RANGE = 80;
    public static final int PSEUDO_TRACKING_RANGE = TRACKING_RANGE / 2;

    public static final TrackedData<Integer> BIND_ID = DataTracker.registerData(FlyableEntity.class, TrackedDataHandlerRegistry.INTEGER);
    public static final TrackedData<Integer> PLAYER_ID = DataTracker.registerData(FlyableEntity.class, TrackedDataHandlerRegistry.INTEGER);
    public static final TrackedData<Boolean> GOD_MODE = DataTracker.registerData(FlyableEntity.class, TrackedDataHandlerRegistry.BOOLEAN);

    public static final TrackedData<Float> FIELD_OF_VIEW = DataTracker.registerData(FlyableEntity.class, TrackedDataHandlerRegistry.FLOAT);
    public static final TrackedData<Frequency> FREQUENCY = DataTracker.registerData(FlyableEntity.class, FlyableDataHandlerRegistry.FREQUENCY);

    public static final TrackedData<Float> MASS = DataTracker.registerData(FlyableEntity.class, TrackedDataHandlerRegistry.FLOAT);
    public static final TrackedData<Integer> SIZE = DataTracker.registerData(QuadcopterEntity.class, TrackedDataHandlerRegistry.INTEGER);
    public static final TrackedData<Float> DRAG_COEFFICIENT = DataTracker.registerData(FlyableEntity.class, TrackedDataHandlerRegistry.FLOAT);

    public final IEntityPhysics physics;

    public FlyableEntity(EntityType<?> type, World world) {
        super(type, world);
        this.ignoreCameraFrustum = true;

        if (world.isClient()) {
            physics = new ClientEntityPhysics(this);
        } else {
            physics = new ServerEntityPhysics(this);
        }
    }

    @Override
    public void tick() {
        super.tick();

//        updatePosition(getPosition().x, getPosition().y, getPosition().z);
        updateEulerRotations();

//        if (playerID != -1 && entity.world.getEntityById(playerID) == null) {
//            entity.kill();
//        }

//        physics.tick();
    }

    @Environment(EnvType.CLIENT)
    public void step(float delta) {
        Vector3f direction = physics.getLinearVelocity();
        direction.normalize();
        ((ClientEntityPhysics) physics).applyForce(Air.getResistanceForce(
                direction,
                this.dataTracker.get(SIZE),
                this.dataTracker.get(DRAG_COEFFICIENT)
        ));
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

    /**
     * Sets the mass of the drone. Also refreshes the {@link RigidBody}.
     * @param mass the new mass
     */
    public void setMass(float mass) {
        float old = this.dataTracker.get(MASS);
        this.dataTracker.set(MASS, mass);

        if (old != mass && world.isClient()) {
            ((ClientEntityPhysics) physics).createRigidBody();
        }
    }

    public float getMass() {
        return this.dataTracker.get(MASS);
    }

    /**
     * Sets the size of the drone. Also refreshes the {@link RigidBody}.
     * @param size the new size
     */
    public void setSize(int size) {
        float old = this.dataTracker.get(SIZE);
        this.dataTracker.set(SIZE, size);

        if (old != size && world.isClient()) {
            ((ClientEntityPhysics) physics).createRigidBody();
        }
    }

    public int getSize() {
        return this.dataTracker.get(SIZE);
    }

    public void setPlayerID(int playerID) {
        this.dataTracker.set(PLAYER_ID, playerID);

        PlayerEntity player = (PlayerEntity) getEntityWorld().getEntityById(playerID);
        if (player != null) {
            setCustomName(new LiteralText(player.getGameProfile().getName()));
            setCustomNameVisible(true);
        }
    }

    public int getPlayerID() {
        return this.dataTracker.get(PLAYER_ID);
    }

    public void updatePositionAndAngles(Vector3f position, float yaw, float pitch) {
        this.updatePositionAndAngles(position.x, position.y, position.z, yaw, pitch);
        physics.setPosition(position);
        setYaw(yaw);

        if (!world.isClient()) {
            EntityPhysicsS2C.send(physics, true);
        }
    }

    @Override
    public void setYaw(float yaw) {
        if (world.isClient()) {
            ((ClientEntityPhysics) physics).rotateY(yaw);
        }

        this.prevYaw = this.yaw;
        this.yaw = yaw;
    }

    public void updateEulerRotations() {
        prevYaw = yaw;
        yaw = QuaternionHelper.getYaw(physics.getOrientation());

        while(yaw - prevYaw < -180.0F) {
            prevYaw -= 360.0F;
        }

        while(yaw - prevYaw >= 180.0F) {
            prevYaw += 360.0F;
        }
    }

    @Override
    protected void writeCustomDataToTag(CompoundTag tag) {
        tag.putInt("bind_id", this.dataTracker.get(BIND_ID));
        tag.putInt("player_id", this.dataTracker.get(PLAYER_ID));
        tag.putBoolean("god_mode", this.dataTracker.get(GOD_MODE));

        tag.putFloat("field_of_view", this.dataTracker.get(FIELD_OF_VIEW));
        this.dataTracker.get(FREQUENCY).toTag(tag);

        tag.putFloat("mass", this.dataTracker.get(MASS));
        tag.putInt("size", this.dataTracker.get(SIZE));
        tag.putFloat("drag_coefficient", this.dataTracker.get(DRAG_COEFFICIENT));
    }

    @Override
    protected void readCustomDataFromTag(CompoundTag tag) {
        this.dataTracker.set(BIND_ID, tag.getInt("bind_id"));
        this.dataTracker.set(PLAYER_ID, tag.getInt("player_id"));
        this.dataTracker.set(GOD_MODE, tag.getBoolean("god_mode"));

        this.dataTracker.set(FIELD_OF_VIEW, tag.getFloat("field_of_view"));
        this.dataTracker.set(FREQUENCY, Frequency.fromTag(tag));

        this.dataTracker.set(MASS, tag.getFloat("mass"));
        this.dataTracker.set(SIZE, tag.getInt("size"));
        this.dataTracker.set(DRAG_COEFFICIENT, tag.getFloat("drag_coefficient"));
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
                this.dataTracker.set(BIND_ID, rand.nextInt());

                setPlayerID(player.getEntityId());

                TransmitterItem.setTagValue(player.getMainHandStack(), Config.BIND, this.dataTracker.get(BIND_ID));
                player.sendMessage(new LiteralText("Transmitter bound"), false);
            }
        } else if (!InputTick.controllerExists()) {
            player.sendMessage(new TranslatableText("Controller not found"), false);
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

    @Override
    protected void initDataTracker() {
        this.dataTracker.startTracking(BIND_ID, -1);
        this.dataTracker.startTracking(PLAYER_ID, -1);
        this.dataTracker.startTracking(GOD_MODE, false);

        this.dataTracker.startTracking(FIELD_OF_VIEW, 120.0f);
        this.dataTracker.startTracking(FREQUENCY, new Frequency('R', 1));

        this.dataTracker.startTracking(MASS, 0.0f);
        this.dataTracker.startTracking(SIZE, 0);
        this.dataTracker.startTracking(DRAG_COEFFICIENT, 0.0f);
    }

    public IEntityPhysics getPhysics() {
        return this.physics;
    }

    public boolean isKillable() {
        return false;
    }
}

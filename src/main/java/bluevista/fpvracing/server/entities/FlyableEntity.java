package bluevista.fpvracing.server.entities;

import bluevista.fpvracing.client.ClientInitializer;
import bluevista.fpvracing.client.input.InputTick;
import bluevista.fpvracing.config.Config;
import bluevista.fpvracing.network.packets.EntityPhysicsS2C;
import bluevista.fpvracing.network.datatracker.FlyableTrackerRegistry;
import bluevista.fpvracing.physics.Air;
import bluevista.fpvracing.physics.entity.ClientEntityPhysics;
import bluevista.fpvracing.physics.entity.IEntityPhysics;
import bluevista.fpvracing.physics.entity.ServerEntityPhysics;
import bluevista.fpvracing.server.ServerInitializer;
import bluevista.fpvracing.server.items.TransmitterItem;
import bluevista.fpvracing.util.math.QuaternionHelper;
import com.bulletphysics.dynamics.RigidBody;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandler;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Packet;
import net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.server.network.ServerPlayerInteractionManager;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.LiteralText;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Box;
import net.minecraft.world.World;

import javax.vecmath.Vector3f;
import java.util.List;
import java.util.Random;
import java.util.UUID;

public abstract class FlyableEntity extends Entity {
    public static final int TRACKING_RANGE = 80;
    public static final int PSEUDO_TRACKING_RANGE = TRACKING_RANGE / 2;
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
        if (world.isClient()) {
            updateEulerRotations();
        }

//        setSize(getValue(FlyableDataRegistry.SIZE));
//        setMass(getValue(FlyableDataRegistry.MASS));

//        if (playerID != -1 && entity.world.getEntityById(playerID) == null) {
//            entity.kill();
//        }
    }

    @Environment(EnvType.CLIENT)
    public void step(float delta) {
        Vector3f direction = physics.getLinearVelocity();
        direction.normalize();
        ((ClientEntityPhysics) physics).applyForce(Air.getResistanceForce(
                direction,
                getValue(FlyableTrackerRegistry.SIZE),
                getValue(FlyableTrackerRegistry.DRAG_COEFFICIENT)
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

    public <T> void setValue(FlyableTrackerRegistry.Entry<T> entry, T value) {
        this.getDataTracker().set(entry.getTrackedData(), value);
    }

    public <T> T getValue(FlyableTrackerRegistry.Entry<T> entry) {
        T data = getDataTracker().get(entry.getTrackedData());

        if (data == null) {
            return entry.getFallback();
        }

        return data;
    }

    /**
     * Sets the mass of the drone. Also refreshes the {@link RigidBody}.
     * @param mass the new mass
     */
    public void setMass(float mass) {
        float old = getValue(FlyableTrackerRegistry.MASS);
        setValue(FlyableTrackerRegistry.MASS, mass);

        if (old != mass && world.isClient()) {
            ((ClientEntityPhysics) physics).createRigidBody();
        }
    }

    /**
     * Sets the size of the drone. Also refreshes the {@link RigidBody}.
     * @param size the new size
     */
    public void setSize(int size) {
        float old = getValue(FlyableTrackerRegistry.SIZE);
        setValue(FlyableTrackerRegistry.SIZE, size);

        if (old != size && world.isClient()) {
            ((ClientEntityPhysics) physics).createRigidBody();
        }
    }

    public void setPlayerID(int playerID) {
        setValue(FlyableTrackerRegistry.PLAYER_ID, playerID);

        PlayerEntity player = (PlayerEntity) getEntityWorld().getEntityById(playerID);
        if (player != null) {
            setCustomName(new LiteralText(player.getGameProfile().getName()));
            setCustomNameVisible(true);
        }
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
    @SuppressWarnings({"unchecked", "rawtypes"})
    protected void writeCustomDataToTag(CompoundTag tag) {
        FlyableTrackerRegistry.getAll(FlyableEntity.class).forEach(entry -> FlyableTrackerRegistry.writeToTag(tag, (FlyableTrackerRegistry.Entry) entry, getValue(entry)));
        FlyableTrackerRegistry.getAll(getClass()).forEach(entry -> FlyableTrackerRegistry.writeToTag(tag, (FlyableTrackerRegistry.Entry) entry, getValue(entry)));
    }

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    protected void readCustomDataFromTag(CompoundTag tag) {
        FlyableTrackerRegistry.getAll(FlyableEntity.class).forEach(entry -> setValue((FlyableTrackerRegistry.Entry) entry, FlyableTrackerRegistry.readFromTag(tag, entry)));
        FlyableTrackerRegistry.getAll(getClass()).forEach(entry -> setValue((FlyableTrackerRegistry.Entry) entry, FlyableTrackerRegistry.readFromTag(tag, entry)));
    }

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    protected void initDataTracker() {
        FlyableTrackerRegistry.getAll().forEach(entry -> getDataTracker().startTracking(((FlyableTrackerRegistry.Entry) entry).getTrackedData(), entry.getFallback()));
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
                setValue(FlyableTrackerRegistry.BIND_ID, rand.nextInt());
                setPlayerID(player.getEntityId());
                player.getMainHandStack().getOrCreateSubTag(ServerInitializer.MODID)
                        .putInt(FlyableTrackerRegistry.BIND_ID.getName(), getValue(FlyableTrackerRegistry.BIND_ID));
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

    public IEntityPhysics getPhysics() {
        return this.physics;
    }

    public boolean isKillable() {
        return false;
    }
}

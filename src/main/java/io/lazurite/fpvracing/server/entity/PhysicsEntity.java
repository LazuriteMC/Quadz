package io.lazurite.fpvracing.server.entity;

import com.bulletphysics.dynamics.RigidBody;
import io.lazurite.fpvracing.network.packet.PhysicsHandlerC2S;
import io.lazurite.fpvracing.network.tracker.Config;
import io.lazurite.fpvracing.network.tracker.GenericDataTrackerRegistry;
import io.lazurite.fpvracing.network.packet.PhysicsHandlerS2C;
import io.lazurite.fpvracing.physics.Air;
import io.lazurite.fpvracing.physics.entity.ClientPhysicsHandler;
import io.lazurite.fpvracing.physics.entity.PhysicsHandler;
import io.lazurite.fpvracing.physics.entity.ServerPhysicsHandler;
import io.lazurite.fpvracing.server.ServerInitializer;
import io.lazurite.fpvracing.util.math.QuaternionHelper;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.world.World;

import javax.vecmath.Vector3f;

public abstract class PhysicsEntity extends NetworkSyncedEntity {
    public static final GenericDataTrackerRegistry.Entry<Integer> PLAYER_ID = GenericDataTrackerRegistry.register(new Config.Key<>("playerId", ServerInitializer.INTEGER_TYPE), -1, PhysicsEntity.class, (entity, value) -> ((PhysicsEntity) entity).setPlayerID(value));
    public static final GenericDataTrackerRegistry.Entry<Integer> SIZE = GenericDataTrackerRegistry.register(new Config.Key<>("size", ServerInitializer.INTEGER_TYPE), 8, PhysicsEntity.class, (entity, value) -> ((PhysicsEntity) entity).setSize(value));
    public static final GenericDataTrackerRegistry.Entry<Float> MASS = GenericDataTrackerRegistry.register(new Config.Key<>("mass", ServerInitializer.FLOAT_TYPE), 10.0f, PhysicsEntity.class, (entity, value) -> ((PhysicsEntity) entity).setMass(value));
    public static final GenericDataTrackerRegistry.Entry<Float> DRAG_COEFFICIENT = GenericDataTrackerRegistry.register(new Config.Key<>("dragCoefficient", ServerInitializer.FLOAT_TYPE), 0.5F, PhysicsEntity.class);

    protected PhysicsHandler physics;

    private int prevSize;
    private float prevMass;

    public PhysicsEntity(EntityType<?> type, World world) {
        super(type, world);
        this.physics = createPhysicsHandler(this);
    }

    public static PhysicsHandler createPhysicsHandler(PhysicsEntity entity) {
        if (entity.getEntityWorld().isClient()) {
            return new ClientPhysicsHandler(entity);
        } else {
            return new ServerPhysicsHandler(entity);
        }
    }

    @Override
    public void tick() {
        super.tick();
        updatePosition();
        updateEulerRotations();

        if (getValue(PLAYER_ID) != -1 && getEntityWorld().getEntityById(getValue(PLAYER_ID)) == null) {
            kill();
        }
    }

    @Environment(EnvType.CLIENT)
    public void step(ClientPhysicsHandler physics, float delta) {
        Vector3f direction = physics.getLinearVelocity();
        direction.normalize();
        physics.applyForce(Air.getResistanceForce(
                direction,
                getValue(SIZE),
                getValue(DRAG_COEFFICIENT)
        ));
    }

    @Override
    public void sendNetworkPacket() {
        if (getEntityWorld().isClient()) {
            ClientPhysicsHandler physics = (ClientPhysicsHandler) getPhysics();

            if (physics.isActive()) {
                PhysicsHandlerC2S.send(physics);
            } else {
                physics.setPrevOrientation(physics.getOrientation());
                physics.setOrientation(physics.getNetOrientation());
            }
        } else {
            PhysicsHandlerS2C.send(getPhysics(), false);
        }
    }

    public PhysicsHandler getPhysics() {
        return physics;
    }

    /**
     * Sets the mass of the physics entity. Also refreshes the {@link RigidBody}.
     * @param mass the new mass
     */
    public void setMass(float mass) {
        if (prevMass != mass && world.isClient()) {
            ((ClientPhysicsHandler) physics).createRigidBody();
        }

        prevMass = mass;
    }

    /**
     * Sets the size of the physics entity. Also refreshes the {@link RigidBody}.
     * @param size the new size
     */
    public void setSize(int size) {
        if (prevSize != size && world.isClient()) {
            ((ClientPhysicsHandler) physics).createRigidBody();
        }

        prevSize = size;
    }

    /**
     * Sets the player ID used by the physics entity.
     * @param playerID the player ID
     */
    public void setPlayerID(int playerID) {
        System.out.println("PlayerID: " + playerID);
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
            PhysicsHandlerS2C.send(physics, true);
        }
    }

    public void updatePosition() {
        updatePosition(
                getPhysics().getPosition().x,
                getPhysics().getPosition().y,
                getPhysics().getPosition().z
        );
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
    public void setYaw(float yaw) {
        if (world.isClient()) {
            ((ClientPhysicsHandler) physics).rotateY(yaw);
        }

        this.prevYaw = this.yaw;
        this.yaw = yaw;
    }
}

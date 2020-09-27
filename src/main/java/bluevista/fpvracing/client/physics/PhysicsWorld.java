package bluevista.fpvracing.client.physics;

import bluevista.fpvracing.client.ClientInitializer;
import bluevista.fpvracing.config.Config;
import bluevista.fpvracing.server.entities.DroneEntity;
import com.bulletphysics.collision.broadphase.BroadphaseInterface;
import com.bulletphysics.collision.broadphase.DbvtBroadphase;
import com.bulletphysics.collision.dispatch.CollisionConfiguration;
import com.bulletphysics.collision.dispatch.CollisionDispatcher;
import com.bulletphysics.collision.dispatch.DefaultCollisionConfiguration;
import com.bulletphysics.collision.shapes.BoxShape;
import com.bulletphysics.collision.shapes.CollisionShape;
import com.bulletphysics.dynamics.DiscreteDynamicsWorld;
import com.bulletphysics.dynamics.RigidBody;
import com.bulletphysics.dynamics.RigidBodyConstructionInfo;
import com.bulletphysics.dynamics.constraintsolver.SequentialImpulseConstraintSolver;
import com.bulletphysics.linearmath.Clock;
import com.bulletphysics.linearmath.DefaultMotionState;
import com.bulletphysics.linearmath.Transform;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.*;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;

import javax.vecmath.Matrix4f;
import javax.vecmath.Quat4f;
import javax.vecmath.Vector3f;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Environment(EnvType.CLIENT)
public class PhysicsWorld {
    public float gravity;
    public float airDensity;
    public int blockRadius;

    public final Clock clock;
    public final List<DroneEntity> droneEntities;
    public final Map<Entity, RigidBody> collisionEntities;
    public final Map<BlockPos, RigidBody> collisionBlocks;

    private final DiscreteDynamicsWorld dynamicsWorld;
    private final List<BlockPos> toKeepBlocks;
    private final List<Entity> toKeepEntities;

    public PhysicsWorld() {
        this.droneEntities = new ArrayList<>();
        this.collisionEntities = new HashMap<>();
        this.collisionBlocks = new HashMap<>();
        this.toKeepBlocks = new ArrayList<>();
        this.toKeepEntities = new ArrayList<>();
        this.clock = new Clock();

        blockRadius = ClientInitializer.getConfig().getIntOption(Config.BLOCK_RADIUS);
        gravity = ClientInitializer.getConfig().getFloatOption(Config.GRAVITY);
        airDensity = ClientInitializer.getConfig().getFloatOption(Config.AIR_DENSITY);

        BroadphaseInterface broadphase = new DbvtBroadphase();
        CollisionConfiguration collisionConfiguration = new DefaultCollisionConfiguration();
        CollisionDispatcher dispatcher = new CollisionDispatcher(collisionConfiguration);
        SequentialImpulseConstraintSolver solver = new SequentialImpulseConstraintSolver();
        dynamicsWorld = new DiscreteDynamicsWorld(dispatcher, broadphase, solver, collisionConfiguration);
        dynamicsWorld.setGravity(new Vector3f(0, gravity, 0));
    }

    public void stepWorld() {
        ClientWorld world = ClientInitializer.client.world;

        float d = clock.getTimeMicroseconds() / 1000000F;
        float maxSubSteps = 5.0f;
        clock.reset();

        List<DroneEntity> toRemove = new ArrayList<>();

        this.droneEntities.forEach(drone -> {
            if (drone.removed) {
                toRemove.add(drone);
            }

            if (world != null) {
                if (drone.isActive()) {
                    if (drone.getConfigValues(Config.NO_CLIP).equals(0)) {
                        loadBlockCollisions(drone, world);
                    }

                    if (!drone.getRigidBody().isInWorld()) {
                        this.dynamicsWorld.addRigidBody(drone.getRigidBody());
                    }
                } else if (drone.getRigidBody().isInWorld()) {
                    this.dynamicsWorld.removeRigidBody(drone.getRigidBody());
                }

                drone.stepPhysics(d);
            }
        });

        unloadBlockCollisions();
        toRemove.forEach(droneEntities::remove);
        this.dynamicsWorld.stepSimulation(d, (int) maxSubSteps, d/maxSubSteps);
    }

    public void loadBlockCollisions(DroneEntity drone, ClientWorld world) {
        Box area = new Box(new BlockPos(drone.getPos())).expand(blockRadius);
        Map<BlockPos, BlockState> blockList = BlockHelper.getBlockList(world, area);
        BlockView blockView = world.getChunkManager().getChunk(drone.chunkX, drone.chunkZ);

        blockList.forEach((blockPos, blockState) -> {
            if (!blockState.getBlock().canMobSpawnInside()) {
                if (!this.collisionBlocks.containsKey(blockPos)) {
                    VoxelShape coll = blockState.getCollisionShape(blockView, blockPos);

                    if (!coll.isEmpty()) {
                        Box b = coll.getBoundingBox();

                        Vector3f box = new Vector3f(
                                ((float) (b.maxX - b.minX) / 2.0F) + 0.005f,
                                ((float) (b.maxY - b.minY) / 2.0F) + 0.005f,
                                ((float) (b.maxZ - b.minZ) / 2.0F) + 0.005f);
                        CollisionShape shape = new BoxShape(box);

                        Vector3f position = new Vector3f(blockPos.getX() + box.x, blockPos.getY() + box.y, blockPos.getZ() + box.z);
                        DefaultMotionState motionState = new DefaultMotionState(new Transform(new Matrix4f(new Quat4f(), position, 1.0f)));
                        RigidBodyConstructionInfo ci = new RigidBodyConstructionInfo(0, motionState, shape, new Vector3f(0, 0, 0));
                        RigidBody body = new RigidBody(ci);

                        if (blockState.getBlock() instanceof IceBlock) {
                            body.setFriction(0.05f);
                        } else if (
                                blockState.getBlock() instanceof HoneyBlock ||
                                blockState.getBlock() instanceof SlimeBlock ||
                                blockState.getBlock() instanceof SoulSandBlock
                        ) {
                            body.setFriction(1.5f);
                        } else {
                            body.setFriction(0.9f);
                        }

                        this.collisionBlocks.put(blockPos, body);
                        this.dynamicsWorld.addRigidBody(body);
                    }
                }

                this.toKeepBlocks.add(blockPos);
            }
        });
    }

    public void unloadBlockCollisions() {
        List<BlockPos> toRemove = new ArrayList();

        this.collisionBlocks.forEach((pos, body) -> {
            if (!toKeepBlocks.contains(pos)) {
                dynamicsWorld.removeRigidBody(collisionBlocks.get(pos));
                toRemove.add(pos);
            }
        });

        toRemove.forEach(this.collisionBlocks::remove);
        toKeepBlocks.clear();
    }

    public void loadEntityCollisions(DroneEntity drone, ClientWorld world) {
        Box area = new Box(new BlockPos(drone.getPos())).expand(blockRadius);

        world.getOtherEntities(drone, area).forEach(entity -> {
            if (!(entity instanceof DroneEntity) && !collisionEntities.containsKey(entity)) {
                Box c = entity.getBoundingBox();
                Vector3f box = new Vector3f(
                        ((float) (c.maxX - c.minX) / 2.0F),
                        ((float) (c.maxY - c.minY) / 2.0F),
                        ((float) (c.maxZ - c.minZ) / 2.0F));
                CollisionShape shape = new BoxShape(box);

                Vec3d pos = entity.getPos();
                Vector3f position = new Vector3f((float) pos.x, (float) pos.y, (float) pos.z);

                DefaultMotionState motionState = new DefaultMotionState(new Transform(new Matrix4f(new Quat4f(0, 1, 0, 0), position, 1.0f)));
                RigidBodyConstructionInfo ci = new RigidBodyConstructionInfo(0, motionState, shape, new Vector3f(0, 0, 0));
                RigidBody body = new RigidBody(ci);

                this.collisionEntities.put(entity, body);
                this.dynamicsWorld.addRigidBody(body);
            }

            toKeepEntities.add(entity);
        });
    }

    public void unloadEntityCollisions() {
        List<Entity> toRemove = new ArrayList();

        this.collisionEntities.forEach((entity, body) -> {
            if (!toKeepEntities.contains(entity)) {
                dynamicsWorld.removeRigidBody(collisionEntities.get(entity));
                toRemove.add(entity);
            }
        });

        toRemove.forEach(this.collisionEntities::remove);
        toKeepEntities.clear();
    }

    public void setConfigValues(String key, Number value) {
        switch (key) {
            case Config.GRAVITY:
                this.gravity = value.floatValue();
                this.dynamicsWorld.setGravity(new Vector3f(0, this.gravity, 0));
                break;
            case Config.AIR_DENSITY:
                this.airDensity = value.floatValue();
                break;
            case Config.BLOCK_RADIUS:
                this.blockRadius = value.intValue();
                break;
            default:
                break;
        }
    }

    public Number getConfigValues(String key) {
        switch (key) {
            case Config.GRAVITY:
                return this.gravity;
            case Config.AIR_DENSITY:
                return this.airDensity;
            case Config.BLOCK_RADIUS:
                return this.blockRadius;
            default:
                return null;
        }
    }

    public void add(DroneEntity drone) {
        this.droneEntities.add(drone);
    }

    public void remove(DroneEntity drone) {
        this.dynamicsWorld.removeRigidBody(drone.getRigidBody());
        this.droneEntities.remove(drone);
    }

    public void addRigidBody(RigidBody body) {
        this.dynamicsWorld.addRigidBody(body);
    }

    public void removeRigidBody(RigidBody body) {
        this.dynamicsWorld.removeRigidBody(body);
    }

    public List<RigidBody> getRigidBodies() {
        List<RigidBody> bodies = new ArrayList();

        droneEntities.forEach(drone -> bodies.add(drone.getRigidBody()));
        bodies.addAll(collisionEntities.values());
        bodies.addAll(collisionBlocks.values());

        return bodies;
    }

    public DiscreteDynamicsWorld getDynamicsWorld() {
        return this.dynamicsWorld;
    }
}

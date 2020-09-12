package bluevista.fpvracingmod.client.physics;

import bluevista.fpvracingmod.client.ClientInitializer;
import bluevista.fpvracingmod.server.entities.PhysicsEntity;
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
import net.minecraft.block.BlockState;
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
    public final float GRAVITY;
    public final int LOAD_AREA;

    public final Clock clock;
    public final List<PhysicsEntity> physicsEntities;
    public final Map<Entity, RigidBody> collisionEntities;
    public final Map<BlockPos, RigidBody> collisionBlocks;

    private final DiscreteDynamicsWorld dynamicsWorld;
    private final List<BlockPos> toKeepBlocks;
    private final List<Entity> toKeepEntities;

    public PhysicsWorld() {
        this.physicsEntities = new ArrayList();
        this.collisionEntities = new HashMap();
        this.collisionBlocks = new HashMap();
        this.toKeepBlocks = new ArrayList();
        this.toKeepEntities = new ArrayList();
        this.clock = new Clock();

        LOAD_AREA = 3; // Get from config
        GRAVITY = -9.81f; // Get from config

        // Create the world
        BroadphaseInterface broadphase = new DbvtBroadphase();
        CollisionConfiguration collisionConfiguration = new DefaultCollisionConfiguration();
        CollisionDispatcher dispatcher = new CollisionDispatcher(collisionConfiguration);
        SequentialImpulseConstraintSolver solver = new SequentialImpulseConstraintSolver();
        dynamicsWorld = new DiscreteDynamicsWorld(dispatcher, broadphase, solver, collisionConfiguration);
        dynamicsWorld.setGravity(new Vector3f(0, GRAVITY, 0));
    }

    public void stepWorld() {
        ClientWorld world = ClientInitializer.client.world;

        float d = clock.getTimeMicroseconds() / 1000000F;
        float maxSubSteps = 5.0f;
        clock.reset();

        loadActivePhysicsEntities();
        this.physicsEntities.forEach(physics -> {
            if(world != null) {
                if(physics.isActive()) {
                    loadEntityCollisions(physics, world);
                    loadBlockCollisions(physics, world);
                }

                physics.stepPhysics(d);
            }
        });

        unloadEntityCollisions();
        unloadBlockCollisions();

        this.dynamicsWorld.stepSimulation(d, (int) maxSubSteps, d/maxSubSteps);
    }

    public void loadActivePhysicsEntities() {
        this.physicsEntities.forEach(physics -> {
            if (physics.isActive()) {
                if (!physics.getRigidBody().isInWorld()) {
                    this.dynamicsWorld.addRigidBody(physics.getRigidBody());
                }
            } else {
                if (physics.getRigidBody().isInWorld()) {
                    this.dynamicsWorld.removeRigidBody(physics.getRigidBody());
                }
            }
        });
    }

    public void loadBlockCollisions(PhysicsEntity physics, ClientWorld world) {
        Box area = new Box(physics.getRigidBodyBlockPos()).expand(LOAD_AREA);
        Map<BlockPos, BlockState> blockList = BlockHelper.getBlockList(world, area);
        BlockView blockView = world.getChunkManager().getChunk(physics.chunkX, physics.chunkZ);

        blockList.forEach((blockPos, blockState) -> {
            if(!blockState.getBlock().canMobSpawnInside()) {
                if(!this.collisionBlocks.containsKey(blockPos)) {
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
            if(!toKeepBlocks.contains(pos)) {
                dynamicsWorld.removeRigidBody(collisionBlocks.get(pos));
                toRemove.add(pos);
            }
        });

        toRemove.forEach(this.collisionBlocks::remove);
        toKeepBlocks.clear();
    }

    public void loadEntityCollisions(PhysicsEntity physics, ClientWorld world) {
        Box area = new Box(physics.getRigidBodyBlockPos()).expand(LOAD_AREA);

        world.getOtherEntities(physics, area).forEach(entity -> {
            if(!(entity instanceof PhysicsEntity) && !collisionEntities.containsKey(entity)) {
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
            if(!toKeepEntities.contains(entity)) {
                dynamicsWorld.removeRigidBody(collisionEntities.get(entity));
                toRemove.add(entity);
            }
        });

        toRemove.forEach(this.collisionEntities::remove);
        toKeepEntities.clear();
    }

    public void add(PhysicsEntity physics) {
        this.physicsEntities.add(physics);
    }

    public void remove(PhysicsEntity physics) {
        this.dynamicsWorld.removeRigidBody(physics.getRigidBody());
        this.physicsEntities.remove(physics);
    }

    public List<RigidBody> getRigidBodies() {
        List<RigidBody> bodies = new ArrayList();

        physicsEntities.forEach(physics -> bodies.add(physics.getRigidBody()));
        bodies.addAll(collisionEntities.values());
        bodies.addAll(collisionBlocks.values());

        return bodies;
    }
}

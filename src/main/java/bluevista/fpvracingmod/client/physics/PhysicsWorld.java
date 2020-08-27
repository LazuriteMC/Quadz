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
    public static final float DEFAULT_GRAVITY = -9.81f;
    public static final int LOAD_AREA = 2;

    public final List<PhysicsEntity> physicsEntities;
    public final Map<Entity, RigidBody> collisionEntities;
    public final Map<BlockPos, RigidBody> collisionBlocks;

    private final DiscreteDynamicsWorld dynamicsWorld;
    private final Clock clock;

    public PhysicsWorld() {
        this.physicsEntities = new ArrayList();
        this.collisionEntities = new HashMap();
        this.collisionBlocks = new HashMap();
        this.clock = new Clock();

        // Create the world
        BroadphaseInterface broadphase = new DbvtBroadphase();
        CollisionConfiguration collisionConfiguration = new DefaultCollisionConfiguration();
        CollisionDispatcher dispatcher = new CollisionDispatcher(collisionConfiguration);
        SequentialImpulseConstraintSolver solver = new SequentialImpulseConstraintSolver();
        dynamicsWorld = new DiscreteDynamicsWorld(dispatcher, broadphase, solver, collisionConfiguration);
        dynamicsWorld.setGravity(new Vector3f(0, DEFAULT_GRAVITY, 0));
    }

    public void stepWorld() {
        float d = clock.getTimeMicroseconds() / 1000000F;
        float maxSubSteps = 10.0f;
        clock.reset();

        // TODO clean old physics entities
        this.dynamicsWorld.stepSimulation(d, (int) maxSubSteps, d/maxSubSteps);
        this.physicsEntities.forEach(physics -> {
            if(ClientInitializer.client.world != null) {
                loadEntityCollisions(physics, ClientInitializer.client.world);
                loadBlockCollisions(physics, ClientInitializer.client.world);
            }
            physics.stepPhysics(d);
        });
    }

    public void loadBlockCollisions(PhysicsEntity physics, ClientWorld world) {
        Box area = physics.getBoundingBox().expand(LOAD_AREA);
        Map<BlockPos, BlockState> blockList = BlockHelper.getBlockList(world, area);
        BlockView blockView = world.getChunkManager().getChunk(physics.chunkX, physics.chunkZ);
        List<BlockPos> toKeep = new ArrayList();

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

                        toKeep.add(blockPos);
                        this.collisionBlocks.put(blockPos, body);
                        this.dynamicsWorld.addRigidBody(body);
                    }
                } else {
                    toKeep.add(blockPos);
                }
            }
        });

        List<BlockPos> toRemove = new ArrayList();
        this.collisionBlocks.forEach((pos, body) -> {
            if(!toKeep.contains(pos)) {
                dynamicsWorld.removeRigidBody(collisionBlocks.get(pos));
                toRemove.add(pos);
            }
        });
        toRemove.forEach(this.collisionBlocks::remove);
    }

    public void loadEntityCollisions(PhysicsEntity physics, ClientWorld world) {
        Box area = physics.getBoundingBox().expand(LOAD_AREA);
        List<Entity> toKeep = new ArrayList();

        world.getEntities(physics, area).forEach(entity -> {
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

                toKeep.add(entity);
                this.collisionEntities.put(entity, body);
                this.dynamicsWorld.addRigidBody(body);
            }
        });

        List<Entity> toRemove = new ArrayList();
        this.collisionEntities.forEach((entity, body) -> {
            if(!toKeep.contains(entity)) {
                this.dynamicsWorld.removeRigidBody(body);
                toRemove.add(entity);
            }
        });
        toRemove.forEach(collisionEntities::remove);
    }

    public void add(PhysicsEntity physics) {
        this.dynamicsWorld.addRigidBody(physics.getRigidBody());
        this.physicsEntities.add(physics);
    }

    public void remove(PhysicsEntity physics) {
        this.dynamicsWorld.removeRigidBody(physics.getRigidBody());
        this.physicsEntities.remove(physics);
    }
}

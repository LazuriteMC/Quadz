package dev.lazurite.quadz.common.util.tick;

import com.jme3.math.Vector3f;
import dev.lazurite.quadz.common.state.entity.QuadcopterEntity;
import dev.lazurite.rayon.core.impl.physics.space.MinecraftSpace;
import dev.lazurite.rayon.core.impl.physics.space.body.BlockRigidBody;
import dev.lazurite.rayon.core.impl.physics.space.body.ElementRigidBody;
import dev.lazurite.rayon.core.impl.util.math.VectorHelper;
import net.minecraft.util.math.BlockPos;

public class QuadSaver {
    public static void step(MinecraftSpace space) {
        space.getRigidBodiesByClass(ElementRigidBody.class).forEach(rigidBody -> {
            if (rigidBody.getElement() instanceof QuadcopterEntity && rigidBody.getClump() != null) {
                QuadcopterEntity quadcopter = (QuadcopterEntity) rigidBody.getElement();
                BlockPos blockPos = new BlockPos(VectorHelper.vector3fToVec3d(rigidBody.getPhysicsLocation(new Vector3f())));

                rigidBody.getClump().getData().forEach(info -> {
                    if (info.getBlockPos().equals(blockPos)) {
                        quadcopter.stuckTicks++;

                        if (quadcopter.stuckTicks > 20) {
                            quadcopter.stuckTicks = 0;
                            space.getRigidBodiesByClass(BlockRigidBody.class).forEach(block -> {
                                if (block.getBlockPos().equals(blockPos)) {
                                    rigidBody.addToIgnoreList(block);
                                }
                            });
                        }
                    }
                });
            }
        });
    }
}

package dev.lazurite.fpvracing.common.entity;

import dev.lazurite.fpvracing.FPVRacing;
import dev.lazurite.fpvracing.client.input.InputTick;
import dev.lazurite.fpvracing.common.item.ChannelWandItem;
import dev.lazurite.fpvracing.common.item.TransmitterItem;
import dev.lazurite.rayon.api.packet.RayonSpawnS2CPacket;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.network.Packet;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.LiteralText;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Box;
import net.minecraft.world.World;

import java.util.List;
import java.util.Random;

public abstract class FlyableEntity extends Entity {

    public void step(float delta) {
        calculateBlockDamage();
        decreaseAngularVelocity();

        if (player != null) {
            if (TransmitterItem.isBoundTransmitter(player.getMainHandStack(), this)) {
                stepInput(delta);
            }
        }
    }

    /**
     * Finds all instances of {@link FlyableEntity} within range of the given {@link Entity}.
     * @param origin the {@link Entity} as the origin
     * @return a {@link List} of type {@link FlyableEntity}
     */
    public static List<FlyableEntity> getList(Entity origin, Class<? extends FlyableEntity> type, int r) {
        ServerWorld world = (ServerWorld) origin.getEntityWorld();
        return world.getEntitiesByClass(type, new Box(origin.getBlockPos()).expand(r), EntityPredicates.VALID_ENTITY);
    }

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

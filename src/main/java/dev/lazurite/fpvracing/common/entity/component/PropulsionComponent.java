package dev.lazurite.fpvracing.common.entity.component;

import dev.lazurite.fpvracing.FPVRacing;
import dev.lazurite.rayon.physics.body.EntityRigidBody;
import dev.lazurite.rayon.physics.helper.math.QuaternionHelper;
import dev.onyxstudios.cca.api.v3.component.ComponentV3;
import dev.onyxstudios.cca.api.v3.component.sync.AutoSyncedComponent;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import physics.javax.vecmath.Quat4f;

public class PropulsionComponent implements ComponentV3, AutoSyncedComponent {
    private final Entity entity;
    private float thrust;

    public PropulsionComponent(Entity entity) {
        this.entity = entity;
    }

    public static PropulsionComponent get(Entity entity) {
        try {
            return FPVRacing.PROPULSION.get(entity);
        } catch (Exception e) {
            return null;
        }
    }

    public static boolean is(Entity entity) {
        return get(entity) != null;
    }

    public void step(float delta) {

    }

    public Entity getEntity() {
        return this.entity;
    }

    @Override
    public void applySyncPacket(PacketByteBuf buf) {
        thrust = buf.readFloat();
    }

    @Override
    public void writeSyncPacket(PacketByteBuf buf, ServerPlayerEntity recipient) {
        buf.writeFloat(thrust);
    }

    @Override
    public void readFromNbt(CompoundTag tag) {
        thrust = tag.getFloat("thrust");
    }

    @Override
    public void writeToNbt(CompoundTag tag) {
        tag.putFloat("thrust", thrust);
    }

    public void rotateX(float angle) {
        EntityRigidBody body = EntityRigidBody.get(getEntity());
        Quat4f orientation = body.getOrientation(new Quat4f());
        QuaternionHelper.rotateX(orientation, angle);
        body.setOrientation(orientation);
    }

    public void rotateY(float angle) {
        EntityRigidBody body = EntityRigidBody.get(getEntity());
        Quat4f orientation = body.getOrientation(new Quat4f());
        QuaternionHelper.rotateY(orientation, angle);
        body.setOrientation(orientation);
    }

    public void rotateZ(float angle) {
        EntityRigidBody body = EntityRigidBody.get(getEntity());
        Quat4f orientation = body.getOrientation(new Quat4f());
        QuaternionHelper.rotateZ(orientation, angle);
        body.setOrientation(orientation);
    }
}

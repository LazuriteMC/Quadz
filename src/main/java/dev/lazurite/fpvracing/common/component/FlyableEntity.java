package dev.lazurite.fpvracing.common.component;

import dev.lazurite.fpvracing.FPVRacing;
import dev.lazurite.fpvracing.client.input.InputFrame;
import dev.lazurite.fpvracing.client.input.InputTick;
import dev.lazurite.fpvracing.common.util.BetaflightHelper;
import dev.lazurite.rayon.physics.body.EntityRigidBody;
import dev.lazurite.rayon.physics.helper.math.QuaternionHelper;
import dev.onyxstudios.cca.api.v3.component.ComponentV3;
import dev.onyxstudios.cca.api.v3.component.sync.AutoSyncedComponent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import physics.javax.vecmath.Quat4f;

import java.util.Random;

public class FlyableEntity implements ComponentV3, AutoSyncedComponent {
    private final Entity entity;
    private final InputFrame inputFrame;

    private int bindId;

    private float rate;
    private float superRate;
    private float expo;

    private float thrust;
    private float thrustCurve;

    public FlyableEntity(Entity entity) {
        this.entity = entity;
        this.inputFrame = new InputFrame();
    }

    public static FlyableEntity get(Entity entity) {
        try {
            return FPVRacing.FLYABLE_ENTITY.get(entity);
        } catch (Exception e) {
            return null;
        }
    }

    public static boolean is(Entity entity) {
        return get(entity) != null;
    }

    public static void step(EntityRigidBody body, float delta) {
        FlyableEntity flyable = FlyableEntity.get(body.getEntity());
        flyable.rotateX((float) BetaflightHelper.calculateRates(flyable.getFrame().getX(), flyable.getRate(), flyable.getExpo(), flyable.getSuperRate(), delta));
        flyable.rotateY((float) BetaflightHelper.calculateRates(flyable.getFrame().getY(), flyable.getRate(), flyable.getExpo(), flyable.getSuperRate(), delta));
        flyable.rotateZ((float) BetaflightHelper.calculateRates(flyable.getFrame().getZ(), flyable.getRate(), flyable.getExpo(), flyable.getSuperRate(), delta));
//        body.applyForce(thrust.getForce());

        if (body.getDynamicsWorld().getWorld().isClient()) {
            flyable.getFrame().set(InputTick.INSTANCE.getFrame());
        }
    }

    public void bind(PlayerEntity player, ItemStack stack) {
        Random rand = new Random();

        // TODO
        bindId = rand.nextInt(10000);

        player.sendMessage(new LiteralText("Transmitter bound"), false);
    }

    public void rotateX(float angle) {
        EntityRigidBody body = EntityRigidBody.get(entity);
        Quat4f orientation = body.getOrientation(new Quat4f());
        QuaternionHelper.rotateX(orientation, angle);
        body.setOrientation(orientation);
    }

    public void rotateY(float angle) {
        EntityRigidBody body = EntityRigidBody.get(entity);
        Quat4f orientation = body.getOrientation(new Quat4f());
        QuaternionHelper.rotateY(orientation, angle);
        body.setOrientation(orientation);
    }

    public void rotateZ(float angle) {
        EntityRigidBody body = EntityRigidBody.get(entity);
        Quat4f orientation = body.getOrientation(new Quat4f());
        QuaternionHelper.rotateZ(orientation, angle);
        body.setOrientation(orientation);
    }

    public InputFrame getFrame() {
        return this.inputFrame;
    }

    public float getRate() {
        return this.rate;
    }

    public float getSuperRate() {
        return this.superRate;
    }

    public float getExpo() {
        return this.expo;
    }

    @Override
    public void applySyncPacket(PacketByteBuf buf) {
        inputFrame.set(InputFrame.fromBuffer(buf));

    }

    @Override
    public void writeSyncPacket(PacketByteBuf buf, ServerPlayerEntity recipient) {
        inputFrame.toBuffer(buf);
    }

    @Override
    public void readFromNbt(CompoundTag compoundTag) {

    }

    @Override
    public void writeToNbt(CompoundTag compoundTag) {

    }
}

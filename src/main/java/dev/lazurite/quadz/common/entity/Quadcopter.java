package dev.lazurite.quadz.common.entity;

import com.jme3.math.Quaternion;
import com.jme3.math.Transform;
import com.jme3.math.Vector3f;
import dev.lazurite.form.api.Templated;
import dev.lazurite.form.api.loader.TemplateLoader;
import dev.lazurite.quadz.Quadz;
import dev.lazurite.quadz.client.Config;
import dev.lazurite.quadz.client.QuadzClient;
import dev.lazurite.quadz.common.util.Bindable;
import dev.lazurite.quadz.common.util.Search;
import dev.lazurite.quadz.common.item.GogglesItem;
import dev.lazurite.quadz.common.item.RemoteItem;
import dev.lazurite.quadz.common.util.BetaflightHelper;
import dev.lazurite.quadz.common.util.Matrix4fHelper;
import dev.lazurite.rayon.api.EntityPhysicsElement;
import dev.lazurite.rayon.impl.bullet.collision.body.ElementRigidBody;
import dev.lazurite.rayon.impl.bullet.collision.body.EntityRigidBody;
import dev.lazurite.rayon.impl.bullet.math.Convert;
import dev.lazurite.toolbox.api.math.QuaternionHelper;
import dev.lazurite.toolbox.api.math.VectorHelper;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.ArrayList;
import java.util.Optional;

public class Quadcopter extends LivingEntity implements EntityPhysicsElement, Templated, GeoEntity, Bindable {

    public static final EntityDataAccessor<String> TEMPLATE = SynchedEntityData.defineId(Quadcopter.class, EntityDataSerializers.STRING);
    public static final EntityDataAccessor<String> PREV_TEMPLATE = SynchedEntityData.defineId(Quadcopter.class, EntityDataSerializers.STRING);
    public static final EntityDataAccessor<Boolean> ARMED = SynchedEntityData.defineId(Quadcopter.class, EntityDataSerializers.BOOLEAN);
    public static final EntityDataAccessor<Integer> BIND_ID = SynchedEntityData.defineId(Quadcopter.class, EntityDataSerializers.INT);
    public static final EntityDataAccessor<Integer> CAMERA_ANGLE = SynchedEntityData.defineId(Quadcopter.class, EntityDataSerializers.INT);

    private final EntityRigidBody rigidBody = new EntityRigidBody(this);
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    public Quadcopter(EntityType<? extends LivingEntity> entityType, Level level) {
        super(entityType, level);
        this.noCulling = true;
        this.rigidBody.setBuoyancyType(ElementRigidBody.BuoyancyType.WATER);
        this.rigidBody.setDragType(ElementRigidBody.DragType.SIMPLE);
    }

    @Override
    public void tick() {
        super.tick();

        // Update template if a change is detected
        if (!getTemplate().equals(getEntityData().get(PREV_TEMPLATE))) {
            getEntityData().set(PREV_TEMPLATE, getTemplate());
            this.refreshDimensions();
        }

        // Server-side only prioritization
        if (!this.level.isClientSide) {
            Optional.ofNullable(getRigidBody().getPriorityPlayer()).ifPresent(player -> {
                if (!((ServerPlayer) player).getCamera().equals(this)) {
                    getRigidBody().prioritize(null);
                }
            });
        }

        Search.forPlayer(this).ifPresentOrElse(player -> {
            this.setArmed(true);
            player.syncJoystick();

            if (player instanceof ServerPlayer serverPlayer && serverPlayer.getCamera() == this && !player.equals(this.getRigidBody().getPriorityPlayer())) {
                this.getRigidBody().prioritize(player);
            }

            var pitch = player.getJoystickValue(new ResourceLocation(Quadz.MODID, "pitch"));
            var yaw = player.getJoystickValue(new ResourceLocation(Quadz.MODID, "yaw"));
            var roll = player.getJoystickValue(new ResourceLocation(Quadz.MODID, "roll"));
            var throttle = player.getJoystickValue(new ResourceLocation(Quadz.MODID, "throttle")) + 1.0f;

            var rate = player.getJoystickValue(new ResourceLocation(Quadz.MODID, "rate"));
            var superRate = player.getJoystickValue(new ResourceLocation(Quadz.MODID, "super_rate"));
            var expo = player.getJoystickValue(new ResourceLocation(Quadz.MODID, "expo"));

            this.rotate(
                    (float) BetaflightHelper.calculateRates(pitch, rate, expo, superRate, 0.05f),
                    (float) BetaflightHelper.calculateRates(yaw, rate, expo, superRate, 0.05f),
                    (float) BetaflightHelper.calculateRates(roll, rate, expo, superRate, 0.05f)
            );

            // Decrease angular velocity
            if (throttle > 0.1f) {
                var correction = getRigidBody().getAngularVelocity(new Vector3f()).multLocal(0.5f * throttle);

                if (Float.isFinite(correction.lengthSquared())) {
                    getRigidBody().setAngularVelocity(correction);
                }
            }

            // Get the thrust unit vector
            // TODO make this into it's own class
            var mat = new Matrix4f();
            Matrix4fHelper.fromQuaternion(mat, QuaternionHelper.rotateX(Convert.toMinecraft(getRigidBody().getPhysicsRotation(new Quaternion())), 90));
            var unit = Convert.toBullet(Matrix4fHelper.matrixToVector(mat));

            // Calculate basic thrust
            var thrust = new Vector3f().set(unit).multLocal((float) (getThrust() * (Math.pow(throttle, getThrustCurve()))));

            // Calculate thrust from yaw spin
            var yawThrust = new Vector3f().set(unit).multLocal(Math.abs(yaw * getThrust() * 0.002f));

            // Add up the net thrust and apply the force
            if (Float.isFinite(thrust.length())) {
                getRigidBody().applyCentralForce(thrust.add(yawThrust).multLocal(-1));
            } else {
                Quadz.LOGGER.warn("Infinite thrust force!");
            }
        }, () -> {
            this.setArmed(false);

            if (!this.level.isClientSide) {
                this.getRigidBody().prioritize(null);
            }
        });
    }

    public float getThrust() {
        return TemplateLoader.getTemplateById(this.getTemplate())
                .map(template -> template.metadata().get("thrust").getAsFloat())
                .orElse(0.0f);
    }

    public float getThrustCurve() {
        return TemplateLoader.getTemplateById(this.getTemplate())
                .map(template -> template.metadata().get("thrustCurve").getAsFloat())
                .orElse(0.0f);
    }

    public void rotate(float x, float y, float z) {
        var rot = new Quaternionf(0, 0, 0, 1);
        QuaternionHelper.rotateX(rot, x);
        QuaternionHelper.rotateY(rot, y);
        QuaternionHelper.rotateZ(rot, z);

        var trans = getRigidBody().getTransform(new Transform());
        trans.getRotation().set(trans.getRotation().mult(Convert.toBullet(rot)));
        getRigidBody().setPhysicsTransform(trans);
    }

    @Override
    public InteractionResult interact(Player player, InteractionHand hand) {
        if (!level.isClientSide()) {
            final var stack = player.getInventory().getSelected();

            if (stack.getItem() instanceof RemoteItem) {
                Bindable.get(stack).ifPresent(bindable -> Bindable.bind(this, bindable));
                player.displayClientMessage(Component.translatable("quadz.message.bound"), true);
            }
        }

        return InteractionResult.SUCCESS;
    }

    @Override
    public void kill() {
        var itemStack = new ItemStack(Quadz.QUADCOPTER_ITEM);
        Bindable.get(itemStack).ifPresent(bindable -> bindable.copyFrom(this));
        Templated.get(itemStack).copyFrom(this);
        this.spawnAtLocation(itemStack);
        this.remove(RemovalReason.KILLED);
    }

    @Override
    public boolean hurt(@NotNull DamageSource source, float amount) {
        if (!level.isClientSide() && source.getEntity() instanceof ServerPlayer) {
            this.kill();
            return true;
        }

        return false;
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        getEntityData().set(TEMPLATE, tag.getString("template"));
        getEntityData().set(BIND_ID, tag.getInt("bind_id"));
        getEntityData().set(CAMERA_ANGLE, tag.getInt("camera_angle"));
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putString("template", getTemplate());
        tag.putInt("bind_id", getEntityData().get(BIND_ID));
        tag.putInt("camera_angle", getEntityData().get(CAMERA_ANGLE));
    }

    @Override
    public Vec3 getPosition(float tickDelta) {
        return VectorHelper.toVec3(Convert.toMinecraft(getPhysicsLocation(new Vector3f(), tickDelta)));
    }

    @Override
    public float getViewYRot(float tickDelta) {
        return QuaternionHelper.getYaw(Convert.toMinecraft(getPhysicsRotation(new Quaternion(), tickDelta)));
    }

    @Override
    public float getViewXRot(float tickDelta) {
        return QuaternionHelper.getPitch(Convert.toMinecraft(getPhysicsRotation(new Quaternion(), tickDelta)));
    }

    @Override
    public Direction getDirection() {
        return Direction.fromYRot(QuaternionHelper.getYaw(Convert.toMinecraft(getPhysicsRotation(new Quaternion(), 1.0f))));
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        getEntityData().define(TEMPLATE, "");
        getEntityData().define(PREV_TEMPLATE, "");
        getEntityData().define(ARMED, false);
        getEntityData().define(BIND_ID, 0);
        getEntityData().define(CAMERA_ANGLE, 0);
    }

//    @Override
//    public boolean shouldRenderPlayer() {
//        return true;
//    }

//    @Override
    public boolean shouldPlayerBeViewing(Player player) {
        return player != null && player.getInventory().armor.get(3).getItem() instanceof GogglesItem;
    }

    public boolean shouldRenderSelf() {
        return (!Config.renderCameraInCenter && Config.renderFirstPerson) || QuadzClient.isInThirdPerson();
    }

    @Override
    public boolean shouldRenderAtSqrDistance(double distance) {
        return true;
    }

    @Override
    public boolean isPickable() {
        return true;
    }

    @Override
    public Iterable<ItemStack> getArmorSlots() {
        return new ArrayList<>();
    }

    @Override
    public ItemStack getItemBySlot(EquipmentSlot equipmentSlot) {
        return new ItemStack(Items.AIR);
    }

    @Override
    public void setItemSlot(EquipmentSlot equipmentSlot, ItemStack itemStack) {

    }

    @Override
    public HumanoidArm getMainArm() {
        return null;
    }

    @Override
    public EntityRigidBody getRigidBody() {
        return this.rigidBody;
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllerRegistrar) {
        controllerRegistrar.add(new AnimationController<>(this, 0, state -> {
            if (this.isArmed()) {
                return state.setAndContinue(RawAnimation.begin().thenLoop("armed"));
            }

            return PlayState.STOP;
        }));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }

    @Override
    public void setBindId(int bindId) {
        this.getEntityData().set(BIND_ID, bindId);
    }

    @Override
    public int getBindId() {
        return this.getEntityData().get(BIND_ID);
    }

    @Override
    public String getTemplate() {
        return this.getEntityData().get(TEMPLATE);
    }

    @Override
    public void setTemplate(String template) {
        this.getEntityData().set(TEMPLATE, template);
    }

    public void setArmed(boolean armed) {
        this.getEntityData().set(ARMED, armed);
    }

    public boolean isArmed() {
        return this.getEntityData().get(ARMED);
    }

}

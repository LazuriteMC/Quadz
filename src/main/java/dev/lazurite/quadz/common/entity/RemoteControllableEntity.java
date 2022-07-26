package dev.lazurite.quadz.common.entity;

import dev.lazurite.quadz.Quadz;
import dev.lazurite.quadz.api.InputHandler;
import dev.lazurite.quadz.client.QuadzClient;
import dev.lazurite.quadz.client.render.ui.toast.ControllerNotFoundToast;
import dev.lazurite.quadz.common.data.Config;
import dev.lazurite.quadz.common.data.model.Bindable;
import dev.lazurite.quadz.common.data.model.Templated;
import dev.lazurite.quadz.common.data.template.TemplateLoader;
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
import org.jetbrains.annotations.NotNull;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.PlayState;
import software.bernie.geckolib3.core.builder.AnimationBuilder;
import software.bernie.geckolib3.core.controller.AnimationController;
import software.bernie.geckolib3.core.manager.AnimationData;
import software.bernie.geckolib3.core.manager.AnimationFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public abstract class RemoteControllableEntity extends LivingEntity implements IAnimatable, Templated, Bindable {
    /* States */
    private static final EntityDataAccessor<Boolean> ACTIVE = SynchedEntityData.defineId(RemoteControllableEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<String> TEMPLATE = SynchedEntityData.defineId(RemoteControllableEntity.class, EntityDataSerializers.STRING);

    /* Data */
    private static final EntityDataAccessor<Integer> BIND_ID = SynchedEntityData.defineId(RemoteControllableEntity.class, EntityDataSerializers.INT);

    /* Physical Attributes */
    private static final EntityDataAccessor<Integer> CAMERA_ANGLE = SynchedEntityData.defineId(RemoteControllableEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Float> WIDTH = SynchedEntityData.defineId(RemoteControllableEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> HEIGHT = SynchedEntityData.defineId(RemoteControllableEntity.class, EntityDataSerializers.FLOAT);

    protected final Map<ResourceLocation, Float> joystickValues = new HashMap<>();
    private final AnimationFactory animationFactory = new AnimationFactory(this);
    private String prevTemplate;

    public RemoteControllableEntity(EntityType<? extends LivingEntity> entityType, Level level) {
        super(entityType, level);
        this.noCulling = true;
    }

    @Override
    public void tick() {
        if (!getTemplate().equals(prevTemplate)) {
            prevTemplate = getTemplate();
            final var template = TemplateLoader.getTemplate(getTemplate());

            if (template != null) {
                setWidth(template.getSettings().getWidth());
                setHeight(template.getSettings().getHeight());
                refreshDimensions();

                if (getCameraAngle() == 0) {
                    setCameraAngle(template.getSettings().getCameraAngle());
                }
            }
        }

        this.joystickValues.clear();
        super.tick();
    }

    public void setJoystickValues(Map<ResourceLocation, Float> joystickValues) {
        this.joystickValues.clear();
        this.joystickValues.putAll(joystickValues);
    }

    @Override
    public boolean hurt(@NotNull DamageSource source, float amount) {
        if (!level.isClientSide() && source.getEntity() instanceof Player) {
            this.kill();
            return true;
        }

        return false;
    }

    @Override
    public void kill() {
        this.dropSpawner();
        this.remove(RemovalReason.KILLED);
    }

    /**
     * Copies information from this {@link RemoteControllableEntity} to a new {@link ItemStack}.
     */
    public void dropSpawner() {
        final var stack = new ItemStack(Quadz.QUADCOPTER_ITEM);
        Bindable.get(stack).ifPresent(state -> state.copyFrom(this));
        Templated.get(stack).ifPresent(state -> state.copyFrom(this));
        this.spawnAtLocation(stack);
    }

    @Override
    public Iterable<ItemStack> getArmorSlots() {
        return new ArrayList<>();
    }

    @Override
    public ItemStack getItemBySlot(EquipmentSlot slot) {
        return new ItemStack(Items.AIR);
    }

    @Override
    public void setItemSlot(EquipmentSlot slot, ItemStack stack) { }

    @Override
    public InteractionResult interact(Player player, InteractionHand hand) {
        final var stack = player.getInventory().getSelected();

        if (!level.isClientSide()) {
            Bindable.get(stack).ifPresent(bindable -> {
                Bindable.bind(this, bindable);
                player.displayClientMessage(Component.translatable("item.quadz.transmitter_item.bound"), true);
            });
        } else {
            if (stack.getItem().equals(Quadz.TRANSMITTER_ITEM)) {
                if (!InputHandler.controllerExists() && Config.controllerId != -1) {
                    ControllerNotFoundToast.add();
                }
            }
        }

        return InteractionResult.SUCCESS;
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        setTemplate(tag.getString("template"));
        setBindId(tag.getInt("bind_id"));
        setCameraAngle(tag.getInt("camera_angle"));
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putString("template", getTemplate());
        tag.putInt("bind_id", getBindId());
        tag.putInt("camera_angle", getCameraAngle());
    }

    @Override
    public boolean shouldRenderAtSqrDistance(double distance) {
        return true;
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        getEntityData().define(ACTIVE, false);
        getEntityData().define(TEMPLATE, "");
        getEntityData().define(BIND_ID, -1);
        getEntityData().define(CAMERA_ANGLE, 0);
        getEntityData().define(WIDTH, -1.0f);
        getEntityData().define(HEIGHT, -1.0f);
    }

    @Override
    public boolean isPickable() {
        return true;
    }

    @Override
    public HumanoidArm getMainArm() {
        return null;
    }

    @Override
    public void setBindId(int bindId) {
        getEntityData().set(BIND_ID, bindId);
    }

    @Override
    public int getBindId() {
        return getEntityData().get(BIND_ID);
    }

    @Override
    public void setTemplate(String template) {
        getEntityData().set(TEMPLATE, template);
    }

    public void setCameraAngle(int cameraAngle) {
        getEntityData().set(CAMERA_ANGLE, cameraAngle);
    }

    public int getCameraAngle() {
        return getEntityData().get(CAMERA_ANGLE);
    }

    public void setActive(boolean active) {
        getEntityData().set(ACTIVE, active);
    }

    public boolean isActive() {
        return getEntityData().get(ACTIVE);
    }

    public void setWidth(float width) {
        getEntityData().set(WIDTH, width);
    }

    public void setHeight(float height) {
        getEntityData().set(HEIGHT, height);
    }

    @Override
    public float getBbWidth() {
        return getEntityData().get(WIDTH);
    }

    @Override
    public float getBbHeight() {
        return getEntityData().get(HEIGHT);
    }

    public boolean shouldRenderSelf() {
        return (!Config.renderCameraInCenter && Config.renderFirstPerson) || QuadzClient.isInThirdPerson();
    }

    @Override
    public void registerControllers(AnimationData animationData) {
        AnimationController<RemoteControllableEntity> controller = new AnimationController<>(this, "remote_controllable_entity_controller", 0, event -> isActive() ? PlayState.CONTINUE : PlayState.STOP);
        controller.setAnimation(new AnimationBuilder().addAnimation("armed", true));
        animationData.addAnimationController(controller);
    }

    @Override
    public AnimationFactory getFactory() {
        return this.animationFactory;
    }

    @Override
    public String getTemplate() {
        return getEntityData().get(TEMPLATE);
    }
}

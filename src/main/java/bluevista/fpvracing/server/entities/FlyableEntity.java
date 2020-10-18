package bluevista.fpvracing.server.entities;

import bluevista.fpvracing.client.ClientInitializer;
import bluevista.fpvracing.client.input.InputTick;
import bluevista.fpvracing.config.Config;
import bluevista.fpvracing.network.entity.EntityPhysicsS2C;
import bluevista.fpvracing.network.entity.FlyableEntityS2C;
import bluevista.fpvracing.physics.Air;
import bluevista.fpvracing.physics.entity.ClientEntityPhysics;
import bluevista.fpvracing.physics.entity.EntityPhysics;
import bluevista.fpvracing.physics.entity.ServerEntityPhysics;
import bluevista.fpvracing.server.items.TransmitterItem;
import bluevista.fpvracing.util.Frequency;
import bluevista.fpvracing.util.math.QuaternionHelper;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Packet;
import net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.LiteralText;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Box;
import net.minecraft.world.World;

import javax.vecmath.Vector3f;
import java.util.List;
import java.util.Random;

public abstract class FlyableEntity extends Entity {
    public static final int TRACKING_RANGE = 80;
    public static final int PSEUDO_TRACKING_RANGE = TRACKING_RANGE / 2;

    protected final EntityPhysics physics;
    protected int bindID;
    protected float fieldOfView;
    protected Frequency frequency;

    public FlyableEntity(EntityType<?> type, World world) {
        super(type, world);

        this.frequency = new Frequency();
        this.ignoreCameraFrustum = true;
        this.bindID = -1;

        if (world.isClient()) {
            physics = new ClientEntityPhysics(this);
        } else {
            physics = new ServerEntityPhysics(this);
        }
    }

    @Override
    public void tick() {
        super.tick();
        physics.tick();

        if (!world.isClient()) {
            FlyableEntityS2C.send(this);
        }
    }

    @Environment(EnvType.CLIENT)
    public void step(float delta) {
        Vector3f direction = physics.getLinearVelocity();
        direction.normalize();
        ((ClientEntityPhysics) physics).applyForce(Air.getResistanceForce(
                direction,
                physics.getConfigValues(Config.SIZE).intValue(),
                physics.getConfigValues(Config.DRAG_COEFFICIENT).floatValue()
        ));
    }

    /**
     * Finds all instances of {@link FlyableEntity} within range of the given {@link Entity}.
     * @param entity the {@link Entity} as the origin
     * @return a {@link List} of type {@link FlyableEntity}
     */
    public static List<FlyableEntity> getList(Entity entity, int r) {
        ServerWorld world = (ServerWorld) entity.getEntityWorld();
        return world.getEntitiesByClass(FlyableEntity.class, new Box(entity.getBlockPos()).expand(r), EntityPredicates.VALID_ENTITY);
    }

    public void setConfigValues(String key, Number value) {
        switch (key) {
            case Config.BIND:
                this.bindID = value.intValue();
                break;
            case Config.FIELD_OF_VIEW:
                this.fieldOfView = value.floatValue();
                break;
            case Config.BAND:
                this.frequency.setBand((char) value.intValue());
                break;
            case Config.CHANNEL:
                this.frequency.setChannel(value.intValue());
                break;
            default:
                physics.setConfigValues(key, value);
        }
    }

    public Number getConfigValues(String key) {
        switch (key) {
            case Config.BIND:
                return this.bindID;
            case Config.FIELD_OF_VIEW:
                return this.fieldOfView;
            case Config.BAND:
                return (int) this.frequency.getBand();
            case Config.CHANNEL:
                return this.frequency.getChannel();
            default:
                return physics.getConfigValues(key);
        }
    }

    public void updatePositionAndAngles(Vector3f position, float yaw, float pitch) {
        this.updatePositionAndAngles(position.x, position.y, position.z, yaw, pitch);
        physics.setPosition(position);
        setYaw(yaw);

        if (!world.isClient()) {
            EntityPhysicsS2C.send(physics, true);
        }
    }

    @Override
    public void setYaw(float yaw) {
        if (world.isClient()) {
            ((ClientEntityPhysics) physics).rotateY(yaw);
        }

        this.prevYaw = this.yaw;
        this.yaw = yaw;
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
    protected void writeCustomDataToTag(CompoundTag tag) {
        physics.writeCustomDataToTag(tag);
        tag.putInt(Config.BIND, getConfigValues(Config.BIND).intValue());
        tag.putInt(Config.BAND, getConfigValues(Config.BAND).intValue());
        tag.putInt(Config.CHANNEL, getConfigValues(Config.CHANNEL).intValue());
        tag.putFloat(Config.FIELD_OF_VIEW, getConfigValues(Config.FIELD_OF_VIEW).floatValue());
    }

    @Override
    protected void readCustomDataFromTag(CompoundTag tag) {
        physics.readCustomDataFromTag(tag);
        setConfigValues(Config.BIND, tag.getInt(Config.BIND));
        setConfigValues(Config.BAND, tag.getInt(Config.BAND));
        setConfigValues(Config.CHANNEL, tag.getInt(Config.CHANNEL));
        setConfigValues(Config.FIELD_OF_VIEW, tag.getFloat(Config.FIELD_OF_VIEW));
    }

    /**
     * If the {@link PlayerEntity} is holding a {@link TransmitterItem} when they right
     * click on the {@link FlyableEntity}, bind it using a new random ID.
     * @param player the {@link PlayerEntity} who is interacting
     * @param hand the hand of the {@link PlayerEntity}
     * @return
     */
    @Override
    public ActionResult interact(PlayerEntity player, Hand hand) {
        if (!player.world.isClient()) {
            if (player.inventory.getMainHandStack().getItem() instanceof TransmitterItem) {
                Random rand = new Random();
                bindID = rand.nextInt();

                physics.setPlayerID(player.getEntityId());

                TransmitterItem.setTagValue(player.getMainHandStack(), Config.BIND, bindID);
                player.sendMessage(new LiteralText("Transmitter bound"), false);
            }
        } else if (!InputTick.controllerExists()) {
            player.sendMessage(new TranslatableText("Controller not found"), false);
        }

        return ActionResult.SUCCESS;
    }

    /**
     * Allows the entity to be seen from far away.
     * @param distance the distance away from the entity
     * @return whether or not the entity is outside of the view distance
     */
    @Override
    @Environment(EnvType.CLIENT)
    public boolean shouldRender(double distance) {
        return distance < Math.pow(ClientInitializer.client.options.viewDistance * 16, 2);
    }

    @Override
    public Packet<?> createSpawnPacket() {
        return new EntitySpawnS2CPacket(this);
    }

    public EntityPhysics getPhysics() {
        return this.physics;
    }

    public boolean isKillable() {
        return false;
    }
}

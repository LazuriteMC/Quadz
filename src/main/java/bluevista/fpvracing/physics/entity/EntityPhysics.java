package bluevista.fpvracing.physics.entity;

import bluevista.fpvracing.config.Config;
import bluevista.fpvracing.server.entities.FlyableEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.text.LiteralText;

import javax.vecmath.Quat4f;
import javax.vecmath.Vector3f;

public abstract class EntityPhysics {
    protected final FlyableEntity entity;
    protected int playerID;

    protected float dragCoefficient;
    protected float mass;
    protected int size;
    protected float thrust;
    protected float thrustCurve;

    protected boolean noClip;
    protected boolean godMode;

    public EntityPhysics(FlyableEntity entity) {
        this.entity = entity;

        this.playerID = -1;
        this.noClip = false;
        this.godMode = false;
    }

    public void tick() {
        entity.updatePosition(getPosition().x, getPosition().y, getPosition().z);
        entity.updateEulerRotations();

        if (playerID != -1 && entity.world.getEntityById(playerID) == null) {
            entity.kill();
        }
    }

    /**
     * Returns the target {@link FlyableEntity}.
     *
     * @return the target entity
     */
    public FlyableEntity getEntity() {
        return this.entity;
    }

    public void setPlayerID(int playerID) {
        this.playerID = playerID;

        PlayerEntity player = (PlayerEntity) entity.world.getEntityById(playerID);
        if (player != null) {
            entity.setCustomName(new LiteralText(player.getGameProfile().getName()));
            entity.setCustomNameVisible(true);
        }
    }

    public void setConfigValues(String key, Number value) {
        switch (key) {
            case Config.PLAYER_ID:
                setPlayerID(value.intValue());
                break;
            case Config.THRUST:
                this.thrust = value.floatValue();
                break;
            case Config.THRUST_CURVE:
                this.thrustCurve = value.floatValue();
                break;
            case Config.MASS:
                this.mass = value.floatValue();
                break;
            case Config.SIZE:
                this.size = value.intValue();
                break;
            case Config.DRAG_COEFFICIENT:
                this.dragCoefficient = value.floatValue();
                break;
            case Config.NO_CLIP:
                this.noClip = value.intValue() == 1;
                break;
            case Config.GOD_MODE:
                this.godMode = value.intValue() == 1;
                break;
            default:
                break;
        }
    }

    public Number getConfigValues(String key) {
        switch (key) {
            case Config.PLAYER_ID:
                return this.playerID;
            case Config.THRUST:
                return this.thrust;
            case Config.THRUST_CURVE:
                return this.thrustCurve;
            case Config.MASS:
                return this.mass;
            case Config.SIZE:
                return this.size;
            case Config.DRAG_COEFFICIENT:
                return this.dragCoefficient;
            case Config.NO_CLIP:
                return this.noClip ? 1 : 0;
            case Config.GOD_MODE:
                return this.godMode ? 1 : 0;
            default:
                return null;
        }
    }

    public void writeCustomDataToTag(CompoundTag tag) {
        tag.putInt(Config.PLAYER_ID, getConfigValues(Config.PLAYER_ID).intValue());

        tag.putFloat(Config.THRUST, getConfigValues(Config.THRUST).floatValue());
        tag.putFloat(Config.THRUST_CURVE, getConfigValues(Config.THRUST_CURVE).floatValue());

        tag.putFloat(Config.MASS, getConfigValues(Config.MASS).floatValue());
        tag.putInt(Config.SIZE, getConfigValues(Config.SIZE).intValue());
        tag.putFloat(Config.DRAG_COEFFICIENT, getConfigValues(Config.DRAG_COEFFICIENT).floatValue());

        // don't write noClip or prevGodMode because...
        // noClip shouldn't be preserved after a restart (your drone may fall through the world) and ...
        // prevGodMode is only used when noClip is set, keeping this value between restarts isn't required
        tag.putInt(Config.GOD_MODE, getConfigValues(Config.GOD_MODE).intValue());
    }

    public void readCustomDataFromTag(CompoundTag tag) {
        setConfigValues(Config.PLAYER_ID, tag.getInt(Config.PLAYER_ID));

        setConfigValues(Config.THRUST, tag.getFloat(Config.THRUST));
        setConfigValues(Config.THRUST_CURVE, tag.getFloat(Config.THRUST_CURVE));

        setConfigValues(Config.MASS, tag.getFloat(Config.MASS));
        setConfigValues(Config.SIZE, tag.getInt(Config.SIZE));
        setConfigValues(Config.DRAG_COEFFICIENT, tag.getFloat(Config.DRAG_COEFFICIENT));

        // don't retrieve noClip or prevGodMode because they weren't written (reason in writeCustomDataToTag)
        setConfigValues(Config.GOD_MODE, tag.getInt(Config.GOD_MODE));
    }

    public abstract Vector3f getPosition();
    public abstract Vector3f getLinearVelocity();
    public abstract Vector3f getAngularVelocity();
    public abstract Quat4f getOrientation();

    public abstract void setPosition(Vector3f position);
    public abstract void setLinearVelocity(Vector3f linearVelocity);
    public abstract void setAngularVelocity(Vector3f angularVelocity);
    public abstract void setOrientation(Quat4f orientation);
}

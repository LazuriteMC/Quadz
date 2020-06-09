package bluevista.fpvracingmod.server.entities;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.MovementType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Packet;
import net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class ViewHandler extends Entity {

    private static final Box NULL_BOX = new Box(0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D);

    private Entity target;

    private double prevPosX;
    private double prevPosY;
    private double prevPosZ;

    public ViewHandler(EntityType<?> entityTypeIn, World worldIn) {
        super(entityTypeIn, worldIn);
        this.noClip = true;
        this.setNoGravity(true);
    }

    public ViewHandler(World worldIn, Entity target) {
        this(EntityType.PLAYER, worldIn);
        this.setTarget(target);

        this.setPos(5, 5, 5);
//        this.setPos(target.getPos().x, target.getPos().y, target.getPos().z);
//        this.prevPosX = target.getPos().x;
//        this.prevPosY = target.getPos().y;
//        this.prevPosZ = target.getPos().z;
    }

    @Override
    public void move(MovementType type, Vec3d movement) {

    }

    public void clientTick(float delta) {
//        if(target != null) {
//            float deltaPosX = (float) (this.prevPosX + (this.target.getPos().x - this.prevPosX) * delta);
//            float deltaPosY = (float) (this.prevPosY + (this.target.getPos().y - this.prevPosY) * delta);
//            float deltaPosZ = (float) (this.prevPosZ + (this.target.getPos().z - this.prevPosZ) * delta);
//
//            this.setPos(deltaPosX, deltaPosY, deltaPosZ);
//
//            this.prevPosX = this.getPos().x;
//            this.prevPosY = this.getPos().y;
//            this.prevPosZ = this.getPos().z;
//            System.out.println(prevPosX + ", " + prevPosY + ", " + prevPosZ);
//        }
    }

    public Entity getTarget() {
        return this.target;
    }

    public void setTarget(Entity target) {
        this.target = target;
    }

    public boolean canBeCollidedWith() {
        return false;
    }

    @Override
    protected void initDataTracker() {

    }

    @Override
    protected void readCustomDataFromTag(CompoundTag tag) {

    }

    @Override
    protected void writeCustomDataToTag(CompoundTag tag) {

    }

    @Override
    public Box getBoundingBox() {
        return NULL_BOX;
    }

    @Override
    public boolean collides() {
        return false;
    }

    @Override
    public Box getHardCollisionBox(Entity collidingEntity) {
        return NULL_BOX;
    }

    @Override
    public Packet<?> createSpawnPacket() {
        return null;
    }
}

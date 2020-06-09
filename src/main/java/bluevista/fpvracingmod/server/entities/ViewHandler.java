package bluevista.fpvracingmod.server.entities;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Packet;
import net.minecraft.world.World;

public class ViewHandler extends Entity {

    private Entity target;

    private double prevPosX;
    private double prevPosY;
    private double prevPosZ;

    public ViewHandler(EntityType<?> entityTypeIn, World worldIn) {
        super(entityTypeIn, worldIn);
        this.noClip = true;
    }

    public ViewHandler(World worldIn, Entity target) {
        this(EntityType.PLAYER, worldIn);
        this.setTarget(target);

        this.setPos(target.getPos().x, target.getPos().y, target.getPos().z);
        this.prevPosX = target.getPos().x;
        this.prevPosY = target.getPos().y;
        this.prevPosZ = target.getPos().z;
    }

    public void clientTick(float delta) {
        if(target != null) {
            float deltaPosX = (float) (this.prevPosX + (this.target.getPos().x - this.prevPosX) * delta);
            float deltaPosY = (float) (this.prevPosY + (this.target.getPos().y - this.prevPosY) * delta);
            float deltaPosZ = (float) (this.prevPosZ + (this.target.getPos().z - this.prevPosZ) * delta);

            this.setPos(deltaPosX, deltaPosY, deltaPosZ);

            this.prevPosX = this.getPos().x;
            this.prevPosY = this.getPos().y;
            this.prevPosZ = this.getPos().z;
        }
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
    public Packet<?> createSpawnPacket() {
        return null; // this is on purpose
    }
}

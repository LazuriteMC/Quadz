package bluevista.fpvracingmod.server.entities;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.IPacket;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.FMLPlayMessages;
import net.minecraftforge.fml.network.NetworkHooks;

public class ViewHandler extends Entity {

    private AxisAlignedBB nullAABB;
    private Entity target;

    public ViewHandler(EntityType<?> entityTypeIn, World worldIn) {
        super(entityTypeIn, worldIn);
        this.noClip = true;
        this.nullAABB = new AxisAlignedBB(0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D);
    }

    public ViewHandler(FMLPlayMessages.SpawnEntity packet, World worldIn) {
        this(EntityType.PLAYER, worldIn);
    }

    public ViewHandler(World worldIn, Entity target) {
        this(EntityType.PLAYER, worldIn);
        this.setTarget(target);

        this.setPosition(target.getPositionVec().x, target.getPositionVec().y, target.getPositionVec().z);
        this.prevPosX = target.getPositionVec().x;
        this.prevPosY = target.getPositionVec().y;
        this.prevPosZ = target.getPositionVec().z;
    }

    public void clientTick(float delta) {
        if(target != null) {
            float deltaPosX = (float) (this.prevPosX + (this.target.getPositionVec().x - this.prevPosX) * delta);
            float deltaPosY = (float) (this.prevPosY + (this.target.getPositionVec().y - this.prevPosY) * delta);
            float deltaPosZ = (float) (this.prevPosZ + (this.target.getPositionVec().z - this.prevPosZ) * delta);

            this.setPosition(deltaPosX, deltaPosY, deltaPosZ);

            this.prevPosX = this.getPositionVec().x;
            this.prevPosY = this.getPositionVec().y;
            this.prevPosZ = this.getPositionVec().z;
        }
    }

    public Entity getTarget() {
        return this.target;
    }

    public void setTarget(Entity target) {
        this.target = target;
    }

    public AxisAlignedBB getCollisionBoundingBox() {
        return null;
    }
    public AxisAlignedBB getBoundingBox() {
        return this.nullAABB;
    }
    public boolean canBeCollidedWith() {
        return false;
    }
    public boolean isSneaking() {
        return false;
    }
    public boolean isSpectator() {
        return false;
    }
    protected void registerData() { }
    protected void readAdditional(CompoundNBT compound) { }
    protected void writeAdditional(CompoundNBT compound) { }
    public IPacket<?> createSpawnPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }
}

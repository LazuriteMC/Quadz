package bluevista.fpvracingmod.client.renderers;

import com.bluevista.fpvracing.client.models.DroneModel;
import com.bluevista.fpvracing.server.entities.DroneEntity;
import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nullable;

@OnlyIn(Dist.CLIENT)
public class DroneRenderer extends EntityRenderer<DroneEntity> {
    private static final ResourceLocation droneTexture = new ResourceLocation("textures/entity/cow/cow.png");
    private static DroneModel droneModel = new DroneModel();

    public DroneRenderer(EntityRendererManager renderManager) {
        super(renderManager);
    }

    public void doRender(DroneEntity entity, double x, double y, double z, float entityYaw, float partialTicks) {
        GlStateManager.pushMatrix();
        this.bindEntityTexture(entity);
        this.setupRotation(entity, entityYaw, partialTicks);
        this.setupTranslation(x, y, z);
//        droneModel.render(entity, partialTicks, 0.0F, -0.1F, 0.0F, 0.0F, 0.0625F);
        GlStateManager.popMatrix();
        super.doRender(entity, x, y, z, entityYaw, partialTicks);
    }

    public void setupRotation(DroneEntity entityIn, float entityYaw, float partialTicks) {
        GlStateManager.rotatef(180.0F - entityYaw, 0.0F, 1.0F, 0.0F);
    }

    public void setupTranslation(double x, double y, double z) {
        GlStateManager.translatef((float)x, (float)y + 0.375F, (float)z);
    }

//    @Override
//    protected void preRenderCallback(DroneEntity drone, float f) {
        // some people do some G11 transformations or blends here, like you can do
        // GL11.glScalef(2F, 2F, 2F); to scale up the entity
        // which is used for Slime entities.  I suggest having the entity cast to
        // your custom type to make it easier to access fields from your
        // custom entity, eg. GL11.glScalef(entity.scaleFactor, entity.scaleFactor,
        // entity.scaleFactor);
//    }

//    protected void setEntityTexture() {
//        droneTexture = new ResourceLocation(FPVRacingMod.MODID+":textures/entity/drones/default.png");
//    }

    /**
     * Returns the location of an entity's texture. Doesn't seem to be called
     * unless you call Render.bindEntityTexture.
     */
    @Nullable
    @Override
    public ResourceLocation getEntityTexture(DroneEntity drone) {
        return droneTexture;
    }
}
package dev.lazurite.quadz.client.render.camera;

import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import dev.lazurite.corduroy.api.View;
import dev.lazurite.form.api.loader.TemplateLoader;
import dev.lazurite.form.impl.common.template.model.Template;
import dev.lazurite.quadz.client.Config;
import dev.lazurite.quadz.common.entity.Quadcopter;
import dev.lazurite.rayon.impl.bullet.math.Convert;
import dev.lazurite.toolbox.api.math.QuaternionHelper;
import dev.lazurite.toolbox.api.math.VectorHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;

public class QuadcopterView extends View {

    private final Quadcopter quadcopter;
    private Template template;

    public QuadcopterView(Quadcopter quadcopter) {
        this.quadcopter = quadcopter;
    }

    public Quadcopter getQuadcopter() {
        return this.quadcopter;
    }

    @Override
    public boolean shouldRenderTarget() {
        return (!Config.renderCameraInCenter && Config.renderFirstPerson) || !Minecraft.getInstance().options.getCameraType().isFirstPerson();
    }

    @Override
    public Vec3 getPosition(float tickDelta) {
        var position = VectorHelper.toVec3(Convert.toMinecraft(this.quadcopter.getPhysicsLocation(new Vector3f(), tickDelta)));
        var options = Minecraft.getInstance().options;

        if (options.getCameraType().isFirstPerson()) {
            if (!Config.renderCameraInCenter) {
                var template = this.getTemplate();
                float cameraX = template.metadata().get("cameraX").getAsFloat();
                float cameraY = template.metadata().get("cameraY").getAsFloat();
                var offset = new org.joml.Vector3f(0, -cameraY, -cameraX);
                offset.rotate(this.getRotation(tickDelta));
                position = position.add(offset.x, offset.y, offset.z);
            }
        } else {
            var offset = new org.joml.Vector3f(0, 0, 5);
            offset.rotate(this.getRotation(tickDelta));
            position = position.add(offset.x, offset.y, offset.z);
        }

        return position;
    }

    @Override
    public Quaternionf getRotation(float tickDelta) {
        return QuaternionHelper.rotateY(QuaternionHelper.rotateX(
                Convert.toMinecraft(this.quadcopter.getPhysicsRotation(new Quaternion(), tickDelta)),
                -this.quadcopter.getEntityData().get(Quadcopter.CAMERA_ANGLE)
        ), 180);
    }

    private Template getTemplate() {
        if (this.template == null) {
            this.template = TemplateLoader.getTemplateById(this.quadcopter.getTemplate()).orElse(null);
        }

        return this.template;
    }

}

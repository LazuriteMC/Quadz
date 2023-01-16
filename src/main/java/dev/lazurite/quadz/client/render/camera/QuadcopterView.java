package dev.lazurite.quadz.client.render.camera;

import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import dev.lazurite.corduroy.api.View;
import dev.lazurite.form.api.loader.TemplateLoader;
import dev.lazurite.form.impl.common.template.model.Template;
import dev.lazurite.quadz.client.Config;
import dev.lazurite.quadz.client.QuadzClient;
import dev.lazurite.quadz.client.event.ClientEventHooks;
import dev.lazurite.quadz.common.entity.Quadcopter;
import dev.lazurite.rayon.impl.bullet.math.Convert;
import dev.lazurite.rayon.impl.bullet.thread.util.Clock;
import dev.lazurite.toolbox.api.math.QuaternionHelper;
import dev.lazurite.toolbox.api.math.VectorHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;

public class QuadcopterView extends View {

    private final Clock clock = new Clock();
    private final Quadcopter quadcopter;
    private Template template;

    public QuadcopterView(Quadcopter quadcopter) {
        this.quadcopter = quadcopter;
    }

    @Override
    public void onRender() {
        var options = Minecraft.getInstance().options;
        var camera = this.getCamera();

        if (options.getCameraType().isFirstPerson()) {
            if (!Config.renderCameraInCenter) {
                var template = this.getTemplate();
                float cameraX = template.metadata().get("cameraX").getAsFloat();
                float cameraY = template.metadata().get("cameraY").getAsFloat();
                camera.move(-cameraX, -cameraY, 0);
            }
        } else {
            camera.move(camera.getMaxZoom(4), 0, 0);
        }
    }

    @Override
    public Vec3 getPosition(float tickDelta) {
        return VectorHelper.toVec3(Convert.toMinecraft(this.quadcopter.getPhysicsLocation(new Vector3f(), tickDelta)));
    }

    @Override
    public Quaternionf getRotation(float tickDelta) {
        return QuaternionHelper.rotateY(QuaternionHelper.rotateX(
                Convert.toMinecraft(this.quadcopter.getPhysicsRotation(new Quaternion(), tickDelta)),
                -this.quadcopter.getEntityData().get(Quadcopter.CAMERA_ANGLE)
        ), 180);
    }

    @Override
    public boolean shouldRenderTarget() {
        return (!Config.renderCameraInCenter && Config.renderFirstPerson) || !Minecraft.getInstance().options.getCameraType().isFirstPerson();
    }

    public void renderShaderEffects(float tickDelta) {
        if (Minecraft.getInstance().options.getCameraType().isFirstPerson()) {
            if (QuadzClient.STATIC_SHADER.isInitialized() && Config.videoInterferenceEnabled) {
                QuadzClient.STATIC_AMOUNT.set(Mth.clamp(ClientEventHooks.quadDistanceFromPlayer / 100, 0.0f, 3.0f));
                QuadzClient.STATIC_TIMER.set(clock.get());
                QuadzClient.STATIC_SHADER.render(tickDelta);
            }

            if (QuadzClient.FISHEYE_SHADER.isInitialized() && Config.fisheyeEnabled) {
                QuadzClient.FISHEYE_AMOUNT.set(Config.fisheyeAmount);
                QuadzClient.FISHEYE_SHADER.render(tickDelta);
            }
        }
    }

    public Quadcopter getQuadcopter() {
        return this.quadcopter;
    }

    private Template getTemplate() {
        if (this.template == null) {
            this.template = TemplateLoader.getTemplateById(this.quadcopter.getTemplate()).orElse(null);
        }

        return this.template;
    }

}

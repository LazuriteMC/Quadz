package dev.lazurite.quadz.client.hooks;

import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import dev.lazurite.form.api.loader.TemplateLoader;
import dev.lazurite.quadz.Quadz;
import dev.lazurite.quadz.client.QuadzClient;
import dev.lazurite.quadz.common.entity.Quadcopter;
import dev.lazurite.rayon.impl.bullet.math.Convert;
import dev.lazurite.toolbox.api.math.QuaternionHelper;
import dev.lazurite.toolbox.api.math.VectorHelper;
import dev.lazurite.toolbox.api.network.ClientNetworking;
import net.minecraft.client.Camera;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import org.joml.Matrix4f;

import java.util.function.Consumer;

public class CameraHooks {

    private static int index = 0;

    public static void onCameraReset() {
        index = 0;
        Minecraft.getInstance().options.setCameraType(CameraType.FIRST_PERSON);
        ClientNetworking.send(Quadz.Networking.REQUEST_PLAYER_VIEW, buf -> {});
    }

    public static CameraType onCycle() {
        var player = Minecraft.getInstance().player;

        /*
            Check to make sure the conditions for player viewing haven't changed.
            If they have changed, switch the player's view back to themself.
         */
        Consumer<Quadcopter> changeIndex = quadcopter -> {
            final var continueViewing = quadcopter.shouldPlayerBeViewing(player);
            index = (index + 1) % (continueViewing ? 5 : 3);

            if (index >= 0 && index <= 2) {
                if (Minecraft.getInstance().cameraEntity instanceof Quadcopter) {
                    ClientNetworking.send(Quadz.Networking.REQUEST_PLAYER_VIEW, buf -> {});
                }
            }
        };

        QuadzClient.getQuadcopter().ifPresentOrElse(changeIndex, () -> {
            if (Minecraft.getInstance().cameraEntity instanceof Quadcopter quadcopter) {
                changeIndex.accept(quadcopter);
            } else {
                index = (index + 1) % 3;
            }
        });

        switch (index) {
            case 0 -> {
                return CameraType.FIRST_PERSON;
            }
            case 1 -> {
                return CameraType.THIRD_PERSON_BACK;
            }
            case 2 -> {
                return CameraType.THIRD_PERSON_FRONT;
            }
            case 3 -> {
                ClientNetworking.send(Quadz.Networking.REQUEST_REMOTE_CONTROLLABLE_VIEW, buf -> buf.writeInt(0));
            }

            case 4 -> {
                if (!(Minecraft.getInstance().cameraEntity instanceof Quadcopter)) {
                    index = 1;
                    return CameraType.THIRD_PERSON_BACK;
                }
            }
        }

        return null;
    }

    /**
     * Upon a call to {@link Camera#setPosition}, check if the camera entity is a {@link Quadcopter}.
     */
    public static boolean onSetPosition(Entity entity) {
        return entity instanceof Quadcopter;
    }

    /**
     * Upon a call to {@link Camera#setRotation}, check if the camera entity is a {@link Quadcopter}.
     */
    public static boolean onSetRotation(Entity entity) {
        return entity instanceof Quadcopter;
    }

    /**
     * Transforms the camera's rotation and position based on the quadcopter's perspective.
     */
    public static void onRotate(Camera camera, Entity entity, float tickDelta, boolean thirdPerson) {
        if (entity instanceof Quadcopter quadcopter && quadcopter.getRigidBody() != null && quadcopter.getRigidBody().getFrame() != null) {
            TemplateLoader.getTemplateById(quadcopter.getTemplate()).ifPresent(template -> {
                // Apply position
                var location = quadcopter.getPhysicsLocation(new Vector3f(), tickDelta);
                camera.setPosition(VectorHelper.toVec3(Convert.toMinecraft(location)));

                // Apply rotation (plus camera angle)
                var quaternion = quadcopter.getPhysicsRotation(new Quaternion(), tickDelta);
                var cameraAngle = quadcopter.getEntityData().get(Quadcopter.CAMERA_ANGLE);
                camera.xRot = QuaternionHelper.getPitch(Convert.toMinecraft(quaternion));
                camera.yRot = QuaternionHelper.getYaw(Convert.toMinecraft(quaternion));
                camera.rotation.set(0.0F, 0.0F, 0.0F, 1.0F);
                camera.rotation.mul(Convert.toMinecraft(quaternion));
                QuaternionHelper.rotateX(camera.rotation, cameraAngle);

                // Rotate planes
                var mat = camera.rotation.get(new Matrix4f());
                camera.forwards.set(0.0F, 0.0F, 1.0F);
                camera.forwards.mulTransposeDirection(mat);
                camera.up.set(0.0F, 1.0F, 0.0F);
                camera.up.mulTransposeDirection(mat);
                camera.left.set(1.0F, 0.0F, 0.0F);
                camera.left.mulTransposeDirection(mat);

                // Adjust camera position for first person view
                if (!thirdPerson) {
                    double cameraX = template.metadata().get("cameraX").getAsDouble();
                    double cameraY = template.metadata().get("cameraY").getAsDouble();
                    camera.move(cameraX, cameraY, 0);
                }
            });
        }
    }
}

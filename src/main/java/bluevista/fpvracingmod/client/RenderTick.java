package bluevista.fpvracingmod.client;

import bluevista.fpvracingmod.client.input.InputTick;
import bluevista.fpvracingmod.client.math.QuaternionHelper;
import bluevista.fpvracingmod.server.entities.DroneEntity;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Matrix4f;
import net.minecraft.util.math.Quaternion;

@Environment(EnvType.CLIENT)

public class RenderTick {
    public static void tick(MinecraftClient client, MatrixStack stack, float delta) {
        Entity entity = client.getCameraEntity();

        if(entity instanceof DroneEntity) {
            DroneEntity drone = (DroneEntity) entity;
            Quaternion q = new Quaternion(drone.getOrientation());

            if (!ClientTick.isValidTransmitter(client.player.getMainHandStack(), drone)) {
                Quaternion prevQ = drone.getPrevOrientation();

                float dX = MathHelper.lerp(delta, prevQ.getX(), q.getX());
                float dY = MathHelper.lerp(delta, prevQ.getY(), q.getY());
                float dZ = MathHelper.lerp(delta, prevQ.getZ(), q.getZ());
                float dW = MathHelper.lerp(delta, prevQ.getW(), q.getW());
                q.set(dX, dY, dZ, dW);

                drone.setPrevOrientation(q);
            }

            QuaternionHelper.rotateX(q, drone.getCameraAngle());
            Matrix4f newMat = new Matrix4f(q);
            Matrix4f screenMat = stack.peek().getModel();
            newMat.transpose();
            screenMat.multiply(newMat);
        }

        if(ClientTick.currentDrone != null)
            InputTick.tick(ClientTick.currentDrone);
    }
}

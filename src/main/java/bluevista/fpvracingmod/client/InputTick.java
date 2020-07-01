package bluevista.fpvracingmod.client;

import bluevista.fpvracingmod.client.controller.Controller;
import bluevista.fpvracingmod.client.math.helper.QuaternionHelper;
import bluevista.fpvracingmod.server.ServerInitializer;
import bluevista.fpvracingmod.server.entities.DroneEntity;
import bluevista.fpvracingmod.server.items.GogglesItem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.event.client.ClientTickCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.ItemStack;

@Environment(EnvType.CLIENT)
public class InputTick {

    private static final MinecraftClient mc = MinecraftClient.getInstance();

    private static boolean shouldTick;

    private static float prevX;
    private static float prevY;
    private static float prevZ;
    private static float prevT;

    public static void initKeyBindings() {
        ClientTickCallback.EVENT.register(client -> {
            while (mc.options.keySneak.wasPressed()) {
                if(client.player.inventory.armor.get(3).getItem() instanceof GogglesItem) {
                    client.player.inventory.armor.get(3).decrement(1);
                    client.player.giveItemStack(new ItemStack(ServerInitializer.GOGGLES_ITEM));
                }
            }
        });
    }

    public static void tick(DroneEntity drone, float delta) {
        if(shouldTick()) {
            float currX = -Controller.getAxis(Controller.ROLL_NUM);
            float currY = -Controller.getAxis(Controller.PITCH_NUM);
            float currZ = -Controller.getAxis(Controller.YAW_NUM);
            float currT = Controller.getAxis(Controller.THROTTLE_NUM) + 1;

            float deltaX = prevX + (currX - prevX) * delta;
            float deltaY = prevY + (currY - prevY) * delta;
            float deltaZ = prevZ + (currZ - prevZ) * delta;
            float deltaT = prevT + (currT - prevT) * delta;

            drone.setOrientation(QuaternionHelper.rotateX(drone.getOrientation(), deltaX));
            drone.setOrientation(QuaternionHelper.rotateY(drone.getOrientation(), deltaY));
            drone.setOrientation(QuaternionHelper.rotateZ(drone.getOrientation(), deltaZ));
            drone.setThrottle(deltaT);

            prevX = currX;
            prevY = currY;
            prevZ = currZ;
            prevT = currT;
        }
    }

    public static void setShouldTick(boolean should) {
        shouldTick = should;
    }

    public static boolean shouldTick() {
        return shouldTick && !mc.isPaused();
    }

}

package bluevista.fpvracing.client;

import bluevista.fpvracing.server.entities.FlyableEntity;
import bluevista.fpvracing.server.entities.QuadcopterEntity;
import bluevista.fpvracing.util.TickTimer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.event.client.ClientTickCallback;
import net.fabricmc.fabric.api.network.ClientSidePacketRegistry;
import net.minecraft.client.MinecraftClient;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;

@Environment(EnvType.CLIENT)
public class ClientTick {
    private static final TickTimer positionTickTimer = new TickTimer(20);

    public static boolean isServerModded = false;
    public static boolean shouldRenderPlayer = false;

    private static float droneFOV;
    private static double prevFOV;

    public static void tick(MinecraftClient client) {
        if (client.player != null && !client.isPaused()) {
            if (client.getCameraEntity() instanceof QuadcopterEntity) {
                QuadcopterEntity drone = (QuadcopterEntity) client.getCameraEntity();

//                if (!isServerModded) {
//                    double x = drone.getX();
//                    double y = drone.getY();
//                    double z = drone.getZ();
//                    boolean onGround = drone.isOnGround();
//
//                    client.player.setPos(x, y, z);
//                    if (positionTickTimer.tick()) {
//                        ClientSidePacketRegistry.INSTANCE.sendToServer(new PlayerMoveC2SPacket.PositionOnly(x, y, z, onGround));
//                    }
//                }

                droneFOV = client.cameraEntity.getDataTracker().get(FlyableEntity.FIELD_OF_VIEW);

                if (droneFOV != 0.0f && client.options.fov != droneFOV) {
                    prevFOV = client.options.fov;
                    client.options.fov = droneFOV;
                }

                if (droneFOV == 0.0f && prevFOV != 0.0f) {
                    client.options.fov = prevFOV;
                    prevFOV = 0.0f;
                }
            } else if (prevFOV != 0.0f) {
                client.options.fov = prevFOV;
                prevFOV = 0.0f;
            }
        }
    }

    public static boolean isPlayerIDClient(int playerID) {
        if (ClientInitializer.client.player != null) {
            return playerID == ClientInitializer.client.player.getEntityId();
        } else {
            return false;
        }
    }

    /**
     * Finds out whether or not the player is in the goggles
     * by checking what the client camera entity is.
     * @param client the minecraft client to use
     * @return whether or not the player is in goggles
     */
    public static boolean isInGoggles(MinecraftClient client) {
        return client.getCameraEntity() instanceof QuadcopterEntity;
    }

    public static void register() {
        ClientTickCallback.EVENT.register(ClientTick::tick);
    }
}

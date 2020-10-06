package bluevista.fpvracing.client;

import bluevista.fpvracing.config.Config;
import bluevista.fpvracing.server.entities.DroneEntity;
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
            if (client.getCameraEntity() instanceof DroneEntity) {
                DroneEntity drone = (DroneEntity) client.getCameraEntity();

                if (!isServerModded) {
                    double x = drone.getX();
                    double y = drone.getY();
                    double z = drone.getZ();
                    boolean onGround = drone.isOnGround();

                    client.player.setPos(x, y, z);
                    if (positionTickTimer.tick()) {
                        ClientSidePacketRegistry.INSTANCE.sendToServer(new PlayerMoveC2SPacket.PositionOnly(x, y, z, onGround));
                    }
                }

                droneFOV = ((DroneEntity)client.cameraEntity).getConfigValues(Config.FIELD_OF_VIEW).floatValue();

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

    public static void createDrone(MinecraftClient client) {
        if (client.player != null && client.world != null) {
            client.setCameraEntity(new DroneEntity(client.world, client.player.getPos(), client.player.yaw));

            DroneEntity drone = (DroneEntity) client.getCameraEntity();
            drone.prepConfig();
            client.world.addEntity(drone.getEntityId(), drone);
            client.setCameraEntity(drone);
        }
    }

    public static void destroyDrone(MinecraftClient client) {
        if (isFlyingClientSideDrone(client)) {
            DroneEntity drone = (DroneEntity) client.getCameraEntity();
            client.setCameraEntity(client.player);
            drone.remove();
        }
    }

    public static boolean isFlyingClientSideDrone(MinecraftClient client) {
        return !isServerModded && client.getCameraEntity() instanceof DroneEntity;
    }

    public static boolean isPlayerIDClient(int playerID) {
        if (ClientInitializer.client.player != null) {
            return playerID == ClientInitializer.client.player.getEntityId();
        } else {
            return false;
        }
    }

    public static void register() {
        ClientTickCallback.EVENT.register(ClientTick::tick);
    }
}

package dev.lazurite.quadz.client.render.camera;

import dev.lazurite.quadz.Quadz;
import dev.lazurite.quadz.client.QuadzClient;
import dev.lazurite.quadz.common.entity.Quadcopter;
import dev.lazurite.toolbox.api.network.ClientNetworking;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;

import java.util.Optional;
import java.util.function.Consumer;

public class CameraHooks {

    private static int index = 0;

    public static void onCameraReset() {
        index = 0;
        Minecraft.getInstance().options.setCameraType(CameraType.FIRST_PERSON);
        ClientNetworking.send(Quadz.Networking.REQUEST_PLAYER_VIEW, buf -> {});
    }

    public static Optional<CameraType> onCycle() {
        var player = Minecraft.getInstance().player;

        /*
            Check to make sure the conditions for player viewing haven't changed.
            If they have changed, switch the player's view back to themself.
         */
        Consumer<Quadcopter> changeIndex = quadcopter -> {
            var continueViewing = quadcopter.shouldPlayerBeViewing(player);
            index = (index + 1) % (continueViewing ? 5 : 3);

            if (index >= 0 && index <= 2) {
                if (QuadzClient.getQuadcopterFromCamera().isPresent()) {
                    ClientNetworking.send(Quadz.Networking.REQUEST_PLAYER_VIEW, buf -> {});
                }
            }
        };

        QuadzClient.getQuadcopterFromRemote().ifPresentOrElse(changeIndex,               // get quadcopter from remote or...
                () -> QuadzClient.getQuadcopterFromCamera().ifPresentOrElse(changeIndex, // get quadcopter from camera or...
                        () -> index = (index + 1) % 3)                                   // just do default behavior
        );

        return switch (index) {
            case 0 -> Optional.of(CameraType.FIRST_PERSON);
            case 1 -> Optional.of(CameraType.THIRD_PERSON_BACK);
            case 2 -> Optional.of(CameraType.THIRD_PERSON_FRONT);
            case 3 -> {
                ClientNetworking.send(Quadz.Networking.REQUEST_QUADCOPTER_VIEW, buf -> buf.writeInt(0));
                yield Optional.of(CameraType.FIRST_PERSON);
            }
            case 4 -> {
                if (QuadzClient.getQuadcopterFromCamera().isEmpty()) {
                    index = 1;
                    yield Optional.of(CameraType.THIRD_PERSON_BACK);
                }
                yield Optional.empty();
            }
            default -> Optional.empty();
        };
    }

}

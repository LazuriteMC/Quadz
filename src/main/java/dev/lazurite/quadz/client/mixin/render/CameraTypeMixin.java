package dev.lazurite.quadz.client.mixin.render;

import dev.lazurite.quadz.Quadz;
import dev.lazurite.quadz.client.util.CameraTypeAccess;
import dev.lazurite.quadz.common.item.GogglesItem;
import dev.lazurite.quadz.common.quadcopter.Quadcopter;
import dev.lazurite.toolbox.api.network.ClientNetworking;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(CameraType.class)
public class CameraTypeMixin implements CameraTypeAccess {
    @Unique private static int index;

    @Inject(method = "cycle", at = @At("HEAD"), cancellable = true)
    public void cycle_HEAD(CallbackInfoReturnable<CameraType> info) {
        final var player = Minecraft.getInstance().player;
        final var wearingGoggles = player != null && player.getInventory().armor.get(3).getItem() instanceof GogglesItem;

        index = (index + 1) % (wearingGoggles ? 5 : 3);

        if (index >= 0 && index <= 2) {
            if (Minecraft.getInstance().cameraEntity instanceof Quadcopter) {
                ClientNetworking.send(Quadz.REQUEST_PLAYER_VIEW_C2S, buf -> {});
            }
        }

        switch (index) {
            case 0 -> info.setReturnValue(CameraType.FIRST_PERSON);
            case 1 -> info.setReturnValue(CameraType.THIRD_PERSON_BACK);
            case 2 -> info.setReturnValue(CameraType.THIRD_PERSON_FRONT);
            case 3 -> ClientNetworking.send(Quadz.REQUEST_QUADCOPTER_VIEW_C2S, buf -> buf.writeInt(0));

            case 4 -> {
                if (!(Minecraft.getInstance().cameraEntity instanceof Quadcopter)) {
                    index = 1;
                    info.setReturnValue(CameraType.THIRD_PERSON_BACK);
                }
            }
        }
    }

    @Override
    public void reset() {
        index = 0;
        Minecraft.getInstance().options.setCameraType(CameraType.FIRST_PERSON);
        ClientNetworking.send(Quadz.REQUEST_PLAYER_VIEW_C2S, buf -> {});
    }
}

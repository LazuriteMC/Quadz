package dev.lazurite.quadz.common.util.event;

import dev.lazurite.toolbox.api.event.Event;
import net.minecraft.world.entity.Entity;

public class CameraEvents {
    public static Event<SwitchCameraEvent> SWITCH_CAMERA_EVENT = Event.create();

    public interface SwitchCameraEvent {
        void onSwitchCamera(Entity previousCameraEntity, Entity currentCameraEntity);
    }
}

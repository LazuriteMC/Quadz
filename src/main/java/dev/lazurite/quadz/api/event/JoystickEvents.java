package dev.lazurite.quadz.api.event;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;

public class JoystickEvents {
    public static final Event<JoystickConnectEvent> JOYSTICK_CONNECT = EventFactory.createArrayBacked(JoystickConnectEvent.class, (callbacks) -> (id, name) -> {
        for (JoystickConnectEvent event : callbacks) {
            event.onConnect(id, name);
        }
    });

    public static final Event<JoystickDisconnectEvent> JOYSTICK_DISCONNECT = EventFactory.createArrayBacked(JoystickDisconnectEvent.class, (callbacks) -> (id, name) -> {
        for (JoystickDisconnectEvent event : callbacks) {
            event.onDisconnect(id, name);
        }
    });

    private JoystickEvents() {
    }

    @FunctionalInterface
    public interface JoystickConnectEvent {
        void onConnect(int id, String name);
    }

    @FunctionalInterface
    public interface JoystickDisconnectEvent {
        void onDisconnect(int id, String name);
    }
}

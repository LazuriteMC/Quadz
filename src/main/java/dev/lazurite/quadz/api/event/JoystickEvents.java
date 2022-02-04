package dev.lazurite.quadz.api.event;

import dev.lazurite.toolbox.api.event.Event;

public class JoystickEvents {
    public static final Event<JoystickConnectEvent> JOYSTICK_CONNECT = Event.create();
    public static final Event<JoystickDisconnectEvent> JOYSTICK_DISCONNECT = Event.create();

    private JoystickEvents() {}

    @FunctionalInterface
    public interface JoystickConnectEvent {
        void onConnect(int id, String name);
    }

    @FunctionalInterface
    public interface JoystickDisconnectEvent {
        void onDisconnect(int id, String name);
    }
}

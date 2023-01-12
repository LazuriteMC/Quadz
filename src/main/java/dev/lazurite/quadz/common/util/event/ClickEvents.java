package dev.lazurite.quadz.common.util.event;

import dev.lazurite.toolbox.api.event.Event;

public class ClickEvents {
    public static Event<Click> RIGHT_CLICK_EVENT = Event.create();
    public static Event<Click> LEFT_CLICK_EVENT = Event.create();

    public interface Click {
        void onClick();
    }
}

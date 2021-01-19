package dev.lazurite.fpvracing.api.event;

import dev.lazurite.fpvracing.common.entity.QuadcopterEntity;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;

public class QuadcopterStepEvents {
    public static final Event<StartQuadcopterStep> START_QUADCOPTER_STEP = EventFactory.createArrayBacked(StartQuadcopterStep.class, (callbacks) -> (quadcopter, delta) -> {
        for (StartQuadcopterStep event : callbacks) {
            event.onStartStep(quadcopter, delta);
        }
    });

    public static final Event<EndQuadcopterStep> END_QUADCOPTER_STEP = EventFactory.createArrayBacked(EndQuadcopterStep.class, (callbacks) -> (quadcopter, delta) -> {
        for (EndQuadcopterStep event : callbacks) {
            event.onEndStep(quadcopter, delta);
        }
    });

    private QuadcopterStepEvents() {
    }

    @FunctionalInterface
    public interface StartQuadcopterStep {
        void onStartStep(QuadcopterEntity quadcopterEntity, float delta);
    }

    @FunctionalInterface
    public interface EndQuadcopterStep {
        void onEndStep(QuadcopterEntity quadcopterEntity, float delta);
    }
}

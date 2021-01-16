package dev.lazurite.fpvracing.common.entity.component;

import dev.lazurite.fpvracing.FPVRacing;
import dev.lazurite.fpvracing.common.util.Frequency;
import dev.onyxstudios.cca.api.v3.component.ComponentV3;
import dev.onyxstudios.cca.api.v3.component.sync.AutoSyncedComponent;
import dev.onyxstudios.cca.api.v3.component.tick.CommonTickingComponent;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.CompoundTag;

public class VideoTransmitterComponent implements ComponentV3, CommonTickingComponent, AutoSyncedComponent {
    private final Entity entity;
    private final Frequency frequency;
    private int fieldOfView;
    private int cameraAngle;

    public VideoTransmitterComponent(Entity entity) {
        this.entity = entity;
        this.frequency = new Frequency();
    }

    public static VideoTransmitterComponent get(Entity entity) {
        try {
            return FPVRacing.VIEWABLE_ENTITY.get(entity);
        } catch (Exception e) {
            return null;
        }
    }

    public static boolean is(Entity entity) {
        return get(entity) != null;
    }

    @Override
    public void tick() {

    }

    public Frequency getFrequency() {
        return this.frequency;
    }

    @Override
    public void readFromNbt(CompoundTag compoundTag) {

    }

    @Override
    public void writeToNbt(CompoundTag compoundTag) {

    }
}

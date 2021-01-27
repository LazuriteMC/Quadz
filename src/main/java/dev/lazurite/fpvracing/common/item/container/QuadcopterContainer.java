package dev.lazurite.fpvracing.common.item.container;

import dev.lazurite.fpvracing.client.input.InputFrame;
import dev.lazurite.fpvracing.common.entity.QuadcopterEntity;
import dev.lazurite.fpvracing.common.item.quadcopter.VoxelRacerOneItem;
import dev.lazurite.fpvracing.common.util.type.QuadcopterState;
import dev.lazurite.fpvracing.common.util.Frequency;
import dev.onyxstudios.cca.api.v3.component.ComponentV3;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;

/**
 * A dumping ground for {@link QuadcopterState} information. The same
 * information found here also goes in {@link QuadcopterEntity}.
 * @see QuadcopterState
 * @see VoxelRacerOneItem
 */
public class QuadcopterContainer implements ComponentV3, QuadcopterState {
    private final ItemStack stack;

    private QuadcopterEntity.State state = QuadcopterEntity.State.DISARMED;
    private boolean godMode = false;
    private int bindId = -1;

    private final Frequency frequency = new Frequency();
    private int cameraAngle = 0;
    private int fieldOfView = 90;
    private int power = 25;

    public QuadcopterContainer(ItemStack stack) {
        this.stack = stack;
    }

    public ItemStack getStack() {
        return this.stack;
    }

    @Override
    public void readFromNbt(CompoundTag tag) {
        state = QuadcopterEntity.State.valueOf(tag.getString("state"));
        godMode = tag.getBoolean("god_mode");
        bindId = tag.getInt("bind_id");

        frequency.setBand((char) tag.getInt("band"));
        frequency.setChannel(tag.getInt("channel"));
        cameraAngle = tag.getInt("camera_angle");
        fieldOfView = tag.getInt("field_of_view");
        power = tag.getInt("power");
    }

    @Override
    public void writeToNbt(CompoundTag tag) {
        tag.putString("state", state.toString());
        tag.putBoolean("god_mode", godMode);
        tag.putInt("bind_id", bindId);

        tag.putInt("band", frequency.getBand());
        tag.putInt("channel", frequency.getChannel());
        tag.putInt("camera_angle", cameraAngle);
        tag.putInt("field_of_view", fieldOfView);
        tag.putInt("power", power);
    }

    @Override
    public void setGodMode(boolean godMode) {
        this.godMode = godMode;
    }

    @Override
    public boolean isInGodMode() {
        return this.godMode;
    }

    @Override
    public void setState(QuadcopterEntity.State state) {
        this.state = state;
    }

    @Override
    public QuadcopterEntity.State getState() {
        return this.state;
    }

    @Override
    public void setBindId(int bindId) {
        this.bindId = bindId;
    }

    @Override
    public int getBindId() {
        return bindId;
    }

    @Override
    public void setInputFrame(InputFrame frame) {

    }

    public InputFrame getInputFrame() {
        return null;
    }

    @Override
    public void setFrequency(Frequency frequency) {
        this.frequency.set(frequency);
    }

    @Override
    public Frequency getFrequency() {
        return frequency;
    }

    @Override
    public void setPower(int power) {
        this.power = power;
    }

    @Override
    public int getPower() {
        return this.power;
    }

    @Override
    public void setFieldOfView(int fieldOfView) {
        this.fieldOfView = fieldOfView;
    }

    @Override
    public int getFieldOfView() {
        return this.fieldOfView;
    }

    @Override
    public void setCameraAngle(int cameraAngle) {
        this.cameraAngle = cameraAngle;
    }

    @Override
    public int getCameraAngle() {
        return this.cameraAngle;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof QuadcopterContainer) {
            return getBindId() == ((QuadcopterContainer) obj).getBindId();
        }

        return false;
    }
}

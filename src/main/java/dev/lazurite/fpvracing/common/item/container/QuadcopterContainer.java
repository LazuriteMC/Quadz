package dev.lazurite.fpvracing.common.item.container;

import dev.lazurite.fpvracing.FPVRacing;
import dev.lazurite.fpvracing.client.input.InputFrame;
import dev.lazurite.fpvracing.common.entity.QuadcopterEntity;
import dev.lazurite.fpvracing.common.entity.component.QuadcopterProperties;
import dev.lazurite.fpvracing.common.util.Frequency;
import dev.onyxstudios.cca.api.v3.component.ComponentV3;
import dev.onyxstudios.cca.api.v3.component.sync.AutoSyncedComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;

public class QuadcopterContainer implements ComponentV3, AutoSyncedComponent, QuadcopterProperties {
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

    public static QuadcopterContainer get(ItemStack stack) {
        try {
            return FPVRacing.QUADCOPTER_CONTAINER.get(stack);
        } catch (Exception e) {
            return null;
        }
    }

    public ItemStack getStack() {
        return this.stack;
    }

    @Override
    public void applySyncPacket(PacketByteBuf buf) {
        state = buf.readEnumConstant(QuadcopterEntity.State.class);
        godMode = buf.readBoolean();
        bindId = buf.readInt();

        frequency.setBand(buf.readChar());
        frequency.setChannel(buf.readInt());
        cameraAngle = buf.readInt();
        fieldOfView = buf.readInt();
        power = buf.readInt();
    }

    @Override
    public void writeSyncPacket(PacketByteBuf buf, ServerPlayerEntity recipient) {
        buf.writeEnumConstant(state);
        buf.writeBoolean(godMode);
        buf.writeInt(bindId);

        buf.writeChar(frequency.getBand());
        buf.writeInt(frequency.getChannel());
        buf.writeInt(cameraAngle);
        buf.writeInt(fieldOfView);
        buf.writeInt(power);
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
}

package dev.lazurite.fpvracing.common.entity.component;

import dev.lazurite.fpvracing.FPVRacing;
import dev.lazurite.fpvracing.client.input.InputFrame;
import dev.lazurite.fpvracing.client.input.InputTick;
import dev.onyxstudios.cca.api.v3.component.ComponentV3;
import dev.onyxstudios.cca.api.v3.component.sync.AutoSyncedComponent;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;

public class ReceiverComponent implements ComponentV3, AutoSyncedComponent {
    private final Entity entity;
    private final InputFrame frame;
    private int bindId;

    public ReceiverComponent(Entity entity) {
        this.entity = entity;
        this.frame = new InputFrame();
    }

    public static ReceiverComponent get(Entity entity) {
        try {
            return FPVRacing.RECEIVER.get(entity);
        } catch (Exception e) {
            return null;
        }
    }

    public void step(float delta) {
        if (getEntity().getEntityWorld().isClient()) {
            getFrame().set(InputTick.INSTANCE.getFrame());
        }
    }

    public static boolean is(Entity entity) {
        return get(entity) != null;
    }

    public void bind(int bindId) {
        this.bindId = bindId;
    }

    public InputFrame getFrame() {
        return this.frame;
    }

    public int getBindId() {
        return this.bindId;
    }

    public Entity getEntity() {
        return this.entity;
    }

    @Override
    public void applySyncPacket(PacketByteBuf buf) {
        getFrame().set(InputFrame.fromBuffer(buf));
        bindId = buf.readInt();
    }

    @Override
    public void writeSyncPacket(PacketByteBuf buf, ServerPlayerEntity recipient) {
        getFrame().toBuffer(buf);
        buf.writeInt(bindId);
    }

    @Override
    public void readFromNbt(CompoundTag tag) {
        getFrame().set(InputFrame.fromTag(tag));
        bindId = tag.getInt("bind_id");
    }

    @Override
    public void writeToNbt(CompoundTag tag) {
        getFrame().toTag(tag);
        tag.putInt("bind_id", bindId);
    }
}

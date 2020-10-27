package bluevista.fpvracing.network.datatracker.datatype;

import bluevista.fpvracing.config.Config;
import net.minecraft.entity.data.TrackedDataHandler;
import net.minecraft.nbt.CompoundTag;

public interface FlyableDataType<T> extends TrackedDataHandler<T> {
    void toTag(CompoundTag tag, String key, T value);
    T fromTag(CompoundTag tag, String key);

    void toConfig(Config config, String key, T value);
    T fromConfig(Config config, String key);
}

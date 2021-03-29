package dev.lazurite.quadz.common.util.type;

import java.util.Random;

public interface Bindable {
    static void bind(Bindable b1, Bindable b2) {
        Random random = new Random();
        int bindId = random.nextInt(10000);
        b1.setBindId(bindId);
        b2.setBindId(bindId);
    }

    default boolean isBoundTo(Bindable bindable) {
        return isBoundTo(bindable.getBindId());
    }

    default boolean isBoundTo(int bindId) {
        return getBindId() != -1 && getBindId() == bindId;
    }

    void setBindId(int bindId);
    int getBindId();
}

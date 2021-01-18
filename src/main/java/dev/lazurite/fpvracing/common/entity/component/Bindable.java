package dev.lazurite.fpvracing.common.entity.component;

import java.util.Random;

public interface Bindable {
    static void bind(Bindable b1, Bindable b2) {
        Random random = new Random();
        int bindId = random.nextInt(10000);
        b1.setBindId(bindId);
        b2.setBindId(bindId);
    }

    void setBindId(int bindId);
    int getBindId();
}

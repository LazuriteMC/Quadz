package dev.lazurite.quadz.common.util;

import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;

public interface FloatBufferUtil {

    static List<Float> toArray(FloatBuffer floatBuffer) {
        var out = new ArrayList<Float>();
        floatBuffer.rewind();

        while (floatBuffer.hasRemaining()) {
            out.add(floatBuffer.get());
        }

        return out;
    }

}

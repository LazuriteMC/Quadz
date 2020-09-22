package bluevista.fpvracingmod.network;

import javax.vecmath.Quat4f;

public class NetQuat4f {
    private Quat4f target;
    private Quat4f prev;

    public NetQuat4f(Quat4f target) {
        this.target = new Quat4f();
        this.prev = new Quat4f();

        this.set(target);
        this.setPrev(target);
    }

    public void set(Quat4f target) {
        this.target.set(target);
    }

    public void setPrev(Quat4f prev) {
        this.prev.set(prev);
    }

    public Quat4f get() {
        return this.target;
    }

    public Quat4f slerp(float tickDelta) {
        Quat4f out = new Quat4f();
        out.interpolate(prev, target, tickDelta);
        return out;
    }
}

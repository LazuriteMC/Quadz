package bluevista.fpvracing.util;

public class TickTimer {
    private int ticks;
    private int count;

    public TickTimer(int ticks) {
        this.ticks = ticks;
        this.count = 0;
    }

    public boolean tick() {
        if (ticks > count) {
            ticks = 0;
            return true;
        }

        ++ticks;
        return false;
    }
}

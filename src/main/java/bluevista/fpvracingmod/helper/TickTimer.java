package bluevista.fpvracingmod.helper;

public class TickTimer {
    private int count;
    private int time;

    public TickTimer(int time) {
        this.time = time;
    }

    public boolean tick() {
        if(count > time) {
            count = 0;
            return true;
        }

        count++;
        return false;
    }
}

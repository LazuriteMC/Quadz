package bluevista.fpvracingmod.network;

import java.util.LinkedList;
import java.util.Queue;

public class GenericBuffer<T> {
    private Queue<T> buffer;
    private T prevValue;
    private float captureRate;
    private int maxLength;

    private float runningDelta;

    public GenericBuffer() {
        this(16);
    }

    public GenericBuffer(int length) {
        this.buffer = new LinkedList();
        this.setMaxLength(length);
        this.setCaptureRate(1);
    }

    public T getHead() {
        return this.buffer.element();
    }

    public void add(T value) {
        this.buffer.add(value);
        if(this.buffer.size() > this.maxLength) {
            buffer.remove();
        }
    }

    public T poll(float d) {
        T value = null;

//        System.out.println("Running Delta BEFORE: " +  runningDelta);
        runningDelta += d;
//        System.out.println("Running Delta AFTER:  " +  runningDelta);

//        System.out.println("Delta: " + d);
//        System.out.println("Capture Rate: " + captureRate);

        if(runningDelta >= captureRate) {
//            System.out.println("kill");
            float div = runningDelta / captureRate;

//            for(int i = 0; i < div; i++) {
                if (this.size() > 0) {
                    value = this.buffer.poll();
                }
//            }

//            runningDelta -= captureRate;
            runningDelta = 0;
        }

        prevValue = value;
        return value;
    }

    public T getLast() {
        return this.prevValue;
    }

    public void setCaptureRate(float captureRate) {
        this.captureRate = captureRate;
    }

    public float getCaptureRate() {
        return this.captureRate;
    }

    public int size() {
        return this.buffer.size();
    }

    public void setMaxLength(int length) {
        this.maxLength = length;
    }

    public int getMaxLength() {
        return this.maxLength;
    }

    public Queue<T> getQueue() {
        return this.buffer;
    }

    public void set(GenericBuffer genericBuffer) {
        this.setMaxLength(genericBuffer.getMaxLength());
        this.setCaptureRate(genericBuffer.getCaptureRate());
        this.buffer = genericBuffer.getQueue();
    }
}

package me.duncanruns.seedcycle;

import java.util.Random;

public class CountedRandom extends Random {

    protected int nextCount;

    public CountedRandom() {
        super();
        this.nextCount = 0;
    }

    public CountedRandom(long seed, int count) {
        this.nextCount = 0;
        setSeed(seed, count);
    }

    public int getCount() {
        return nextCount;
    }

    public void setSeed(long seed, int count) {
        setSeed(seed);
        nextCount = 0;
        for (int i = 0; i < count; i++) {
            next(1);
        }
    }

    @Override
    protected int next(int bits) {
        nextCount++;
        return super.next(bits);
    }
}

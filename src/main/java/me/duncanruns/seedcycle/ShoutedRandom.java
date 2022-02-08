package me.duncanruns.seedcycle;

// Class for debugging seed based RNG. Should be unused in released mods.
public class ShoutedRandom extends CountedRandom {

    public ShoutedRandom(long seed, int count) {
        super(seed, count);
        System.out.println("SHOUTEDRANDOM > Constructor - " + seed + " - " + count);
    }

    @Override
    protected int next(int bits) {
        int x = super.next(bits);
        System.out.println("SHOUTEDRANDOM > " + nextCount + " - " + bits + " - " + x);
        return x;
    }
}

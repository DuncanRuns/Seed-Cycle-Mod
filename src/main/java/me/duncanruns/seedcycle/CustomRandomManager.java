package me.duncanruns.seedcycle;

public class CustomRandomManager {
    public final CountedRandom blazeRandom, spawnerRandom, gravelRandom, eyeRandom, barterRandom, endermanRandom;

    public CustomRandomManager(long seed, RNGCounterInfo rngCounterInfo) {
        blazeRandom = new CountedRandom(seed, rngCounterInfo.blaze);
        spawnerRandom = new CountedRandom(seed, rngCounterInfo.spawner);
        gravelRandom = new CountedRandom(seed, rngCounterInfo.gravel);
        eyeRandom = new CountedRandom(seed, rngCounterInfo.eye);
        barterRandom = new CountedRandom(seed, rngCounterInfo.barter);
        endermanRandom = new CountedRandom(seed, rngCounterInfo.enderman);
    }

}

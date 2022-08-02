package me.duncanruns.seedcycle;

public class CustomRandomManager {
    public final CountedRandom blazeRandom, spawnerRandom, gravelRandom, eyeRandom, barterRandom, endermanRandom;

    public CustomRandomManager(long seed, RNGInfo rngInfo) {
        blazeRandom = new CountedRandom(seed, rngInfo.blaze);
        spawnerRandom = new CountedRandom(seed, rngInfo.spawner);
        gravelRandom = new CountedRandom(seed, rngInfo.gravel);
        eyeRandom = new CountedRandom(seed, rngInfo.eye);
        barterRandom = new CountedRandom(seed, rngInfo.barter);
        endermanRandom = new CountedRandom(seed, rngInfo.enderman);
    }

}

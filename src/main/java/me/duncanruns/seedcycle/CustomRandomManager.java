package me.duncanruns.seedcycle;

public class CustomRandomManager {
    public final CountedRandom blazeRandom, spawnerRandom, gravelRandom, eyeRandom, barterRandom, endermanRandom;
    public long dropSeed;

    public CustomRandomManager(RNGInfo rngInfo) {
        blazeRandom = new CountedRandom();
        spawnerRandom = new CountedRandom();
        gravelRandom = new CountedRandom();
        eyeRandom = new CountedRandom();
        barterRandom = new CountedRandom();
        endermanRandom = new CountedRandom();
        dropSeed = 0L;
        provideRNGInfo(rngInfo);
    }

    public void provideRNGInfo(RNGInfo rngInfo) {
        dropSeed = rngInfo.dropSeed;
        blazeRandom.setSeed(dropSeed, rngInfo.blaze);
        spawnerRandom.setSeed(dropSeed, rngInfo.spawner);
        gravelRandom.setSeed(dropSeed, rngInfo.gravel);
        eyeRandom.setSeed(dropSeed, rngInfo.eye);
        barterRandom.setSeed(dropSeed, rngInfo.barter);
        endermanRandom.setSeed(dropSeed, rngInfo.enderman);
    }

}

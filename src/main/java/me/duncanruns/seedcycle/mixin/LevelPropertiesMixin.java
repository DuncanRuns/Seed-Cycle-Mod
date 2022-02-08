package me.duncanruns.seedcycle.mixin;

import com.mojang.datafixers.DataFixer;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.Lifecycle;
import me.duncanruns.seedcycle.CRWorldProperties;
import me.duncanruns.seedcycle.RNGCounterInfo;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.util.registry.RegistryTracker;
import net.minecraft.world.gen.GeneratorOptions;
import net.minecraft.world.level.LevelInfo;
import net.minecraft.world.level.LevelProperties;
import net.minecraft.world.level.storage.SaveVersionInfo;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LevelProperties.class)
public abstract class LevelPropertiesMixin implements CRWorldProperties {
    private RNGCounterInfo rngCounterInfo;

    @Inject(method = "method_29029", at = @At("RETURN"))
    private static void readLevelDatMixin(Dynamic<Tag> dynamic, DataFixer dataFixer, int i, @Nullable CompoundTag compoundTag, LevelInfo levelInfo, SaveVersionInfo saveVersionInfo, GeneratorOptions generatorOptions, Lifecycle lifecycle, CallbackInfoReturnable<LevelProperties> info) {
        LevelProperties levelProperties = info.getReturnValue();
        RNGCounterInfo rngCounterInfo = new RNGCounterInfo();
        rngCounterInfo.barter = dynamic.get("BarterCount").asInt(0);
        rngCounterInfo.blaze = dynamic.get("BlazeCount").asInt(0);
        rngCounterInfo.eye = dynamic.get("EyeCount").asInt(0);
        rngCounterInfo.gravel = dynamic.get("GravelCount").asInt(0);
        rngCounterInfo.spawner = dynamic.get("SpawnerCount").asInt(0);
        rngCounterInfo.enderman = dynamic.get("EndermanCount").asInt(0);
        ((CRWorldProperties) levelProperties).setRCI(rngCounterInfo);
    }

    @Override
    public RNGCounterInfo getRCI() {
        return rngCounterInfo;
    }

    @Override
    public void setRCI(RNGCounterInfo rngCounterInfo) {
        this.rngCounterInfo = rngCounterInfo;
    }

    @Inject(method = "updateProperties", at = @At("TAIL"))
    private void addLevelDat(RegistryTracker registryTracker, CompoundTag compoundTag, CompoundTag compoundTag2, CallbackInfo info) {
        if (rngCounterInfo == null) {
            rngCounterInfo = new RNGCounterInfo();
        }
        compoundTag.putInt("BarterCount", rngCounterInfo.barter);
        compoundTag.putInt("BlazeCount", rngCounterInfo.blaze);
        compoundTag.putInt("EyeCount", rngCounterInfo.eye);
        compoundTag.putInt("GravelCount", rngCounterInfo.gravel);
        compoundTag.putInt("SpawnerCount", rngCounterInfo.spawner);
        compoundTag.putInt("EndermanCount", rngCounterInfo.enderman);
    }
}

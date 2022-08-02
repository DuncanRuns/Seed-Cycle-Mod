package me.duncanruns.seedcycle.mixin;

import com.mojang.datafixers.DataFixer;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.Lifecycle;
import me.duncanruns.seedcycle.CRWorldProperties;
import me.duncanruns.seedcycle.RNGInfo;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.util.registry.RegistryTracker;
import net.minecraft.world.gen.GeneratorOptions;
import net.minecraft.world.level.LevelInfo;
import net.minecraft.world.level.LevelProperties;
import net.minecraft.world.level.storage.SaveVersionInfo;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LevelProperties.class)
public abstract class LevelPropertiesMixin implements CRWorldProperties {
    @Shadow
    public abstract GeneratorOptions getGeneratorOptions();

    private RNGInfo rngInfo;

    @Inject(method = "method_29029", at = @At("RETURN"))
    private static void readLevelDatMixin(Dynamic<Tag> dynamic, DataFixer dataFixer, int i, @Nullable CompoundTag compoundTag, LevelInfo levelInfo, SaveVersionInfo saveVersionInfo, GeneratorOptions generatorOptions, Lifecycle lifecycle, CallbackInfoReturnable<LevelProperties> info) {
        LevelProperties levelProperties = info.getReturnValue();
        RNGInfo rngInfo = new RNGInfo();
        rngInfo.dropSeed = dynamic.get("DropSeed").asLong(levelProperties.getGeneratorOptions().getSeed());
        rngInfo.barter = dynamic.get("BarterCount").asInt(0);
        rngInfo.blaze = dynamic.get("BlazeCount").asInt(0);
        rngInfo.eye = dynamic.get("EyeCount").asInt(0);
        rngInfo.gravel = dynamic.get("GravelCount").asInt(0);
        rngInfo.spawner = dynamic.get("SpawnerCount").asInt(0);
        rngInfo.enderman = dynamic.get("EndermanCount").asInt(0);
        ((CRWorldProperties) levelProperties).setRI(rngInfo);
    }

    @Override
    public RNGInfo getRI() {
        return rngInfo;
    }

    @Override
    public void setRI(RNGInfo rngInfo) {
        this.rngInfo = rngInfo;
    }

    @Inject(method = "updateProperties", at = @At("TAIL"))
    private void addLevelDat(RegistryTracker registryTracker, CompoundTag compoundTag, CompoundTag compoundTag2, CallbackInfo info) {
        if (rngInfo == null) {
            rngInfo = new RNGInfo();
            rngInfo.dropSeed = getGeneratorOptions().getSeed();
        }
        compoundTag.putLong("DropSeed", rngInfo.dropSeed);
        compoundTag.putInt("BarterCount", rngInfo.barter);
        compoundTag.putInt("BlazeCount", rngInfo.blaze);
        compoundTag.putInt("EyeCount", rngInfo.eye);
        compoundTag.putInt("GravelCount", rngInfo.gravel);
        compoundTag.putInt("SpawnerCount", rngInfo.spawner);
        compoundTag.putInt("EndermanCount", rngInfo.enderman);
    }
}

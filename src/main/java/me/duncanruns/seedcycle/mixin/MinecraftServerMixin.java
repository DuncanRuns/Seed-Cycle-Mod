package me.duncanruns.seedcycle.mixin;

import com.mojang.authlib.GameProfileRepository;
import com.mojang.authlib.minecraft.MinecraftSessionService;
import com.mojang.datafixers.DataFixer;
import me.duncanruns.seedcycle.CRMOwner;
import me.duncanruns.seedcycle.CRWorldProperties;
import me.duncanruns.seedcycle.CustomRandomManager;
import me.duncanruns.seedcycle.RNGCounterInfo;
import net.minecraft.resource.ResourcePackManager;
import net.minecraft.resource.ServerResourceManager;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.WorldGenerationProgressListenerFactory;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.util.UserCache;
import net.minecraft.util.registry.RegistryTracker;
import net.minecraft.world.GameRules;
import net.minecraft.world.SaveProperties;
import net.minecraft.world.level.storage.LevelStorage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.net.Proxy;

@Mixin(MinecraftServer.class)
public abstract class MinecraftServerMixin implements CRMOwner {
    private CustomRandomManager customRandomManager;

    @Shadow
    public abstract SaveProperties getSaveProperties();

    @Shadow
    public abstract boolean save(boolean bl, boolean bl2, boolean bl3);

    @Shadow
    public abstract GameRules getGameRules();

    @Shadow
    public abstract CommandManager getCommandManager();

    @Shadow
    public abstract ServerCommandSource getCommandSource();

    @Inject(method = "<init>", at = @At("TAIL"))
    private void addCRMMixin(Thread thread, RegistryTracker.Modifiable modifiable, LevelStorage.Session session, SaveProperties saveProperties, ResourcePackManager resourcePackManager, Proxy proxy, DataFixer dataFixer, ServerResourceManager serverResourceManager, MinecraftSessionService minecraftSessionService, GameProfileRepository gameProfileRepository, UserCache userCache, WorldGenerationProgressListenerFactory worldGenerationProgressListenerFactory, CallbackInfo info) {
        customRandomManager = new CustomRandomManager(getSaveProperties().getGeneratorOptions().getSeed(), ((CRWorldProperties) saveProperties).getRCI());
    }

    @Override
    public CustomRandomManager getCRM() {
        return customRandomManager;
    }

    @Inject(method = "save", at = @At("HEAD"))
    private void saveRCIMixin(boolean bl, boolean bl2, boolean bl3, CallbackInfoReturnable<Boolean> cir) {
        RNGCounterInfo rngCounterInfo = new RNGCounterInfo();
        CustomRandomManager customRandomManager = ((CRMOwner) this).getCRM();
        rngCounterInfo.barter = customRandomManager.barterRandom.getCount();
        rngCounterInfo.blaze = customRandomManager.blazeRandom.getCount();
        rngCounterInfo.eye = customRandomManager.eyeRandom.getCount();
        rngCounterInfo.gravel = customRandomManager.gravelRandom.getCount();
        rngCounterInfo.spawner = customRandomManager.spawnerRandom.getCount();
        rngCounterInfo.enderman = customRandomManager.endermanRandom.getCount();
        ((CRWorldProperties) getSaveProperties()).setRCI(rngCounterInfo);
    }

    @Inject(method = "loadWorld", at = @At("TAIL"))
    private void setSpawnRadiusMixin(CallbackInfo info) {
        getCommandManager().execute(getCommandSource(), "gamerule spawnRadius 0");
    }
}

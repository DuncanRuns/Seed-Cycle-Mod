package me.duncanruns.seedcycle.mixin;

import com.mojang.brigadier.CommandDispatcher;
import me.duncanruns.seedcycle.MakeDropSeedCommand;
import me.duncanruns.seedcycle.SetDropSeedCommand;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CommandManager.class)
public abstract class CommandManagerMixin {
    @Shadow @Final private CommandDispatcher<ServerCommandSource> dispatcher;

    @Inject(method = "<init>",at=@At("TAIL"))
    private void addNewCommands(CommandManager.RegistrationEnvironment environment, CallbackInfo info){
        MakeDropSeedCommand.register(this.dispatcher);
        SetDropSeedCommand.register(this.dispatcher);
    }
}

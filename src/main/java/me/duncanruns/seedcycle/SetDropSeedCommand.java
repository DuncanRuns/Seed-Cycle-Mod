package me.duncanruns.seedcycle;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.LongArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.LiteralText;

public abstract class SetDropSeedCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("setdropseed").requires(serverCommandSource -> serverCommandSource.hasPermissionLevel(3)).then(CommandManager.argument("seed", LongArgumentType.longArg()).executes(context -> execute(context.getSource(), LongArgumentType.getLong(context, "seed")))));
    }

    private static int execute(ServerCommandSource commandSource, long seed) {
        RNGInfo rngInfo = new RNGInfo();
        rngInfo.dropSeed = seed;
        ((CRMOwner) commandSource.getMinecraftServer()).getCRM().provideRNGInfo(rngInfo);
        commandSource.sendFeedback(new LiteralText("Set the drop seed to " + seed + " and reset all rng counters back to 0"), true);
        return (int) seed;
    }
}

package me.duncanruns.seedcycle;


import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.mob.BlazeEntity;
import net.minecraft.entity.mob.PiglinEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.LootTables;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.loot.context.LootContextTypes;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.*;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

import java.util.List;
import java.util.Objects;
import java.util.Random;

public abstract class MakeDropSeedCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("makedropseed").requires(serverCommandSource -> serverCommandSource.hasPermissionLevel(3)).then(
                CommandManager.argument("gold", IntegerArgumentType.integer(0, 250)).then(
                        CommandManager.literal("ingots").then(getSearchArgs(false))
                ).then(
                        CommandManager.literal("blocks").then(getSearchArgs(true))
                )
        ));
    }

    private static int execute(ServerCommandSource commandSource, boolean isBlocks, int gold, int beds, int anchors, int obsidian, int pearls, int maxObsidian) {
        Random dropSeedGen = new Random();
        for (int tries = 0; tries < 1000000; tries++) {
            long dropSeed = dropSeedGen.nextLong() + 1L;
            if (checkOther(commandSource, dropSeed)) {

                PiglinEntity piglinEntity = new PiglinEntity(EntityType.PIGLIN, commandSource.getWorld());

                int totalTrades = isBlocks ? gold * 9 : gold;

                int foundString = 0;
                int foundGlowstone = 0;
                int foundCrying = 0;
                int foundObsidian = 0;
                int foundPearls = 0;
                int foundPotion = 0;

                Random random = new Random();
                random.setSeed(dropSeed);

                for (int i = 0; i < totalTrades; i++) {
                    for (ItemStack itemStack : getBarteredItem(piglinEntity, random)) {
                        if (Items.ENDER_PEARL.equals(itemStack.getItem())) {
                            foundPearls += itemStack.getCount();
                        } else if (Items.GLOWSTONE_DUST.equals(itemStack.getItem())) {
                            foundGlowstone += itemStack.getCount();
                        } else if (Items.CRYING_OBSIDIAN.equals(itemStack.getItem())) {
                            foundCrying += itemStack.getCount();
                        } else if (Items.STRING.equals(itemStack.getItem())) {
                            foundString += itemStack.getCount();
                        } else if (Items.OBSIDIAN.equals(itemStack.getItem())) {
                            foundObsidian += itemStack.getCount();
                        } else if (Items.POTION.equals(itemStack.getItem()) ||
                                Items.SPLASH_POTION.equals(itemStack.getItem())) {
                            foundPotion += 1;
                        }
                    }
                }
                if (foundPotion >= 2 &&
                        foundString >= (beds * 12) &&
                        foundGlowstone >= (anchors * 16) &&
                        foundCrying >= (anchors * 6) &&
                        foundObsidian >= obsidian &&
                        (maxObsidian == -1 || foundObsidian <= maxObsidian) &&
                        foundPearls >= pearls
                ) {
                    Text text = Texts.bracketed((new LiteralText(String.valueOf(dropSeed))).styled((style) -> style.withColor(Formatting.GREEN).withClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, String.valueOf(dropSeed))).setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TranslatableText("chat.copy.click"))).withInsertion(String.valueOf(dropSeed))));
                    commandSource.sendFeedback(new TranslatableText("commands.seed.success", text), false);

                    return 1;
                }

            }
        }
        commandSource.sendFeedback(new LiteralText("Could not find seed within 1 million tries").formatted(Formatting.RED), false);
        return 0;
    }

    private static boolean checkOther(ServerCommandSource commandSource, final long seed) {
        Random random = new Random();

        // Blazes must drop 8 rods in 13 kills

        BlazeEntity blazeEntity = new BlazeEntity(EntityType.BLAZE, commandSource.getWorld());
        Identifier blazeIdentifier = Registry.ENTITY_TYPE.getId(EntityType.BLAZE);
        Identifier blazeLootTableId = new Identifier(blazeIdentifier.getNamespace(), "entities/" + blazeIdentifier.getPath());

        random.setSeed(seed);

        int blazeCount = 0;
        for (int i = 0; i < 13; i++) {
            blazeCount += getBlazeDrop(blazeEntity, random, blazeLootTableId).get(0).getCount();
        }
        if (blazeCount >= 8) {

            // First 3 eyes must drop and the next 10 throws must have at least 8 drops.
            random.setSeed(seed);
            if (random.nextInt(5) > 0 && random.nextInt(5) > 0 && random.nextInt(5) > 0) {
                int dropCount = 0;
                for (int i = 0; i < 10; i++) {
                    dropCount += random.nextInt(5) > 0 ? 1 : 0;
                }
                return dropCount >= 8;
            }
        }
        return false;
    }

    private static List<ItemStack> getBarteredItem(PiglinEntity piglin, Random random) {
        LootTable lootTable = Objects.requireNonNull(piglin.world.getServer()).getLootManager().getTable(LootTables.PIGLIN_BARTERING_GAMEPLAY);
        return lootTable.generateLoot((new LootContext.Builder((ServerWorld) piglin.world)).parameter(LootContextParameters.THIS_ENTITY, piglin).random(random).build(LootContextTypes.BARTER));
    }

    private static List<ItemStack> getBlazeDrop(BlazeEntity blaze, Random random, Identifier lootTableId) {
        LootTable lootTable = Objects.requireNonNull(blaze.world.getServer()).getLootManager().getTable(lootTableId);
        LootContext.Builder builder = getLootContextBuilder(DamageSource.player(MinecraftClient.getInstance().player), blaze, random);
        return lootTable.generateLoot(builder.build(LootContextTypes.ENTITY));
    }

    private static LootContext.Builder getLootContextBuilder(DamageSource source, BlazeEntity blazeEntity, Random random) {
        LootContext.Builder builder = (new LootContext.Builder((ServerWorld) blazeEntity.getEntityWorld())).random(random).parameter(LootContextParameters.THIS_ENTITY, blazeEntity).parameter(LootContextParameters.POSITION, blazeEntity.getBlockPos()).parameter(LootContextParameters.DAMAGE_SOURCE, source).optionalParameter(LootContextParameters.KILLER_ENTITY, source.getAttacker()).optionalParameter(LootContextParameters.DIRECT_KILLER_ENTITY, source.getSource());
        assert MinecraftClient.getInstance().player != null;
        builder = builder.parameter(LootContextParameters.LAST_DAMAGE_PLAYER, MinecraftClient.getInstance().player).luck(MinecraftClient.getInstance().player.getLuck());
        return builder;
    }

    private static RequiredArgumentBuilder<ServerCommandSource, Integer> getSearchArgs(boolean isBlocks) {
        return CommandManager.argument("beds", IntegerArgumentType.integer(0, 10)).then(
                CommandManager.argument("anchors", IntegerArgumentType.integer(0, 10)).then(
                        CommandManager.argument("obsidian", IntegerArgumentType.integer(0, 20)).then(
                                CommandManager.argument("pearls", IntegerArgumentType.integer(0, 50)).executes(context -> execute(
                                        context.getSource(),
                                        isBlocks,
                                        IntegerArgumentType.getInteger(context, "gold"),
                                        IntegerArgumentType.getInteger(context, "beds"),
                                        IntegerArgumentType.getInteger(context, "anchors"),
                                        IntegerArgumentType.getInteger(context, "obsidian"),
                                        IntegerArgumentType.getInteger(context, "pearls"),
                                        -1
                                )).then(CommandManager.argument("max_obsidian", IntegerArgumentType.integer(0, 20)).executes(context -> execute(
                                        context.getSource(),
                                        isBlocks,
                                        IntegerArgumentType.getInteger(context, "gold"),
                                        IntegerArgumentType.getInteger(context, "beds"),
                                        IntegerArgumentType.getInteger(context, "anchors"),
                                        IntegerArgumentType.getInteger(context, "obsidian"),
                                        IntegerArgumentType.getInteger(context, "pearls"),
                                        IntegerArgumentType.getInteger(context, "max_obsidian")))
                                )
                        )
                )
        );
    }
}
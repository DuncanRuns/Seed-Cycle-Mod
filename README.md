 # Seed Cycle Mod

The seed cycle mod is a fabric mod made for the speedrunning challenge/competition "Seed Cycle" (https://mcseedcycle.com/).

The mod is not specifically made for Seed Cycle and can be used by anyone looking to play a seed against each other or host a tournament/competition themselves.
 
The mod includes various features with the main goal of standardizing RNG.

# Usage/Installation

Once installed, the mod will work for any worlds.

The Seed Cycle Mod has no requirements other than the fabric loader.

A tutorial for [installing the fabric loader and fabric mods can be found here](https://fabricmc.net/wiki/player:tutorials:install_mcl:windows).
Seed Cycle's fabric mod jar can be found on the [releases page](https://github.com/duncanRuns/Seed-Cycle-Mod/releases).
Please make sure to skip the step of installing Fabric API, as it is not required for speedrun related mods and is banned for speedrunning and related competitions.

# Full Feature List

### Ender Dragon Standardization

In a nutshell, the mod will force the dragon to perch after 90 seconds.

A timer starting at 0 starts ticking up towards 1800 ticks (90 seconds).
During this counting, any natural perch will **pause** the timer, though it is unlikely to get one during the first 90 seconds.
Once the dragon has completed any perch-related phases, the timer will continue to tick upwards.
Once the timer reaches 1800 ticks (90 seconds), the timer will then set to -1 and the dragon will perch.
Once the dragon has completed the perch-related phases, the timer will then start again going up to 1800 ticks (90 seconds) when it will perch again.

### Standardized Loot Tables

The following loot tables each have been made seed based to ensure standardization across multiple playthroughs of the same seed:

- Endermen
- Blazes
- Piglin Barters
- Gravel

Additionally, the iron golem loot table has been standardized by having a set drop of 5 iron ingots.

### Standardized Eye Breaks

The RNG call for what determines an eye break has been made seed based to ensure standardization across multiple playthroughs of the same seed. 

### Blaze Spawner Standardization

This is perhaps the most advanced aspect of the mod. For a player, there are a few things to know:

- If you do the same thing, you will very likely get the same result.
- You will benifit from doing strategies found in RSG.
- Despite the standardization of the blaze spawner, it is difficult to abuse this fact, and you will definitely benifit from clearing out the area that a spawner could potentially spawn as if it were RSG.
- Clearing out every possible block that blazes can spawn will guarantee you 4 blazes per cycle.

A detailed explanation:

The randomization for blaze spawners over the playthrough of a seed can be split into "spawn cycles".
Spawn cycles do 2 things: spawn blazes, and determine the spawner's next activation delay.
When a spawner activates, it will run a spawn cycle.

With the seed cycle mod, it is important to remember that all spawners share the same set of spawn cycles. The first spawn cycle can only be claimed by the first activation of the first spawner you go to; the second spawn cycle will be the activation of any spawner after the first activation has already been done.

The first spawn cycle will always have the exact same pattern of blaze spawns (positions of blazes) based on the seed. However, after the first cycle, spawn positions of blazes once again becomes completely random with the exception that blazes attempting to spawn inside the spawner block itself will retry. Additionally, a blaze spawned in blaze cycle cannot prevent the spawning of another blaze. This ensures clearing out blocks comes with great benifits such as guaranteed 4 blazes when all blocks are cleared.

While the spawn position of blazes isn't completely seed dependent, the delays for the next activation of the spawners will always follow a pattern based on the seed. For example, seed `2954846` may generate the following delays: 20.55s, 10.95s, 39.8s, 16.65s. Using this seed means that a spawner running the first seed cycle will then wait 20.55s before activating again and claiming another cycle. A spawner running the second seed cycle, regardless of what it has done before will have to wait 10.95s before next activation. And so on.

### Relog Protection

Usage of the seed based standardization is saved in the world file and is uninterrupted by the world being reloaded.

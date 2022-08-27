package me.duncanruns.seedcycle.mixin;

import me.duncanruns.seedcycle.CRMOwner;
import me.duncanruns.seedcycle.CountedRandom;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.SpawnRestriction;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.WeightedPicker;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.MobSpawnerEntry;
import net.minecraft.world.MobSpawnerLogic;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.*;

@Mixin(MobSpawnerLogic.class)
public abstract class MobSpawnerLogicMixin {
    private static final boolean ALWAYS_RANDOM_POSITIONS = true;

    @Shadow
    private MobSpawnerEntry spawnEntry;
    @Shadow
    private int spawnDelay;
    @Shadow
    private int spawnCount;
    @Shadow
    private int spawnRange;
    @Shadow
    private int maxSpawnDelay;
    @Shadow
    private int minSpawnDelay;
    @Shadow
    @Final
    private List<MobSpawnerEntry> spawnPotentials;
    @Shadow
    private double field_9159;
    @Shadow
    private double field_9161;

    @Shadow
    public abstract World getWorld();

    @Shadow
    @Nullable
    protected abstract Identifier getEntityId();


    @Shadow
    protected abstract void spawnEntity(Entity entity);

    @Shadow
    public abstract BlockPos getPos();

    @Shadow
    public abstract void setSpawnEntry(MobSpawnerEntry spawnEntry);

    @Shadow
    public abstract void sendStatus(int var1);

    @Shadow
    protected abstract boolean isPlayerInRange();

    @Inject(method = "update", at = @At("HEAD"), cancellable = true)
    private void standardizeBlazeMixin(CallbackInfo info) {
        /*
        A couple notes about the difference between vanilla and this method:
        - lots of seed-based rng obviously
        - storing spawn positions in a list before spawning to stop 2 or more blazes from the same cycle preventing each
          other from spawning when they collide
        - lots of jank
         */


        World world = getWorld();
        // If running server-side code and the spawner is a blaze spawner
        if (!world.isClient && "minecraft:blaze".equals(Objects.requireNonNull(getEntityId()).toString())) {
            // Cancel original function to override with this custom code
            info.cancel();

            // Code found in the regular spawner code. Should inject at a later point to have less copy-paste from mojang
            if (!this.isPlayerInRange()) {
                this.field_9159 = this.field_9161;
                return;
            }
            BlockPos blockPos = this.getPos();
            if (this.spawnDelay == -1) {
                updateSpawnsStandard();
            }
            if (this.spawnDelay > 0) {
                --this.spawnDelay;
                // If there is still spawnDelay, return out of the method until the spawnDelay is 0
                return;
                // Anything below here runs when we want to run a blaze spawn cycle
            }
            boolean doSpawnUpdate = false;

            // Get spawnerRandom, a random object used specifically for blaze spawners
            CountedRandom spawnerRandom = ((CRMOwner) world.getServer()).getCRM().spawnerRandom;
            // Get worldRandom, a random object used for many of the world's random calls, useful for when things shouldn't be seed based
            Random worldRandom = world.getRandom();

            // spawnerRandom is a CountedRandom object, so we can check how much it has been used, and determine if this is the first time blazes would spawn
            boolean isFirstSpawn = spawnerRandom.getCount() <= 0;

            // Create a list to store the positions we want to spawn blazes at
            List<Vec3d> toSpawn = new ArrayList<>();
            for (int i = 0; i < this.spawnCount; ++i) {
                CompoundTag entityTag = this.spawnEntry.getEntityTag();
                Optional<EntityType<?>> optional = EntityType.fromTag(entityTag);
                if (!optional.isPresent()) {
                    // This code should never run in a normal scenario
                    updateSpawnsStandard();
                    return;
                }
                // Get the tag "Pos" of type 6 (double I think)
                ListTag posList = entityTag.getList("Pos", 6);
                int posListSize = posList.size();

                // Declare x y and z, set them based on the stuff below
                double x, y, z;

                // ALWAYS_RANDOM_POSITIONS means the positions of spawns will be completely random after the first time
                // When true: Seed based positions -> random -> random -> random...
                // When false: Seed based positions -> seed based -> seed based -> seed based...
                if (isFirstSpawn || !ALWAYS_RANDOM_POSITIONS) {
                    // Seed based positioning.
                    x = posListSize >= 1 ? posList.getDouble(0) : (double) blockPos.getX() + (spawnerRandom.nextDouble() - spawnerRandom.nextDouble()) * (double) this.spawnRange + 0.5;
                    y = posListSize >= 2 ? posList.getDouble(1) : (double) (blockPos.getY() + spawnerRandom.nextInt(3) - 1);
                    z = posListSize >= 3 ? posList.getDouble(2) : (double) blockPos.getZ() + (spawnerRandom.nextDouble() - spawnerRandom.nextDouble()) * (double) this.spawnRange + 0.5;
                } else {
                    // Random positions minus inside spawner.
                    double potX = 0, potY = 0, potZ = 0;

                    // Because positions generated by this code will not be seed based, we need to re-roll if
                    // the position is inside the spawner block, which is outside the player's control
                    while (potY < 0.5 &&
                            potX > -0.3 && potX < 1.3 &&
                            potZ > -0.3 && potZ < 1.3) {
                        potX = (worldRandom.nextDouble() - worldRandom.nextDouble()) * (double) this.spawnRange + 0.5;
                        potY = worldRandom.nextInt(3) - 1;
                        potZ = (worldRandom.nextDouble() - worldRandom.nextDouble()) * (double) this.spawnRange + 0.5;
                    }

                    x = posListSize >= 1 ? posList.getDouble(0) : (double) blockPos.getX() + potX;
                    y = posListSize >= 2 ? posList.getDouble(1) : (blockPos.getY() + potY);
                    z = posListSize >= 3 ? posList.getDouble(2) : (double) blockPos.getZ() + potZ;
                }

                // If the position collides with a block or is prevented by light levels (!SpawnRestriction.canSpawn),
                // continue to the next random or standardized position, otherwise add it to the toSpawn list
                if (!world.doesNotCollide(optional.get().createSimpleBoundingBox(x, y, z)) || !SpawnRestriction.canSpawn(optional.get(), world.getWorld(), SpawnReason.SPAWNER, new BlockPos(x, y, z), worldRandom))
                    continue;
                toSpawn.add(new Vec3d(x, y, z));
            }
            // Loop through toSpawn
            for (Vec3d vec3d : toSpawn) {
                // A lot of this is replicating mojang's stuff to spawn a blaze
                CompoundTag entityTag = this.spawnEntry.getEntityTag();
                double x, y, z;
                x = vec3d.x;
                y = vec3d.y;
                z = vec3d.z;
                Entity entity2 = EntityType.loadEntityWithPassengers(entityTag, world, entity -> {
                    entity.refreshPositionAndAngles(x, y, z, entity.yaw, entity.pitch);
                    return entity;
                });
                if (entity2 == null) {
                    // This code should never run in a normal scenario
                    updateSpawnsStandard();
                    return;
                }
                entity2.refreshPositionAndAngles(entity2.getX(), entity2.getY(), entity2.getZ(), worldRandom.nextFloat() * 360.0f, 0.0f);
                if (entity2 instanceof MobEntity) {
                    MobEntity mobEntity = (MobEntity) entity2;
                    if (!mobEntity.canSpawn(world, SpawnReason.SPAWNER) || !mobEntity.canSpawn(world)) continue;
                    if (this.spawnEntry.getEntityTag().getSize() == 1 && this.spawnEntry.getEntityTag().contains("id", 8)) {
                        ((MobEntity) entity2).initialize(world, world.getLocalDifficulty(entity2.getBlockPos()), SpawnReason.SPAWNER, null, null);
                    }
                }
                // finally spawn entity and cause a flame poof effect out of the spawner and a mob spawn poof effect
                this.spawnEntity(entity2);
                world.syncWorldEvent(2004, blockPos, 0);
                if (entity2 instanceof MobEntity) {
                    ((MobEntity) entity2).playSpawnEffects();
                }
                // If any mobs are spawned, doSpawnUpdate = true
                doSpawnUpdate = true;
            }
            if (doSpawnUpdate) {
                // This will set the delay to the next blaze spawn, which will be seed based
                updateSpawnsStandard();
            }
        }
    }

    private void updateSpawnsStandard() {
        // The same as "updateSpawns" except standard(ized)

        // Get world's spawnerRandom
        Random spawnerRandom = ((CRMOwner) getWorld().getServer()).getCRM().spawnerRandom;
        // Set spawn delay using spawnerRandom
        this.spawnDelay = this.maxSpawnDelay <= this.minSpawnDelay ? this.minSpawnDelay : this.minSpawnDelay + spawnerRandom.nextInt(this.maxSpawnDelay - this.minSpawnDelay);
        if (!this.spawnPotentials.isEmpty()) {
            this.setSpawnEntry(WeightedPicker.getRandom(spawnerRandom, this.spawnPotentials));
        }
        this.sendStatus(1);
    }
}

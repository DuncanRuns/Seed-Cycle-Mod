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
    private int maxNearbyEntities;
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
    protected abstract void updateSpawns();

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
        World world = getWorld();
        if (!world.isClient && "minecraft:blaze".equals(Objects.requireNonNull(getEntityId()).toString())) {
            info.cancel();
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
                return;
            }
            boolean doSpawnUpdate = false;
            CountedRandom spawnerRandom = ((CRMOwner) world.getServer()).getCRM().spawnerRandom;
            Random worldRandom = world.getRandom();
            boolean isFirstSpawn = spawnerRandom.getCount() <= 0;
            List<Vec3d> toSpawn = new ArrayList<>();
            for (int i = 0; i < this.spawnCount; ++i) {
                CompoundTag entityTag = this.spawnEntry.getEntityTag();
                Optional<EntityType<?>> optional = EntityType.fromTag(entityTag);
                if (!optional.isPresent()) {
                    updateSpawnsStandard();
                    return;
                }
                ListTag posList = entityTag.getList("Pos", 6);
                int posListSize = posList.size();
                double x, y, z;
                if (isFirstSpawn || !ALWAYS_RANDOM_POSITIONS) {
                    // Seed based positioning.
                    x = posListSize >= 1 ? posList.getDouble(0) : (double) blockPos.getX() + (spawnerRandom.nextDouble() - spawnerRandom.nextDouble()) * (double) this.spawnRange + 0.5;
                    y = posListSize >= 2 ? posList.getDouble(1) : (double) (blockPos.getY() + spawnerRandom.nextInt(3) - 1);
                    z = posListSize >= 3 ? posList.getDouble(2) : (double) blockPos.getZ() + (spawnerRandom.nextDouble() - spawnerRandom.nextDouble()) * (double) this.spawnRange + 0.5;
                } else {
                    // Random positions minus inside spawner.
                    double potX = 0, potY = 0, potZ = 0;
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
                if (!world.doesNotCollide(optional.get().createSimpleBoundingBox(x, y, z)) || !SpawnRestriction.canSpawn(optional.get(), world.getWorld(), SpawnReason.SPAWNER, new BlockPos(x, y, z), worldRandom))
                    continue;
                toSpawn.add(new Vec3d(x, y, z));
            }
            for (Vec3d vec3d : toSpawn) {
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
                this.spawnEntity(entity2);
                world.syncWorldEvent(2004, blockPos, 0);
                if (entity2 instanceof MobEntity) {
                    ((MobEntity) entity2).playSpawnEffects();
                }
                doSpawnUpdate = true;
            }
            if (doSpawnUpdate) {
                updateSpawnsStandard();
            }
        }
    }

    private void updateSpawnsStandard() {
        Random spawnerRandom = ((CRMOwner) getWorld().getServer()).getCRM().spawnerRandom;
        this.spawnDelay = this.maxSpawnDelay <= this.minSpawnDelay ? this.minSpawnDelay : this.minSpawnDelay + spawnerRandom.nextInt(this.maxSpawnDelay - this.minSpawnDelay);
        if (!this.spawnPotentials.isEmpty()) {
            this.setSpawnEntry(WeightedPicker.getRandom(spawnerRandom, this.spawnPotentials));
        }
        this.sendStatus(1);
    }
}

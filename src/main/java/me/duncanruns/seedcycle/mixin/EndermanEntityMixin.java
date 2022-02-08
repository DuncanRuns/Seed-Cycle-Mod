package me.duncanruns.seedcycle.mixin;

import me.duncanruns.seedcycle.CRMOwner;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.mob.Angerable;
import net.minecraft.entity.mob.EndermanEntity;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.context.LootContextTypes;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(EndermanEntity.class)
public abstract class EndermanEntityMixin extends HostileEntity implements Angerable {
    protected EndermanEntityMixin(EntityType<? extends HostileEntity> entityType, World world) {
        super(entityType, world);
    }

    @Override
    protected void dropLoot(DamageSource source, boolean causedByPlayer) {
        Identifier identifier = this.getLootTable();
        LootTable lootTable = this.world.getServer().getLootManager().getTable(identifier);
        LootContext.Builder builder = this.getLootContextBuilder(causedByPlayer, source).random(((CRMOwner) getServer()).getCRM().endermanRandom);
        lootTable.generateLoot(builder.build(LootContextTypes.ENTITY), this::dropStack);
    }

}

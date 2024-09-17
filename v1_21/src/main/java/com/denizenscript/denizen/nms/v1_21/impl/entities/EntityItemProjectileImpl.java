package com.denizenscript.denizen.nms.v1_21.impl.entities;

import com.denizenscript.denizen.nms.v1_21.ReflectionMappingsInfo;
import com.denizenscript.denizencore.utilities.ReflectionHelper;
import com.denizenscript.denizencore.utilities.debugging.Debug;
import com.google.common.base.Preconditions;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_21_R1.CraftRegistry;

import java.lang.invoke.MethodHandle;

public class EntityItemProjectileImpl extends ThrowableProjectile {

    public static MethodHandle setBukkitEntityMethod = ReflectionHelper.getFinalSetter(Entity.class, "bukkitEntity");

    public static final EntityDataAccessor<ItemStack> ITEM;

    static {
        EntityDataAccessor<ItemStack> watcher = null;
        try {
            watcher = (EntityDataAccessor<ItemStack>) ReflectionHelper.getFields(ItemEntity.class).get(ReflectionMappingsInfo.ItemEntity_DATA_ITEM).get(null);
        }
        catch (Throwable ex) {
            Debug.echoError(ex);
        }
        ITEM = watcher;
    }

    public EntityItemProjectileImpl(Level world, Location location, ItemStack item) {
        super((net.minecraft.world.entity.EntityType) net.minecraft.world.entity.EntityType.ITEM, world);
        try {
            setBukkitEntityMethod.invoke(this, new CraftItemProjectileImpl(((ServerLevel) world).getServer().server, this));
        }
        catch (Throwable ex) {
            Debug.echoError(ex);
        }
        setPosRaw(location.getX(), location.getY(), location.getZ());
        setRot(location.getYaw(), location.getPitch());
        setItemStack(item);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(ITEM, ItemStack.EMPTY);
    }

    public ItemStack getItemStack() {
        return this.getEntityData().get(ITEM);
    }

    public void setItemStack(ItemStack itemstack) {
        Preconditions.checkArgument(!itemstack.isEmpty(), "Cannot drop air");
        this.getEntityData().set(ITEM, itemstack);
        this.getEntityData().markDirty(ITEM);
    }

    @Override
    protected void onHitBlock(BlockHitResult movingobjectpositionblock) {
        super.onHitBlock(movingobjectpositionblock);
        remove(RemovalReason.KILLED);
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> datawatcherobject) {
        super.onSyncedDataUpdated(datawatcherobject);
        if (ITEM.equals(datawatcherobject)) {
            this.getItemStack().setEntityRepresentation(this);
        }
    }

    @Override
    public boolean save(net.minecraft.nbt.CompoundTag nbttagcompound) {
        if (!this.getItemStack().isEmpty()) {
            nbttagcompound.put("Item", this.getItemStack().save(CraftRegistry.getMinecraftRegistry()));
        }
        super.save(nbttagcompound);
        return true;
    }

    @Override
    public void load(net.minecraft.nbt.CompoundTag nbttagcompound) {
        net.minecraft.nbt.CompoundTag nbttagcompound1 = nbttagcompound.getCompound("Item");
        this.setItemStack(ItemStack.parseOptional(CraftRegistry.getMinecraftRegistry(), nbttagcompound1));
        if (this.getItemStack().isEmpty()) {
            this.remove(RemovalReason.KILLED);
        }
        super.load(nbttagcompound);
    }

    @Override
    public CraftItemProjectileImpl getBukkitEntity() {
        return (CraftItemProjectileImpl) super.getBukkitEntity();
    }
}

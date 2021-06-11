package com.denizenscript.denizen.nms.v1_17.impl.entities;

import com.denizenscript.denizencore.utilities.ReflectionHelper;
import com.denizenscript.denizen.utilities.debugging.Debug;
import com.google.common.base.Preconditions;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.bukkit.Location;

import java.lang.invoke.MethodHandle;

public class EntityItemProjectileImpl extends ThrowableProjectile {

    public static MethodHandle setBukkitEntityMethod = ReflectionHelper.getFinalSetter(Entity.class, "bukkitEntity");

    public static final EntityDataAccessor<ItemStack> ITEM;

    static {
        EntityDataAccessor<ItemStack> watcher = null;
        try {
            watcher = (EntityDataAccessor<ItemStack>) ReflectionHelper.getFields(EntityItem.class).get("ITEM").get(null);
        }
        catch (Throwable ex) {
            Debug.echoError(ex);
        }
        ITEM = watcher;
    }

    public EntityItemProjectileImpl(Level world, Location location, ItemStack item) {
        super((net.minecraft.world.entity.EntityType) net.minecraft.world.entity.EntityType.ITEM, world);
        try {
            setBukkitEntityMethod.invoke(this, new CraftItemProjectileImpl(world.getServer(), this));
        }
        catch (Throwable ex) {
            Debug.echoError(ex);
        }
        setPositionRotation(location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch());
        setItemStack(item);
    }

    @Override
    protected void initDatawatcher() {
        this.getEntityData().register(ITEM, ItemStack.b);
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
    protected void a(MovingObjectPositionBlock movingobjectpositionblock) {
        super.a(movingobjectpositionblock);
        die();
    }

    @Override
    public void a(EntityDataAccessor<?> datawatcherobject) {
        super.a(datawatcherobject);
        if (ITEM.equals(datawatcherobject)) {
            this.getItemStack().a(this);
        }
    }

    @Override
    public void saveData(net.minecraft.nbt.CompoundTag nbttagcompound) {
        if (!this.getItemStack().isEmpty()) {
            nbttagcompound.set("Item", this.getItemStack().save(new net.minecraft.nbt.CompoundTag()));
        }
        super.saveData(nbttagcompound);
    }

    @Override
    public void loadData(net.minecraft.nbt.CompoundTag nbttagcompound) {
        net.minecraft.nbt.CompoundTag nbttagcompound1 = nbttagcompound.getCompound("Item");
        this.setItemStack(ItemStack.a(nbttagcompound1));
        if (this.getItemStack().isEmpty()) {
            this.die();
        }
        super.loadData(nbttagcompound);
    }

    @Override
    public CraftItemProjectileImpl getBukkitEntity() {
        return (CraftItemProjectileImpl) super.getBukkitEntity();
    }
}

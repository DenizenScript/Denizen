package com.denizenscript.denizen.nms.v1_17.impl.entities;

import com.denizenscript.denizencore.utilities.ReflectionHelper;
import com.denizenscript.denizen.utilities.debugging.Debug;
import com.google.common.base.Preconditions;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.syncher.DataWatcherObject;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.item.EntityItem;
import net.minecraft.world.entity.projectile.EntityProjectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.World;
import net.minecraft.world.phys.MovingObjectPositionBlock;
import org.bukkit.Location;

import java.lang.invoke.MethodHandle;

public class EntityItemProjectileImpl extends EntityProjectile {

    public static MethodHandle setBukkitEntityMethod = ReflectionHelper.getFinalSetter(Entity.class, "bukkitEntity");

    public static final DataWatcherObject<ItemStack> ITEM;

    static {
        DataWatcherObject<ItemStack> watcher = null;
        try {
            watcher = (DataWatcherObject<ItemStack>) ReflectionHelper.getFields(EntityItem.class).get("ITEM").get(null);
        }
        catch (Throwable ex) {
            Debug.echoError(ex);
        }
        ITEM = watcher;
    }

    public EntityItemProjectileImpl(World world, Location location, ItemStack item) {
        super((EntityTypes) EntityTypes.ITEM, world);
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
        this.getDataWatcher().register(ITEM, ItemStack.b);
    }

    public ItemStack getItemStack() {
        return this.getDataWatcher().get(ITEM);
    }

    public void setItemStack(ItemStack itemstack) {
        Preconditions.checkArgument(!itemstack.isEmpty(), "Cannot drop air");
        this.getDataWatcher().set(ITEM, itemstack);
        this.getDataWatcher().markDirty(ITEM);
    }

    @Override
    protected void a(MovingObjectPositionBlock movingobjectpositionblock) {
        super.a(movingobjectpositionblock);
        die();
    }

    @Override
    public void a(DataWatcherObject<?> datawatcherobject) {
        super.a(datawatcherobject);
        if (ITEM.equals(datawatcherobject)) {
            this.getItemStack().a(this);
        }
    }

    @Override
    public void saveData(NBTTagCompound nbttagcompound) {
        if (!this.getItemStack().isEmpty()) {
            nbttagcompound.set("Item", this.getItemStack().save(new NBTTagCompound()));
        }
        super.saveData(nbttagcompound);
    }

    @Override
    public void loadData(NBTTagCompound nbttagcompound) {
        NBTTagCompound nbttagcompound1 = nbttagcompound.getCompound("Item");
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

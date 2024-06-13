package com.denizenscript.denizen.nms.v1_20.impl.entities;

import com.denizenscript.denizen.nms.interfaces.ItemProjectile;
import com.denizenscript.denizencore.utilities.ReflectionHelper;
import com.denizenscript.denizencore.utilities.debugging.Debug;
import org.bukkit.craftbukkit.v1_20_R4.CraftServer;
import org.bukkit.craftbukkit.v1_20_R4.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_20_R4.inventory.CraftItemStack;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.projectiles.ProjectileSource;

import java.lang.invoke.MethodHandle;
import java.util.UUID;

public class CraftItemProjectileImpl extends CraftEntity implements ItemProjectile {

    private boolean doesBounce;

    public CraftItemProjectileImpl(CraftServer server, EntityItemProjectileImpl entity) {
        super(server, entity);
        MethodHandle handle = ReflectionHelper.getFinalSetterForFirstOfType(CraftEntity.class, EntityType.class);
        if (handle != null) {
            try {
                handle.invoke(this, EntityType.ITEM);
            }
            catch (Throwable ex) {
                Debug.echoError(ex);
            }
        }
    }

    @Override
    public EntityItemProjectileImpl getHandle() {
        return (EntityItemProjectileImpl) super.getHandle();
    }

    @Override
    public String getEntityTypeName() {
        return getType().name();
    }

    @Override
    public ItemStack getItemStack() {
        return CraftItemStack.asBukkitCopy(getHandle().getItemStack());
    }

    @Override
    public void setItemStack(ItemStack itemStack) {
        getHandle().setItemStack(CraftItemStack.asNMSCopy(itemStack));
    }

    @Override
    public int getPickupDelay() {
        return 0;
    }

    @Override
    public void setPickupDelay(int i) {
        // Do nothing
    }

    @Override
    public void setUnlimitedLifetime(boolean b) {
        // Do nothing
    }

    @Override
    public boolean isUnlimitedLifetime() {
        return false;
    }

    @Override
    public void setOwner(UUID uuid) {
        // Do nothing
    }

    @Override
    public UUID getOwner() {
        return null;
    }

    @Override
    public void setThrower(UUID uuid) {
        // Do nothing
    }

    @Override
    public UUID getThrower() {
        return null;
    }

    @Override
    public ProjectileSource getShooter() {
        return getHandle().projectileSource;
    }

    @Override
    public void setShooter(ProjectileSource projectileSource) {
        if (projectileSource instanceof CraftEntity) {
            getHandle().setOwner(((CraftEntity) projectileSource).getHandle());
        }
        else {
            getHandle().projectileSource = projectileSource;
        }
    }

    @Override
    public boolean doesBounce() {
        return doesBounce;
    }

    @Override
    public void setBounce(boolean doesBounce) {
        this.doesBounce = doesBounce;
    }
}

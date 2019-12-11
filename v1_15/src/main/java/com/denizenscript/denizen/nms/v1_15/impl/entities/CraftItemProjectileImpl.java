package com.denizenscript.denizen.nms.v1_15.impl.entities;

import com.denizenscript.denizen.nms.interfaces.ItemProjectile;
import org.bukkit.craftbukkit.v1_15_R1.CraftServer;
import org.bukkit.craftbukkit.v1_15_R1.entity.CraftHumanEntity;
import org.bukkit.craftbukkit.v1_15_R1.entity.CraftItem;
import org.bukkit.craftbukkit.v1_15_R1.entity.CraftLivingEntity;
import org.bukkit.projectiles.ProjectileSource;

public class CraftItemProjectileImpl extends CraftItem implements ItemProjectile {

    private boolean doesBounce;

    public CraftItemProjectileImpl(CraftServer server, EntityItemProjectileImpl entity) {
        super(server, entity);
    }

    /*
    public static ItemProjectile createItemProjectile(Location location, ArrayList<Mechanism> mechanisms) {
    }*/

    @Override
    public EntityItemProjectileImpl getHandle() {
        return (EntityItemProjectileImpl) super.getHandle();
    }

    @Override
    public String getEntityTypeName() {
        return "ITEM_PROJECTILE";
    }

    @Override
    public ProjectileSource getShooter() {
        return getHandle().projectileSource;
    }

    @Override
    public void setShooter(ProjectileSource shooter) {
        if (shooter instanceof CraftLivingEntity) {
            getHandle().shooter = ((CraftLivingEntity) shooter).getHandle();
            if (shooter instanceof CraftHumanEntity) {
                getHandle().shooterId = ((CraftHumanEntity) shooter).getUniqueId();
            }
        }
        else {
            getHandle().shooter = null;
            getHandle().shooterId = null;
        }
        getHandle().projectileSource = shooter;
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

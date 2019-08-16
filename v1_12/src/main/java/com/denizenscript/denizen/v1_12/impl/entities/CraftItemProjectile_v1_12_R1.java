package com.denizenscript.denizen.v1_12.impl.entities;

import com.denizenscript.denizen.nms.interfaces.ItemProjectile;
import org.bukkit.craftbukkit.v1_12_R1.CraftServer;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftHumanEntity;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftItem;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftLivingEntity;
import org.bukkit.projectiles.ProjectileSource;

public class CraftItemProjectile_v1_12_R1 extends CraftItem implements ItemProjectile {

    private boolean doesBounce;

    public CraftItemProjectile_v1_12_R1(CraftServer server, EntityItemProjectile_v1_12_R1 entity) {
        super(server, entity);
    }

    /*
    public static ItemProjectile createItemProjectile(Location location, ArrayList<Mechanism> mechanisms) {
    }*/

    @Override
    public EntityItemProjectile_v1_12_R1 getHandle() {
        return (EntityItemProjectile_v1_12_R1) super.getHandle();
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
                getHandle().shooterName = ((CraftHumanEntity) shooter).getName();
            }
        }
        else {
            getHandle().shooter = null;
            getHandle().shooterName = null;
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

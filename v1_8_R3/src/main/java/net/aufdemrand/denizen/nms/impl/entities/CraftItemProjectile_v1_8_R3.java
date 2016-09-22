package net.aufdemrand.denizen.nms.impl.entities;

import net.aufdemrand.denizen.nms.interfaces.ItemProjectile;
import org.bukkit.craftbukkit.v1_8_R3.CraftServer;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftHumanEntity;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftItem;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftLivingEntity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.projectiles.ProjectileSource;

public class CraftItemProjectile_v1_8_R3 extends CraftItem implements ItemProjectile {

    private boolean doesBounce;

    public CraftItemProjectile_v1_8_R3(CraftServer server, EntityItemProjectile_v1_8_R3 entity) {
        super(server, entity);
    }

    /*
    public static ItemProjectile createItemProjectile(Location location, ArrayList<Mechanism> mechanisms) {
    }*/

    @Override
    public EntityItemProjectile_v1_8_R3 getHandle() {
        return (EntityItemProjectile_v1_8_R3) super.getHandle();
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


    @Override
    public void _INVALID_setShooter(LivingEntity livingEntity) {
        // Do nothing
    }

    @Override
    public LivingEntity _INVALID_getShooter() {
        return null;
    }
}

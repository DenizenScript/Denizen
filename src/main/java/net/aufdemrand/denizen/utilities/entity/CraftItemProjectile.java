package net.aufdemrand.denizen.utilities.entity;

import net.aufdemrand.denizen.objects.dItem;
import net.aufdemrand.denizencore.objects.Mechanism;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_8_R3.CraftServer;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftHumanEntity;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftItem;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftLivingEntity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.projectiles.ProjectileSource;

import java.util.ArrayList;

public class CraftItemProjectile extends CraftItem implements DenizenCustomEntity, ItemProjectile {

    private boolean doesBounce;

    public CraftItemProjectile(CraftServer server, EntityItemProjectile entity) {
        super(server, entity);
    }

    @CreateEntity
    public static ItemProjectile createItemProjectile(Location location, ArrayList<Mechanism> mechanisms) {
        CraftWorld world = (CraftWorld) location.getWorld();
        ItemStack itemStack = new ItemStack(Material.STONE);
        for (Mechanism mechanism : mechanisms) {
            if (mechanism.matches("item") && mechanism.requireObject(dItem.class)) {
                itemStack = mechanism.getValue().asType(dItem.class).getItemStack();
                break;
            }
        }
        EntityItemProjectile entity = new EntityItemProjectile(world, location, itemStack);
        return (ItemProjectile) entity.getBukkitEntity();
    }

    @Override
    public EntityItemProjectile getHandle() {
        return (EntityItemProjectile) super.getHandle();
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

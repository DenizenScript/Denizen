package net.aufdemrand.denizen.utilities.entity;

import net.minecraft.server.v1_7_R4.EntityArrow;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_7_R4.CraftServer;
import org.bukkit.craftbukkit.v1_7_R4.CraftWorld;
import org.bukkit.craftbukkit.v1_7_R4.entity.CraftArrow;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Vehicle;

public class CraftFakeArrow extends CraftArrow implements Vehicle {
    public CraftFakeArrow(CraftServer craftServer, EntityArrow entityArrow) {
        super(craftServer, entityArrow);
    }

    public void setShooter (LivingEntity livingEntity) {
    }

    @Override
    public void remove() {
        if (getPassenger() != null) {
            return;
        }
        super.remove();
    }

    public static Arrow createArrow(Location location) {
        CraftWorld world = (CraftWorld) location.getWorld();
        EntityArrow arrow = new FakeArrowEntity(world, location);
        return (Arrow) arrow.getBukkitEntity();
    }
}

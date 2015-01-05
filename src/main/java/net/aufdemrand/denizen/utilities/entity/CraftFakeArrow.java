package net.aufdemrand.denizen.utilities.entity;

import net.aufdemrand.denizen.utilities.debugging.dB;
import net.minecraft.server.v1_8_R1.EntityArrow;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_8_R1.CraftServer;
import org.bukkit.craftbukkit.v1_8_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_8_R1.entity.CraftArrow;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Vehicle;

public class CraftFakeArrow extends CraftArrow implements Vehicle {
    public CraftFakeArrow(CraftServer craftServer, EntityArrow entityArrow) {
        super(craftServer, entityArrow);
    }

    @Override
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

    @Override
    public String getName() {
        return "FakeArrow";
    }

    @Override
    public void sendMessage(String message) {
        dB.log("Message sent to FakeArrow: " + message);
    }

    @Override
    public void sendMessage(String[] messages) {
        dB.log("Messages sent to FakeArrow: " + messages);
    }
}

package net.aufdemrand.denizen.nms.interfaces;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Animals;
import org.bukkit.entity.Creature;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import java.util.UUID;

public interface EntityHelper {

    void forceInteraction(Player player, Location location);

    Entity getEntity(World world, UUID uuid);

    boolean isBreeding(Animals entity);

    void setBreeding(Animals entity, boolean breeding);

    void setTarget(Creature entity, LivingEntity target);
}

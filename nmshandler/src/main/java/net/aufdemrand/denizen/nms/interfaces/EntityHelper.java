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

    void stopFollowing(Entity follower);

    void stopWalking(Entity entity);

    void toggleAI(Entity entity, boolean hasAI);

    boolean isAIDisabled(Entity entity);

    double getSpeed(Entity entity);

    void setSpeed(Entity entity, double speed);

    void follow(final Entity target, final Entity follower, final double speed, final double lead,
                final double maxRange, final boolean allowWander);

    void walkTo(final Entity entity, Location location, double speed, final Runnable callback);

    void hideEntity(Player player, Entity entity, boolean keepInTabList);

    void unhideEntity(Player player, Entity entity);

    boolean isHidden(Player player, UUID entity);
}

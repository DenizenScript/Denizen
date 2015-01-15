package net.aufdemrand.denizen.utilities.entity;

import net.aufdemrand.denizen.utilities.DenizenAPI;
import net.aufdemrand.denizen.utilities.Utilities;
import net.minecraft.server.v1_8_R1.EntityInsentient;
import net.minecraft.server.v1_8_R1.GenericAttributes;
import net.minecraft.server.v1_8_R1.NavigationAbstract;
import net.minecraft.server.v1_8_R1.PathEntity;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_8_R1.entity.CraftEntity;
import org.bukkit.entity.Entity;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class EntityMovement {

    private final static Map<UUID, BukkitTask> followTasks = new HashMap<UUID, BukkitTask>();

    public static void stopFollowing(Entity follower) {
        if (follower == null)
            return;
        UUID uuid = follower.getUniqueId();
        if (followTasks.containsKey(uuid))
            followTasks.get(uuid).cancel();
    }

    public static void follow(final Entity target, final Entity follower, final double speed, final double lead,
                              final double maxRange, final boolean allowWander) {
        if (target == null || follower == null)
            return;

        net.minecraft.server.v1_8_R1.Entity nmsEntityFollower = ((CraftEntity) follower).getHandle();
        if (!(nmsEntityFollower instanceof EntityInsentient))
            return;
        final EntityInsentient nmsFollower = (EntityInsentient) nmsEntityFollower;
        final NavigationAbstract followerNavigation = nmsFollower.getNavigation();

        UUID uuid = follower.getUniqueId();

        if (followTasks.containsKey(uuid))
            followTasks.get(uuid).cancel();

        final int locationNearInt = (int) Math.floor(lead);
        final boolean hasMax = maxRange > lead;

        followTasks.put(follower.getUniqueId(), new BukkitRunnable() {

            private boolean inRadius = false;

            public void run(){
                if (!target.isValid() || !follower.isValid()) {
                    this.cancel();
                }
                followerNavigation.a(2F);
                Location targetLocation = target.getLocation();
                PathEntity path;

                if (hasMax && !Utilities.checkLocation(targetLocation, follower.getLocation(), maxRange)
                        && !target.isDead() && target.isOnGround()) {
                    if (!inRadius) {
                        follower.teleport(Utilities.getWalkableLocationNear(targetLocation, locationNearInt));
                    }
                    else {
                        inRadius = false;
                        path = followerNavigation.a(targetLocation.getX(), targetLocation.getY(), targetLocation.getZ());
                        if (path != null) {
                            followerNavigation.a(path, 1D);
                            followerNavigation.a(2D);
                        }
                    }
                }
                else if (!inRadius && !Utilities.checkLocation(targetLocation, follower.getLocation(), lead)) {
                    path = followerNavigation.a(targetLocation.getX(), targetLocation.getY(), targetLocation.getZ());
                    if (path != null) {
                        followerNavigation.a(path, 1D);
                        followerNavigation.a(2D);
                    }
                }
                else {
                    inRadius = true;
                }
                if (inRadius && !allowWander) {
                    followerNavigation.n();
                }
                nmsFollower.getAttributeInstance(GenericAttributes.d).setValue(speed);
            }
        }.runTaskTimer(DenizenAPI.getCurrentInstance(), 0, 20));
    }

    public static void walkTo(Entity entity, Location location, double speed) {
        if (entity == null || location == null)
            return;

        net.minecraft.server.v1_8_R1.Entity nmsEntityEntity = ((CraftEntity) entity).getHandle();
        if (!(nmsEntityEntity instanceof EntityInsentient))
            return;
        final EntityInsentient nmsEntity = (EntityInsentient) nmsEntityEntity;
        final NavigationAbstract followerNavigation = nmsEntity.getNavigation();

        PathEntity path;
        path = followerNavigation.a(location.getX(), location.getY(), location.getZ());
        if (path != null) {
            followerNavigation.a(path, 1D);
            followerNavigation.a(2D);
        }
        if (!Utilities.checkLocation(location, entity.getLocation(), 20)) {
            entity.teleport(location);
        }
        nmsEntity.getAttributeInstance(GenericAttributes.d).setValue(speed);
    }
}

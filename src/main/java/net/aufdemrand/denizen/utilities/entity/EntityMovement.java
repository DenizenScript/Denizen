package net.aufdemrand.denizen.utilities.entity;

import net.aufdemrand.denizen.utilities.DenizenAPI;
import net.aufdemrand.denizen.utilities.Utilities;
import net.minecraft.server.v1_9_R2.EntityInsentient;
import net.minecraft.server.v1_9_R2.GenericAttributes;
import net.minecraft.server.v1_9_R2.NavigationAbstract;
import net.minecraft.server.v1_9_R2.PathEntity;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_9_R2.entity.CraftEntity;
import org.bukkit.entity.Entity;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class EntityMovement {

    private final static Map<UUID, BukkitTask> followTasks = new HashMap<UUID, BukkitTask>();

    public static void stopFollowing(Entity follower) {
        if (follower == null) {
            return;
        }
        UUID uuid = follower.getUniqueId();
        if (followTasks.containsKey(uuid)) {
            followTasks.get(uuid).cancel();
        }
    }

    public static void stopWalking(Entity entity) {
        net.minecraft.server.v1_9_R2.Entity nmsEntity = ((CraftEntity) entity).getHandle();
        if (!(nmsEntity instanceof EntityInsentient)) {
            return;
        }
        ((EntityInsentient) nmsEntity).getNavigation().n();
    }

    public static void toggleAI(Entity entity, boolean hasAI) {
        net.minecraft.server.v1_9_R2.Entity nmsEntity = ((CraftEntity) entity).getHandle();
        if (!(nmsEntity instanceof EntityInsentient)) {
            return;
        }
        // TODO: 1.9.4: ((EntityInsentient) nmsEntity).m(!hasAI);
    }

    public static boolean isAIDisabled(Entity entity) {
        net.minecraft.server.v1_9_R2.Entity nmsEntity = ((CraftEntity) entity).getHandle();
        if (!(nmsEntity instanceof EntityInsentient)) {
            return true;
        }
        // TODO: 1.9.4: return ((EntityInsentient) nmsEntity).cR();
        return false;
    }

    public static double getSpeed(Entity entity) {
        net.minecraft.server.v1_9_R2.Entity nmsEntityEntity = ((CraftEntity) entity).getHandle();
        if (!(nmsEntityEntity instanceof EntityInsentient)) {
            return 0.0;
        }
        EntityInsentient nmsEntity = (EntityInsentient) nmsEntityEntity;
        return nmsEntity.getAttributeInstance(GenericAttributes.MOVEMENT_SPEED).b();
    }

    public static void setSpeed(Entity entity, double speed) {
        net.minecraft.server.v1_9_R2.Entity nmsEntityEntity = ((CraftEntity) entity).getHandle();
        if (!(nmsEntityEntity instanceof EntityInsentient)) {
            return;
        }
        EntityInsentient nmsEntity = (EntityInsentient) nmsEntityEntity;
        nmsEntity.getAttributeInstance(GenericAttributes.MOVEMENT_SPEED).setValue(speed);
    }

    public static void follow(final Entity target, final Entity follower, final double speed, final double lead,
                              final double maxRange, final boolean allowWander) {
        if (target == null || follower == null) {
            return;
        }

        final net.minecraft.server.v1_9_R2.Entity nmsEntityFollower = ((CraftEntity) follower).getHandle();
        if (!(nmsEntityFollower instanceof EntityInsentient)) {
            return;
        }
        final EntityInsentient nmsFollower = (EntityInsentient) nmsEntityFollower;
        final NavigationAbstract followerNavigation = nmsFollower.getNavigation();

        UUID uuid = follower.getUniqueId();

        if (followTasks.containsKey(uuid)) {
            followTasks.get(uuid).cancel();
        }

        final int locationNearInt = (int) Math.floor(lead);
        final boolean hasMax = maxRange > lead;

        followTasks.put(follower.getUniqueId(), new BukkitRunnable() {

            private boolean inRadius = false;

            public void run() {
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
                nmsFollower.getAttributeInstance(GenericAttributes.MOVEMENT_SPEED).setValue(speed);
            }
        }.runTaskTimer(DenizenAPI.getCurrentInstance(), 0, 20));
    }

    public static void walkTo(final Entity entity, Location location, double speed, final Runnable callback) {
        if (entity == null || location == null) {
            return;
        }

        net.minecraft.server.v1_9_R2.Entity nmsEntityEntity = ((CraftEntity) entity).getHandle();
        if (!(nmsEntityEntity instanceof EntityInsentient)) {
            return;
        }
        final EntityInsentient nmsEntity = (EntityInsentient) nmsEntityEntity;
        final NavigationAbstract entityNavigation = nmsEntity.getNavigation();

        final PathEntity path;
        final boolean aiDisabled = isAIDisabled(entity);
        if (aiDisabled) {
            toggleAI(entity, true);
            nmsEntity.onGround = true;
        }
        path = entityNavigation.a(location.getX(), location.getY(), location.getZ());
        if (path != null) {
            entityNavigation.a(path, 1D);
            entityNavigation.a(2D);
            final double oldSpeed = nmsEntity.getAttributeInstance(GenericAttributes.MOVEMENT_SPEED).b();
            nmsEntity.getAttributeInstance(GenericAttributes.MOVEMENT_SPEED).setValue(speed);
            new BukkitRunnable() {
                @Override
                public void run() {
                    if (entityNavigation.n() || path.b()) {
                        if (callback != null) {
                            callback.run();
                        }
                        nmsEntity.getAttributeInstance(GenericAttributes.MOVEMENT_SPEED).setValue(oldSpeed);
                        if (aiDisabled) {
                            toggleAI(entity, false);
                        }
                        cancel();
                    }
                }
            }.runTaskTimer(DenizenAPI.getCurrentInstance(), 1, 1);
        }
        //if (!Utilities.checkLocation(location, entity.getLocation(), 20)) {
        // TODO: generate waypoints to the target location?
        else {
            entity.teleport(location);
        }
    }
}

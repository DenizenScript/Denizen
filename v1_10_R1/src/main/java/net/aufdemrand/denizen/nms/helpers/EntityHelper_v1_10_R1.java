package net.aufdemrand.denizen.nms.helpers;

import net.aufdemrand.denizen.nms.NMSHandler;
import net.aufdemrand.denizen.nms.interfaces.EntityHelper;
import net.aufdemrand.denizen.nms.util.Utilities;
import net.minecraft.server.v1_10_R1.*;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_10_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_10_R1.entity.CraftAnimals;
import org.bukkit.craftbukkit.v1_10_R1.entity.CraftCreature;
import org.bukkit.craftbukkit.v1_10_R1.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_10_R1.entity.CraftLivingEntity;
import org.bukkit.craftbukkit.v1_10_R1.entity.CraftPlayer;
import org.bukkit.entity.Animals;
import org.bukkit.entity.Creature;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class EntityHelper_v1_10_R1 implements EntityHelper {

    /*
        General Entity Methods
     */

    @Override
    public void forceInteraction(Player player, Location location) {
        CraftPlayer craftPlayer = (CraftPlayer) player;
        BlockPosition pos = new BlockPosition(location.getBlockX(), location.getBlockY(), location.getBlockZ());
        Block.getById(location.getBlock().getType().getId())
                .interact(((CraftWorld) location.getWorld()).getHandle(), pos,
                        ((CraftWorld) location.getWorld()).getHandle().getType(pos),
                        craftPlayer != null ? craftPlayer.getHandle() : null, EnumHand.MAIN_HAND, null,
                        EnumDirection.NORTH, 0f, 0f, 0f);
    }

    @Override
    public Entity getEntity(World world, UUID uuid) {
        net.minecraft.server.v1_10_R1.Entity entity = ((CraftWorld) world).getHandle().getEntity(uuid);
        return entity == null ? null : entity.getBukkitEntity();
    }

    @Override
    public boolean isBreeding(Animals entity) {
        return ((CraftAnimals) entity).getHandle().isInLove();
    }

    @Override
    public void setBreeding(Animals entity, boolean breeding) {
        if (breeding) {
            ((CraftAnimals) entity).getHandle().c((EntityHuman) null);
        }
        else {
            ((CraftAnimals) entity).getHandle().resetLove();
        }
    }

    @Override
    public void setTarget(Creature entity, LivingEntity target) {
        EntityLiving nmsTarget = target != null ? ((CraftLivingEntity) target).getHandle() : null;
        ((CraftCreature) entity).getHandle().setGoalTarget(nmsTarget, EntityTargetEvent.TargetReason.CUSTOM, true);
        entity.setTarget(target);
    }

    /*
        Entity Movement
     */

    private final static Map<UUID, BukkitTask> followTasks = new HashMap<UUID, BukkitTask>();

    @Override
    public void stopFollowing(Entity follower) {
        if (follower == null) {
            return;
        }
        UUID uuid = follower.getUniqueId();
        if (followTasks.containsKey(uuid)) {
            followTasks.get(uuid).cancel();
        }
    }

    @Override
    public void stopWalking(Entity entity) {
        net.minecraft.server.v1_10_R1.Entity nmsEntity = ((CraftEntity) entity).getHandle();
        if (!(nmsEntity instanceof EntityInsentient)) {
            return;
        }
        ((EntityInsentient) nmsEntity).getNavigation().o();
    }

    @Override
    public void toggleAI(Entity entity, boolean hasAI) {
        net.minecraft.server.v1_10_R1.Entity nmsEntity = ((CraftEntity) entity).getHandle();
        if (!(nmsEntity instanceof EntityInsentient)) {
            return;
        }
        ((EntityInsentient) nmsEntity).setAI(!hasAI);
    }

    @Override
    public boolean isAIDisabled(Entity entity) {
        net.minecraft.server.v1_10_R1.Entity nmsEntity = ((CraftEntity) entity).getHandle();
        if (!(nmsEntity instanceof EntityInsentient)) {
            return true;
        }
        return ((EntityInsentient) nmsEntity).hasAI();
    }

    @Override
    public double getSpeed(Entity entity) {
        net.minecraft.server.v1_10_R1.Entity nmsEntityEntity = ((CraftEntity) entity).getHandle();
        if (!(nmsEntityEntity instanceof EntityInsentient)) {
            return 0.0;
        }
        EntityInsentient nmsEntity = (EntityInsentient) nmsEntityEntity;
        return nmsEntity.getAttributeInstance(GenericAttributes.MOVEMENT_SPEED).b();
    }

    @Override
    public void setSpeed(Entity entity, double speed) {
        net.minecraft.server.v1_10_R1.Entity nmsEntityEntity = ((CraftEntity) entity).getHandle();
        if (!(nmsEntityEntity instanceof EntityInsentient)) {
            return;
        }
        EntityInsentient nmsEntity = (EntityInsentient) nmsEntityEntity;
        nmsEntity.getAttributeInstance(GenericAttributes.MOVEMENT_SPEED).setValue(speed);
    }

    @Override
    public void follow(final Entity target, final Entity follower, final double speed, final double lead,
                              final double maxRange, final boolean allowWander) {
        if (target == null || follower == null) {
            return;
        }

        final net.minecraft.server.v1_10_R1.Entity nmsEntityFollower = ((CraftEntity) follower).getHandle();
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
                    followerNavigation.o();
                }
                nmsFollower.getAttributeInstance(GenericAttributes.MOVEMENT_SPEED).setValue(speed);
            }
        }.runTaskTimer(NMSHandler.getJavaPlugin(), 0, 10));
    }

    @Override
    public void walkTo(final Entity entity, Location location, double speed, final Runnable callback) {
        if (entity == null || location == null) {
            return;
        }

        net.minecraft.server.v1_10_R1.Entity nmsEntityEntity = ((CraftEntity) entity).getHandle();
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
            }.runTaskTimer(NMSHandler.getJavaPlugin(), 1, 1);
        }
        //if (!Utilities.checkLocation(location, entity.getLocation(), 20)) {
        // TODO: generate waypoints to the target location?
        else {
            entity.teleport(location);
        }
    }

    /*
        Hide Entity
     */

    public static Map<UUID, Set<UUID>> hiddenEntities = new HashMap<UUID, Set<UUID>>();

    @Override
    public void hideEntity(Player player, Entity entity, boolean keepInTabList) {
        CraftPlayer craftPlayer = (CraftPlayer)player;
        EntityPlayer entityPlayer = craftPlayer.getHandle();
        UUID playerUUID = player.getUniqueId();
        if (entityPlayer.playerConnection != null && !craftPlayer.equals(entity)) {
            if (!hiddenEntities.containsKey(playerUUID)) {
                hiddenEntities.put(playerUUID, new HashSet<UUID>());
            }
            Set hidden = hiddenEntities.get(playerUUID);
            UUID entityUUID = entity.getUniqueId();
            if (!hidden.contains(entityUUID)) {
                hidden.add(entityUUID);
                EntityTracker tracker = ((WorldServer)craftPlayer.getHandle().world).tracker;
                net.minecraft.server.v1_10_R1.Entity other = ((CraftEntity)entity).getHandle();
                EntityTrackerEntry entry = tracker.trackedEntities.get(other.getId());
                if (entry != null) {
                    entry.clear(entityPlayer);
                }
                if (entity instanceof Player && !entity.hasMetadata("NPC") && !keepInTabList) {
                    entityPlayer.playerConnection.sendPacket(new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.REMOVE_PLAYER, (EntityPlayer) other));
                }
            }
        }
    }

    @Override
    public void unhideEntity(Player player, Entity entity) {
        CraftPlayer craftPlayer = (CraftPlayer)player;
        EntityPlayer entityPlayer = craftPlayer.getHandle();
        UUID playerUUID = player.getUniqueId();
        if (entityPlayer.playerConnection != null && !craftPlayer.equals(entity) && hiddenEntities.containsKey(playerUUID)) {
            Set hidden = hiddenEntities.get(playerUUID);
            UUID entityUUID = entity.getUniqueId();
            if (hidden.contains(entityUUID)) {
                hidden.remove(entityUUID);
                EntityTracker tracker = ((WorldServer)craftPlayer.getHandle().world).tracker;
                net.minecraft.server.v1_10_R1.Entity other = ((CraftEntity)entity).getHandle();
                if (entity instanceof Player && !entity.hasMetadata("NPC")) {
                    entityPlayer.playerConnection.sendPacket(new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.ADD_PLAYER, (EntityPlayer) other));
                }
                EntityTrackerEntry entry = tracker.trackedEntities.get(other.getId());
                if(entry != null && !entry.trackedPlayers.contains(entityPlayer)) {
                    entry.updatePlayer(entityPlayer);
                }
            }
        }
    }

    @Override
    public boolean isHidden(Player player, UUID entity) {
        UUID uuid = player.getUniqueId();
        return hiddenEntities.containsKey(uuid) && hiddenEntities.get(uuid).contains(entity);
    }
}

package net.aufdemrand.denizen.nms.interfaces;

import net.aufdemrand.denizen.nms.NMSHandler;
import net.aufdemrand.denizen.nms.util.BoundingBox;
import net.aufdemrand.denizen.nms.util.jnbt.CompoundTag;
import net.aufdemrand.denizencore.utilities.debugging.dB;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.*;

public abstract class EntityHelper {

    public void setRiptide(Entity entity, boolean state) {
        dB.echoError("Riptide control not available on this server version.");
    }

    public void setCarriedItem(Enderman entity, ItemStack item) {
        entity.setCarriedMaterial(item.getData());
    }

    public abstract int getBodyArrows(Entity entity);

    public abstract void setBodyArrows(Entity entity, int numArrows);

    public abstract double getArrowDamage(Entity arrow);

    public abstract void setArrowDamage(Entity arrow, double damage);

    public abstract String getArrowPickupStatus(Entity entity);

    public abstract void setArrowPickupStatus(Entity entity, String status);

    public abstract Entity getFishHook(PlayerFishEvent event);

    public abstract void forceInteraction(Player player, Location location);

    public abstract Entity getEntity(World world, UUID uuid);

    public abstract boolean isBreeding(Animals entity);

    public abstract void setBreeding(Animals entity, boolean breeding);

    public abstract void setTarget(Creature entity, LivingEntity target);

    public abstract CompoundTag getNbtData(Entity entity);

    public abstract void setNbtData(Entity entity, CompoundTag compoundTag);

    public abstract void setSilent(Entity entity, boolean silent);

    public abstract boolean isSilent(Entity entity);

    public abstract ItemStack getItemInHand(LivingEntity entity);

    public abstract void setItemInHand(LivingEntity entity, ItemStack itemStack);

    public abstract ItemStack getItemInOffHand(LivingEntity entity);

    public abstract void setItemInOffHand(LivingEntity entity, ItemStack itemStack);

    public abstract void stopFollowing(Entity follower);

    public abstract void stopWalking(Entity entity);

    public abstract void toggleAI(Entity entity, boolean hasAI);

    public abstract boolean isAIDisabled(Entity entity);

    public abstract double getSpeed(Entity entity);

    public abstract void setSpeed(Entity entity, double speed);

    public abstract void follow(final Entity target, final Entity follower, final double speed, final double lead,
                                final double maxRange, final boolean allowWander);

    public abstract void walkTo(final Entity entity, Location location, double speed, final Runnable callback);

    public class EnforcePlayerHides implements Listener {

        public Plugin denizenPlugin;

        @EventHandler
        public void onPlayerJoin(PlayerJoinEvent event) {
            for (UUID id : hiddenByDefaultPlayers) {
                Entity pTarget = Bukkit.getEntity(id);
                if (pTarget != null && pTarget instanceof Player) {
                    event.getPlayer().hidePlayer((Player) pTarget);
                }
            }
            final Player pl = event.getPlayer();
            final Set<UUID> hides = hiddenEntitiesPlEnt.get(pl.getUniqueId());
            if (hides == null || hides.isEmpty()) {
                return;
            }
            new BukkitRunnable() {
                @Override
                public void run() {
                    if (pl.isOnline()) {
                        for (UUID id : hides) {
                            Entity ent = Bukkit.getEntity(id);
                            if (ent != null) {
                                sendHidePacket(pl, ent);
                            }
                        }
                    }
                }
            }.runTaskLater(denizenPlugin, 5);
        }
    }

    public void ensurePlayerHiding() {
        if (EPH == null) {
            Plugin pl = Bukkit.getPluginManager().getPlugin("Denizen"); // Very lazy way to get the correct plugin instance
            EPH = new EnforcePlayerHides();
            EPH.denizenPlugin = pl;
            Bukkit.getPluginManager().registerEvents(EPH, pl);
        }
    }

    public boolean addHide(UUID player, UUID entity) {
        Set<UUID> hidden = hiddenEntitiesEntPl.get(entity);
        if (hidden == null) {
            hidden = new HashSet<>();
            hiddenEntitiesEntPl.put(entity, hidden);
        }
        if (player.equals(DEFAULT_HIDE)) {
            for (UUID pl : hidden) {
                Set<UUID> plHid = hiddenEntitiesPlEnt.get(pl);
                if (plHid != null) {
                    plHid.remove(entity);
                }
            }
            hidden.clear();
        }
        else {
            Set<UUID> plHid = hiddenEntitiesPlEnt.get(player);
            if (plHid == null) {
                plHid = new HashSet<>();
                hiddenEntitiesPlEnt.put(player, plHid);
            }
            plHid.add(entity);
        }
        return hidden.add(player);
    }

    public void hideEntity(Player player, Entity entity, boolean keepInTabList) { // TODO: remove or reimplement tablist option somehow?
        if (player == null) {
            addHide(DEFAULT_HIDE, entity.getUniqueId());
            if (entity instanceof Player) {
                hiddenByDefaultPlayers.add(entity.getUniqueId());
            }
            for (Player pl : Bukkit.getOnlinePlayers()) {
                sendHidePacket(pl, entity);
            }
            return;
        }
        if (isHiddenByDefault(entity)) {
            removeHide(player.getUniqueId(), entity.getUniqueId());
        }
        else {
            addHide(player.getUniqueId(), entity.getUniqueId());
        }
        sendHidePacket(player, entity);
    }

    public static boolean removeHide(UUID player, UUID entity) {
        Set<UUID> hidden = hiddenEntitiesEntPl.get(entity);
        if (hidden == null) {
            return false;
        }
        boolean toRet = hidden.remove(player);
        if (player.equals(DEFAULT_HIDE)) {
            for (UUID pl : hidden) {
                Set<UUID> plHid = hiddenEntitiesPlEnt.get(pl);
                if (plHid != null) {
                    plHid.remove(entity);
                }
            }
            hidden.clear();
        }
        else {
            Set<UUID> plHid = hiddenEntitiesPlEnt.get(player);
            if (plHid != null) {
                plHid.remove(entity);
            }
        }
        return toRet;
    }

    public void unhideEntity(Player player, Entity entity) {
        if (player == null) {
            removeHide(DEFAULT_HIDE, entity.getUniqueId());
            if (entity instanceof Player) {
                hiddenByDefaultPlayers.remove(entity.getUniqueId());
            }
            for (Player pl : Bukkit.getOnlinePlayers()) {
                sendShowPacket(pl, entity);
            }
            return;
        }
        if (isHiddenByDefault(entity)) {
            addHide(player.getUniqueId(), entity.getUniqueId());
        }
        else {
            removeHide(player.getUniqueId(), entity.getUniqueId());
        }
        sendShowPacket(player, entity);
    }

    public static UUID DEFAULT_HIDE = new UUID(0, 0);

    public boolean isHiddenByDefault(Entity ent) {
        Set<UUID> hiding = hiddenEntitiesEntPl.get(ent.getUniqueId());
        return hiding != null && hiding.contains(DEFAULT_HIDE);
    }

    public boolean isHidden(Player player, Entity entity) {
        if (isHiddenByDefault(entity)) {
            Set<UUID> hiding = hiddenEntitiesEntPl.get(entity.getUniqueId());
            return hiding == null || !hiding.contains(player.getUniqueId());
        }
        Set<UUID> hiding = hiddenEntitiesEntPl.get(entity.getUniqueId());
        return hiding != null && hiding.contains(player.getUniqueId());
    }

    public static Map<UUID, Set<UUID>> hiddenEntitiesEntPl = new HashMap<>();

    public static Map<UUID, Set<UUID>> hiddenEntitiesPlEnt = new HashMap<>();

    public static EnforcePlayerHides EPH = null;

    public static Set<UUID> hiddenByDefaultPlayers = new HashSet<>();

    public abstract void sendHidePacket(Player pl, Entity entity);

    public abstract void sendShowPacket(Player pl, Entity entity);

    /**
     * Rotates an entity.
     *
     * @param entity The Entity you want to rotate.
     * @param yaw    The new yaw of the entity.
     * @param pitch  The new pitch of the entity.
     */
    public abstract void rotate(Entity entity, float yaw, float pitch);

    public abstract float getBaseYaw(Entity entity);

    // Taken from C2 NMS class for less dependency on C2
    public abstract void look(Entity entity, float yaw, float pitch);

    public static class MapTraceResult {
        public Location hitLocation;
        public BlockFace angle;
    }

    public abstract boolean canTrace(World world, Vector start, Vector end);

    public abstract MapTraceResult mapTrace(LivingEntity from, double range);

    /**
     * Gets the precise location in the specified direction.
     *
     * @param start     The location to start the check from.
     * @param direction The one-length vector to use as a direction.
     * @param range     The maximum distance between the start and end.
     * @return The location, or null if it isn't in range.
     */
    public abstract Location rayTrace(Location start, Vector direction, double range);

    public abstract Location rayTraceBlock(Location start, Vector direction, double range);

    public abstract Location getImpactNormal(Location start, Vector direction, double range);

    /**
     * Gets the precise location a LivingEntity is looking at.
     *
     * @param from  The LivingEntity to start the trace from.
     * @param range The maximum distance between the LivingEntity and the location.
     * @return The location, or null if it isn't in range.
     */
    public Location eyeTrace(LivingEntity from, double range) {
        Location start = from.getEyeLocation();
        double xzLen = Math.cos((start.getPitch() % 360) * (Math.PI / 180));
        double nx = xzLen * Math.sin(-start.getYaw() * (Math.PI / 180));
        double ny = Math.sin(start.getPitch() * (Math.PI / 180));
        double nz = xzLen * Math.cos(start.getYaw() * (Math.PI / 180));
        return rayTrace(start, new Vector(nx, -ny, nz), range);
    }

    public Location faceLocation(Location from, Location at) {
        Vector direction = from.toVector().subtract(at.toVector()).normalize();
        Location newLocation = from.clone();
        newLocation.setYaw(180 - (float) Math.toDegrees(Math.atan2(direction.getX(), direction.getZ())));
        newLocation.setPitch(90 - (float) Math.toDegrees(Math.acos(direction.getY())));
        return newLocation;
    }

    /**
     * Changes an entity's yaw and pitch to make it face a location.
     *
     * @param from The Entity whose yaw and pitch you want to change.
     * @param at   The Location it should be looking at.
     */
    public void faceLocation(Entity from, Location at) {
        if (from.getWorld() != at.getWorld()) {
            return;
        }
        Location origin = from instanceof LivingEntity ? ((LivingEntity) from).getEyeLocation()
                : from.getLocation().getBlock().getLocation().add(0.5, 0.5, 0.5);
        Location rotated = faceLocation(origin, at);
        rotate(from, rotated.getYaw(), rotated.getPitch());
    }

    /**
     * Changes an entity's yaw and pitch to make it face another entity.
     *
     * @param entity The Entity whose yaw and pitch you want to change.
     * @param target The Entity it should be looking at.
     */
    public void faceEntity(Entity entity, Entity target) {
        faceLocation(entity, target.getLocation());
    }

    public boolean isFacingLocation(Location from, Location at, float yawLimitDegrees, float pitchLimitDegrees) {
        Vector direction = from.toVector().subtract(at.toVector()).normalize();
        float pitch = 90 - (float) Math.toDegrees(Math.acos(direction.getY()));
        if (from.getPitch() > pitch + pitchLimitDegrees
                || from.getPitch() < pitch - pitchLimitDegrees) {
            return false;
        }

        return isFacingLocation(from, at, yawLimitDegrees);
    }

    /**
     * Checks if a Location's yaw is facing another Location.
     * <p/>
     * Note: do not use a player's location as the first argument,
     * because player yaws need to modified. Use the method
     * below this one instead.
     *
     * @param from        The Location we check.
     * @param at          The Location we want to know if the first Location's yaw
     *                    is facing
     * @param degreeLimit How many degrees can be between the direction the
     *                    first location's yaw is facing and the direction
     *                    we check if it is facing.
     * @return Returns a boolean.
     */
    public boolean isFacingLocation(Location from, Location at, float degreeLimit) {
        double currentYaw = normalizeYaw(from.getYaw());
        double requiredYaw = normalizeYaw(getYaw(at.toVector().subtract(
                from.toVector()).normalize()));
        return (Math.abs(requiredYaw - currentYaw) < degreeLimit ||
                Math.abs(requiredYaw + 360 - currentYaw) < degreeLimit ||
                Math.abs(currentYaw + 360 - requiredYaw) < degreeLimit);
    }

    /**
     * Checks if an Entity is facing a Location.
     *
     * @param from        The Entity we check.
     * @param at          The Location we want to know if it is looking at.
     * @param degreeLimit How many degrees can be between the direction the
     *                    Entity is facing and the direction we check if it
     *                    is facing.
     * @return Returns a boolean.
     */
    public boolean isFacingLocation(Entity from, Location at, float degreeLimit) {
        return isFacingLocation(from.getLocation(), at, degreeLimit);
    }

    /**
     * Checks if an Entity is facing another Entity.
     *
     * @param from        The Entity we check.
     * @param at          The Entity we want to know if it is looking at.
     * @param degreeLimit How many degrees can be between the direction the
     *                    Entity is facing and the direction we check if it
     *                    is facing.
     * @return Returns a boolean.
     */
    public boolean isFacingEntity(Entity from, Entity at, float degreeLimit) {
        return isFacingLocation(from.getLocation(), at.getLocation(), degreeLimit);
    }

    /**
     * Normalizes Mincraft's yaws (which can be negative or can exceed 360)
     * by turning them into proper yaw values that only go from 0 to 359.
     *
     * @param yaw The original yaw.
     * @return The normalized yaw.
     */
    public float normalizeYaw(float yaw) {
        yaw = yaw % 360;
        if (yaw < 0) {
            yaw += 360.0;
        }
        return yaw;
    }

    /**
     * Converts a vector to a yaw.
     * <p/>
     * Thanks to bergerkiller.
     *
     * @param vector The vector you want to get a yaw from.
     * @return The yaw.
     */
    public float getYaw(Vector vector) {
        double dx = vector.getX();
        double dz = vector.getZ();
        double yaw = 0;
        // Set yaw
        if (dx != 0) {
            // Set yaw start value based on dx
            if (dx < 0) {
                yaw = 1.5 * Math.PI;
            }
            else {
                yaw = 0.5 * Math.PI;
            }
            yaw -= Math.atan(dz / dx);
        }
        else if (dz < 0) {
            yaw = Math.PI;
        }
        return (float) (-yaw * 180 / Math.PI);
    }

    /**
     * Converts a yaw to a cardinal direction name.
     *
     * @param yaw The yaw you want to get a cardinal direction from.
     * @return The name of the cardinal direction as a String.
     */
    public String getCardinal(float yaw) {
        yaw = normalizeYaw(yaw);
        // Compare yaws, return closest direction.
        if (0 <= yaw && yaw < 22.5) {
            return "south";
        }
        else if (22.5 <= yaw && yaw < 67.5) {
            return "southwest";
        }
        else if (67.5 <= yaw && yaw < 112.5) {
            return "west";
        }
        else if (112.5 <= yaw && yaw < 157.5) {
            return "northwest";
        }
        else if (157.5 <= yaw && yaw < 202.5) {
            return "north";
        }
        else if (202.5 <= yaw && yaw < 247.5) {
            return "northeast";
        }
        else if (247.5 <= yaw && yaw < 292.5) {
            return "east";
        }
        else if (292.5 <= yaw && yaw < 337.5) {
            return "southeast";
        }
        else if (337.5 <= yaw && yaw < 360.0) {
            return "south";
        }
        else {
            return null;
        }
    }

    public abstract void move(Entity entity, Vector vector);

    public abstract void teleport(Entity entity, Vector vector);

    public abstract BoundingBox getBoundingBox(Entity entity);

    public abstract void setBoundingBox(Entity entity, BoundingBox boundingBox);

    public abstract boolean isChestedHorse(Entity horse);

    public abstract boolean isCarryingChest(Entity horse);

    public abstract void setCarryingChest(Entity horse, boolean carrying);

    public BlockData getBlockDataFor(FallingBlock entity) {
        return NMSHandler.getInstance().getBlockHelper().getBlockData(entity.getMaterial(), (byte) 0);
    }
}

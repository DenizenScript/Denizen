package com.denizenscript.denizen.nms.interfaces;

import com.denizenscript.denizen.nms.util.BoundingBox;
import com.denizenscript.denizen.nms.util.jnbt.CompoundTag;
import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizen.objects.LocationTag;
import com.denizenscript.denizencore.utilities.debugging.Debug;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.BlockFace;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.*;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import java.util.*;

public abstract class EntityHelper {

    public void setInvisible(Entity entity, boolean invisible) {
        // Do nothing on older versions
    }

    public abstract double getAbsorption(LivingEntity entity);

    public abstract void setAbsorption(LivingEntity entity, double value);

    public abstract void setSneaking(Entity player, boolean sneak);

    public abstract double getDamageTo(LivingEntity attacker, Entity target);

    public abstract String getRawHoverText(Entity entity);

    public List<String> getDiscoveredRecipes(Player player) {
        throw new UnsupportedOperationException();
    }

    public void setRiptide(Entity entity, boolean state) {
        Debug.echoError("Riptide control not available on this server version.");
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

    public abstract ItemStack getItemFromTrident(Entity entity);

    public abstract void setItemForTrident(Entity entity, ItemStack item);

    public abstract void forceInteraction(Player player, Location location);

    public abstract Entity getEntity(World world, UUID uuid);

    public abstract void setTarget(Creature entity, LivingEntity target);

    public abstract CompoundTag getNbtData(Entity entity);

    public abstract void setNbtData(Entity entity, CompoundTag compoundTag);

    public abstract void stopFollowing(Entity follower);

    public abstract void stopWalking(Entity entity);

    public abstract double getSpeed(Entity entity);

    public abstract void setSpeed(Entity entity, double speed);

    public abstract void follow(final Entity target, final Entity follower, final double speed, final double lead,
                                final double maxRange, final boolean allowWander, final boolean teleport);

    public abstract void walkTo(final LivingEntity entity, Location location, Double speed, final Runnable callback);

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
                : new LocationTag(from.getLocation()).getBlockLocation().add(0.5, 0.5, 0.5);
        Location rotated = faceLocation(origin, at);
        rotate(from, rotated.getYaw(), rotated.getPitch());
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
    public static float normalizeYaw(float yaw) {
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

    public abstract void snapPositionTo(Entity entity, Vector vector);

    public abstract void move(Entity entity, Vector vector);

    public abstract void teleport(Entity entity, Location loc);

    public abstract BoundingBox getBoundingBox(Entity entity);

    public abstract void setBoundingBox(Entity entity, BoundingBox boundingBox);

    public abstract boolean isChestedHorse(Entity horse);

    public abstract boolean isCarryingChest(Entity horse);

    public abstract void setCarryingChest(Entity horse, boolean carrying);

    public List<Player> getPlayersThatSee(Entity entity) {
        throw new UnsupportedOperationException();
    }

    public void setTicksLived(Entity entity, int ticks) {
        entity.setTicksLived(ticks);
    }

    public int getShulkerPeek(Entity entity) {
        throw new UnsupportedOperationException();
    }

    public void setShulkerPeek(Entity entity, int peek) {
        throw new UnsupportedOperationException();
    }

    public void setHeadAngle(Entity entity, float angle) {
        throw new UnsupportedOperationException();
    }

    public void setGhastAttacking(Entity entity, boolean attacking) {
        throw new UnsupportedOperationException();
    }

    public void setEndermanAngry(Entity entity, boolean angry) {
        throw new UnsupportedOperationException();
    }

    public static EntityDamageEvent fireFakeDamageEvent(Entity target, Entity source, EntityDamageEvent.DamageCause cause, float amount) {
        EntityDamageEvent ede = source == null ? new EntityDamageEvent(target, cause, amount) : new EntityDamageByEntityEvent(source, target, cause, amount);
        Bukkit.getPluginManager().callEvent(ede);
        return ede;
    }

    public void damage(LivingEntity target, float amount, Entity source, EntityDamageEvent.DamageCause cause) {
        if (cause == null) {
            if (source == null) {
                target.damage(amount);
            }
            else {
                target.damage(amount, source);
            }
        }
        else {
            EntityDamageEvent ede = fireFakeDamageEvent(target, source, cause, amount);
            if (!ede.isCancelled()) {
                target.setLastDamageCause(ede);
                if (source == null) {
                    target.damage(ede.getFinalDamage());
                }
                else {
                    target.damage(ede.getFinalDamage(), source);
                }
                target.setLastDamageCause(ede);
            }
        }
    }

    public void setLastHurtBy(LivingEntity mob, LivingEntity damager) {
        throw new UnsupportedOperationException();
    }

    public void setFallingBlockType(FallingBlock entity, BlockData block) {
        throw new UnsupportedOperationException();
    }

    public EntityTag getMobSpawnerDisplayEntity(CreatureSpawner spawner) {
        throw new UnsupportedOperationException();
    }

    public void setFireworkLifetime(Firework firework, int ticks) {
        throw new UnsupportedOperationException();
    }

    public int getFireworkLifetime(Firework firework) {
        throw new UnsupportedOperationException();
    }
}

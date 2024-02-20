package com.denizenscript.denizen.nms.interfaces;

import com.denizenscript.denizen.nms.util.jnbt.CompoundTag;
import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizen.objects.LocationTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.core.MapTag;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.BlockFace;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.*;
import org.bukkit.event.entity.EntityDamageByBlockEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.meta.MapMeta;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

import java.util.List;
import java.util.UUID;

public abstract class EntityHelper {

    public abstract void setInvisible(Entity entity, boolean invisible);

    public abstract boolean isInvisible(Entity entity);

    public abstract void setPose(Entity entity, Pose pose);

    public void setSneaking(Entity entity, boolean sneak) {
        if (entity instanceof Player player) {
            player.setSneaking(sneak);
        }
        setPose(entity, sneak ? Pose.SNEAKING : Pose.STANDING);
    }

    public abstract double getDamageTo(LivingEntity attacker, Entity target);

    public abstract void setRiptide(Entity entity, boolean state);

    public abstract void forceInteraction(Player player, Location location);

    public abstract CompoundTag getNbtData(Entity entity);

    public abstract void setNbtData(Entity entity, CompoundTag compoundTag);

    public abstract void stopFollowing(Entity follower);

    public abstract void stopWalking(Entity entity);

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

    public abstract float getBaseYaw(LivingEntity entity);

    // Taken from C2 NMS class for less dependency on C2
    public abstract void look(Entity entity, float yaw, float pitch);

    public MapTag mapTrace(LivingEntity inputEntity) {
        double range = 200;
        Location start = inputEntity.getEyeLocation();
        Vector startVec = start.toVector();
        Vector direction = start.getDirection();
        double bestDist = Double.MAX_VALUE;
        ItemFrame best = null;
        Vector bestHitPos = null;
        BlockFace bestHitFace = null;
        for (Entity entity : start.getWorld().getNearbyEntities(start.clone().add(direction.clone().multiply(50)), 100, 100, 100, (e) -> e instanceof ItemFrame itemFrame && itemFrame.getItem().getType() == Material.FILLED_MAP)) {
            double centerDist = entity.getLocation().distanceSquared(start);
            if (centerDist > bestDist) {
                continue;
            }
            ItemFrame frame = (ItemFrame) entity;
            double EXP_RATE = 0.125;
            double expandX = 0, expandY = 0, expandZ = 0;
            BlockFace face = frame.getFacing();
            switch (face) {
                case SOUTH, NORTH -> {
                    expandX = EXP_RATE;
                    expandY = EXP_RATE;
                }
                case EAST, WEST -> {
                    expandZ = EXP_RATE;
                    expandY = EXP_RATE;
                }
                case UP, DOWN -> {
                    expandX = EXP_RATE;
                    expandZ = EXP_RATE;
                }
            }
            RayTraceResult traced = frame.getBoundingBox().expand(expandX, expandY, expandZ).rayTrace(startVec, direction, range);
            if (traced == null || traced.getHitBlockFace() == null || traced.getHitBlockFace() != face) {
                continue;
            }
            bestDist = centerDist;
            best = frame;
            bestHitPos = traced.getHitPosition();
            bestHitFace = face;
        }
        if (best == null) {
            return null;
        }
        double x = 0;
        double y = 0;
        double basex = bestHitPos.getX() - Math.floor(bestHitPos.getX());
        double basey = bestHitPos.getY() - Math.floor(bestHitPos.getY());
        double basez = bestHitPos.getZ() - Math.floor(bestHitPos.getZ());
        switch (bestHitFace) {
            case NORTH -> {
                x = 128f - (basex * 128f);
                y = 128f - (basey * 128f);
            }
            case SOUTH -> {
                x = basex * 128f;
                y = 128f - (basey * 128f);
            }
            case WEST -> {
                x = basez * 128f;
                y = 128f - (basey * 128f);
            }
            case EAST -> {
                x = 128f - (basez * 128f);
                y = 128f - (basey * 128f);
            }
            case UP -> {
                x = basex * 128f;
                y = basez * 128f;
            }
            case DOWN -> {
                x = basex * 128f;
                y = 128f - (basez * 128f);
            }
        }
        MapMeta map = (MapMeta) best.getItem().getItemMeta();
        switch (best.getRotation()) {
            case CLOCKWISE_45, FLIPPED_45 -> { // 90 deg
                double origX = x;
                x = y;
                y = 128f - origX;
            }
            case CLOCKWISE, COUNTER_CLOCKWISE -> { // 180 deg
                x = 128f - x;
                y = 128f - y;
            }
            case CLOCKWISE_135, COUNTER_CLOCKWISE_45 -> { // 270 deg
                double origX2 = x;
                x = 128f - y;
                y = origX2;
            }
        }
        MapTag result = new MapTag();
        result.putObject("x", new ElementTag(Math.round(x)));
        result.putObject("y", new ElementTag(Math.round(y)));
        result.putObject("entity", new EntityTag(best));
        result.putObject("map", new ElementTag(map.hasMapId() ? map.getMapId() : 0));
        return result;
    }

    public abstract boolean canTrace(World world, Vector start, Vector end);

    public Location faceLocation(Location from, Location at) {
        Vector direction = from.toVector().subtract(at.toVector()).normalize();
        Location newLocation = from.clone();
        newLocation.setYaw(180 - (float) Math.toDegrees(Math.atan2(direction.getX(), direction.getZ())));
        newLocation.setPitch(90 - (float) Math.toDegrees(Math.acos(direction.getY())));
        return newLocation;
    }

    public boolean internalLook(Player player, Location at) {
        return false;
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
        if (EntityTag.isPlayer(from)) {
            if (internalLook((Player) from, at)) {
                return;
            }
        }
        Location origin = from instanceof LivingEntity livingEntity ? livingEntity.getEyeLocation()
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

    public void fakeMove(Entity entity, Vector vector) {
        throw new UnsupportedOperationException();
    }

    public void fakeTeleport(Entity entity, Location location) {
        throw new UnsupportedOperationException();
    }

    public void clientResetLoc(Entity entity) {
        throw new UnsupportedOperationException();
    }

    public abstract void teleport(Entity entity, Location loc);

    public abstract void setBoundingBox(Entity entity, BoundingBox box);

    public List<Player> getPlayersThatSee(Entity entity) { // TODO: once the minimum supported version is 1.20, remove from NMS
        return List.copyOf(entity.getTrackedBy());
    }

    public void sendAllUpdatePackets(Entity entity) {
        throw new UnsupportedOperationException();
    }

    public abstract void setTicksLived(Entity entity, int ticks);

    public abstract void setHeadAngle(LivingEntity entity, float angle);

    public void setGhastAttacking(Ghast ghast, boolean attacking) { // TODO: once minimum version is 1.19 or higher, remove from NMS
        ghast.setCharging(attacking);
    }

    public abstract void setEndermanAngry(Enderman enderman, boolean angry);

    public static EntityDamageEvent fireFakeDamageEvent(Entity target, EntityTag source, Location sourceLoc, EntityDamageEvent.DamageCause cause, float amount) {
        EntityDamageEvent ede;
        if (source != null) {
            ede = new EntityDamageByEntityEvent(source.getBukkitEntity(), target, cause, amount);
        }
        else if (sourceLoc != null) {
            ede = new EntityDamageByBlockEvent(sourceLoc.getBlock(), target, cause, amount);
        }
        else {
            ede = new EntityDamageEvent(target, cause, amount);
        }
        Bukkit.getPluginManager().callEvent(ede);
        return ede;
    }

    public abstract void damage(LivingEntity target, float amount, EntityTag source, Location sourceLoc, EntityDamageEvent.DamageCause cause);

    public abstract void setLastHurtBy(LivingEntity mob, LivingEntity damager);

    public abstract void setFallingBlockType(FallingBlock fallingBlock, BlockData block);

    public abstract EntityTag getMobSpawnerDisplayEntity(CreatureSpawner spawner);

    public void setFireworkLifetime(Firework firework, int ticks) { // TODO: once minimum version is 1.19, remove from NMS
        firework.setMaxLife(ticks);
    }

    public int getFireworkLifetime(Firework firework) { // TODO: once minimum version is 1.19, remove from NMS
        return firework.getMaxLife();
    }

    public abstract int getInWaterTime(Zombie zombie);

    public abstract void setInWaterTime(Zombie zombie, int ticks);

    public abstract void setTrackingRange(Entity entity, int range);

    public abstract boolean isAggressive(Mob mob);

    public abstract void setAggressive(Mob mob, boolean aggressive);

    public void setUUID(Entity entity, UUID id) {
        throw new UnsupportedOperationException();
    }

    public float getStepHeight(Entity entity) {
        throw new UnsupportedOperationException();
    }

    public void setStepHeight(Entity entity, float stepHeight) {
        throw new UnsupportedOperationException();
    }

    public List<Object> convertInternalEntityDataValues(Entity entity, MapTag internalData) {
        throw new UnsupportedOperationException();
    }

    public void modifyInternalEntityData(Entity entity, MapTag internalData) {
        throw new UnsupportedOperationException();
    }

    public void startUsingItem(LivingEntity entity, EquipmentSlot hand) {
        throw new UnsupportedOperationException();
    }

    public void stopUsingItem(LivingEntity entity) {
        throw new UnsupportedOperationException();
    }

    public abstract void openHorseInventory(Player player, AbstractHorse horse);

    public CompoundTag getRawNBT(Entity entity) {
        throw new UnsupportedOperationException();
    }

    public void modifyRawNBT(Entity entity, CompoundTag tag) {
        throw new UnsupportedOperationException();
    }
}

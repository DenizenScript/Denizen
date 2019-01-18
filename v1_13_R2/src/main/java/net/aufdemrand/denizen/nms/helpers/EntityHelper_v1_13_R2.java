package net.aufdemrand.denizen.nms.helpers;

import net.aufdemrand.denizen.nms.NMSHandler;
import net.aufdemrand.denizen.nms.impl.jnbt.CompoundTag_v1_13_R2;
import net.aufdemrand.denizen.nms.interfaces.EntityHelper;
import net.aufdemrand.denizen.nms.util.BoundingBox;
import net.aufdemrand.denizen.nms.util.Utilities;
import net.aufdemrand.denizen.nms.util.jnbt.CompoundTag;
import net.minecraft.server.v1_13_R2.*;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.BlockFace;
import org.bukkit.craftbukkit.v1_13_R2.CraftWorld;
import org.bukkit.craftbukkit.v1_13_R2.block.CraftBlock;
import org.bukkit.craftbukkit.v1_13_R2.entity.CraftAnimals;
import org.bukkit.craftbukkit.v1_13_R2.entity.CraftCreature;
import org.bukkit.craftbukkit.v1_13_R2.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_13_R2.entity.CraftLivingEntity;
import org.bukkit.craftbukkit.v1_13_R2.entity.CraftPlayer;
import org.bukkit.entity.*;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class EntityHelper_v1_13_R2 implements EntityHelper {

    /*
        General Entity Methods
     */

    @Override
    public void setRiptide(Entity entity, boolean state) {
        // https://wiki.vg/Entity_metadata#Living
        ((CraftEntity) entity).getHandle().getDataWatcher().set(DataWatcherRegistry.a.a(6), (byte) (state ? 4 : 0));
    }

    @Override
    public int getBodyArrows(Entity entity) {
        // https://wiki.vg/Entity_metadata#Living
        return ((CraftEntity) entity).getHandle().getDataWatcher().get(DataWatcherRegistry.b.a(10));
    }

    @Override
    public void setBodyArrows(Entity entity, int numArrows) {
        // https://wiki.vg/Entity_metadata#Living
        ((CraftEntity) entity).getHandle().getDataWatcher().set(DataWatcherRegistry.b.a(10), numArrows);
    }

    @Override
    public Entity getFishHook(PlayerFishEvent event) {
        return event.getHook();
    }

    @Override
    public void forceInteraction(Player player, Location location) {
        CraftPlayer craftPlayer = (CraftPlayer) player;
        BlockPosition pos = new BlockPosition(location.getBlockX(), location.getBlockY(), location.getBlockZ());
        ((CraftBlock) location.getBlock()).getNMS().interact(((CraftWorld) location.getWorld()).getHandle(), pos,
                        craftPlayer != null ? craftPlayer.getHandle() : null, EnumHand.MAIN_HAND,
                        null, 0f, 0f, 0f);
    }

    @Override
    public Entity getEntity(World world, UUID uuid) {
        net.minecraft.server.v1_13_R2.Entity entity = ((CraftWorld) world).getHandle().getEntity(uuid);
        return entity == null ? null : entity.getBukkitEntity();
    }

    @Override
    public boolean isBreeding(Animals entity) {
        return ((CraftAnimals) entity).getHandle().isInLove();
    }

    @Override
    public void setBreeding(Animals entity, boolean breeding) {
        if (breeding) {
            ((CraftAnimals) entity).getHandle().a((EntityHuman) null);
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

    @Override
    public CompoundTag getNbtData(Entity entity) {
        NBTTagCompound compound = new NBTTagCompound();
        ((CraftEntity) entity).getHandle().c(compound);
        return CompoundTag_v1_13_R2.fromNMSTag(compound);
    }

    @Override
    public void setNbtData(Entity entity, CompoundTag compoundTag) {
        ((CraftEntity) entity).getHandle().f(((CompoundTag_v1_13_R2) compoundTag).toNMSTag());
    }

    @Override
    public void setSilent(Entity entity, boolean silent) {
        entity.setSilent(silent);
    }

    @Override
    public boolean isSilent(Entity entity) {
        return entity.isSilent();
    }

    @Override
    public ItemStack getItemInHand(LivingEntity entity) {
        return entity.getEquipment().getItemInMainHand();
    }

    @Override
    public void setItemInHand(LivingEntity entity, ItemStack itemStack) {
        entity.getEquipment().setItemInMainHand(itemStack);
    }

    @Override
    public ItemStack getItemInOffHand(LivingEntity entity) {
        return entity.getEquipment().getItemInOffHand();
    }

    @Override
    public void setItemInOffHand(LivingEntity entity, ItemStack itemStack) {
        entity.getEquipment().setItemInOffHand(itemStack);
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
        net.minecraft.server.v1_13_R2.Entity nmsEntity = ((CraftEntity) entity).getHandle();
        if (!(nmsEntity instanceof EntityInsentient)) {
            return;
        }
        ((EntityInsentient) nmsEntity).getNavigation().q();
    }

    @Override
    public void toggleAI(Entity entity, boolean hasAI) {
        net.minecraft.server.v1_13_R2.Entity nmsEntity = ((CraftEntity) entity).getHandle();
        if (!(nmsEntity instanceof EntityInsentient)) {
            return;
        }
        ((EntityInsentient) nmsEntity).setNoAI(!hasAI);
    }

    @Override
    public boolean isAIDisabled(Entity entity) {
        net.minecraft.server.v1_13_R2.Entity nmsEntity = ((CraftEntity) entity).getHandle();
        if (!(nmsEntity instanceof EntityInsentient)) {
            return true;
        }
        return ((EntityInsentient) nmsEntity).isNoAI();
    }

    @Override
    public double getSpeed(Entity entity) {
        net.minecraft.server.v1_13_R2.Entity nmsEntityEntity = ((CraftEntity) entity).getHandle();
        if (!(nmsEntityEntity instanceof EntityInsentient)) {
            return 0.0;
        }
        EntityInsentient nmsEntity = (EntityInsentient) nmsEntityEntity;
        return nmsEntity.getAttributeInstance(GenericAttributes.MOVEMENT_SPEED).b();
    }

    @Override
    public void setSpeed(Entity entity, double speed) {
        net.minecraft.server.v1_13_R2.Entity nmsEntityEntity = ((CraftEntity) entity).getHandle();
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

        final net.minecraft.server.v1_13_R2.Entity nmsEntityFollower = ((CraftEntity) follower).getHandle();
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
                    followerNavigation.q();
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

        net.minecraft.server.v1_13_R2.Entity nmsEntityEntity = ((CraftEntity) entity).getHandle();
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
                    if (entityNavigation.p() || path.b()) {
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

    public static class EnforcePlayerHides implements Listener {

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

    public static Map<UUID, Set<UUID>> hiddenEntitiesEntPl = new HashMap<UUID, Set<UUID>>();

    public static Map<UUID, Set<UUID>> hiddenEntitiesPlEnt = new HashMap<UUID, Set<UUID>>();

    public static EnforcePlayerHides EPH = null;

    public static Set<UUID> hiddenByDefaultPlayers = new HashSet<UUID>();

    public static void ensurePlayerHiding() {
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
            hidden = new HashSet<UUID>();
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
                plHid = new HashSet<UUID>();
                hiddenEntitiesPlEnt.put(player, plHid);
            }
            plHid.add(entity);
        }
        return hidden.add(player);
    }

    public static void sendHidePacket(Player pl, Entity entity) {
        if (entity instanceof Player) {
            ensurePlayerHiding();
            pl.hidePlayer((Player) entity);
            return;
        }
        CraftPlayer craftPlayer = (CraftPlayer) pl;
        EntityPlayer entityPlayer = craftPlayer.getHandle();
        if (entityPlayer.playerConnection != null && !craftPlayer.equals(entity)) {
            EntityTracker tracker = ((WorldServer) craftPlayer.getHandle().world).tracker;
            net.minecraft.server.v1_13_R2.Entity other = ((CraftEntity) entity).getHandle();
            EntityTrackerEntry entry = tracker.trackedEntities.get(other.getId());
            if (entry != null) {
                entry.clear(entityPlayer);
            }
        }
    }

    @Override
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

    public void sendShowPacket(Player pl, Entity entity) {
        if (entity instanceof Player) {
            pl.showPlayer((Player) entity);
            return;
        }
        CraftPlayer craftPlayer = (CraftPlayer) pl;
        EntityPlayer entityPlayer = craftPlayer.getHandle();
        if (entityPlayer.playerConnection != null && !craftPlayer.equals(entity)) {
            EntityTracker tracker = ((WorldServer) craftPlayer.getHandle().world).tracker;
            net.minecraft.server.v1_13_R2.Entity other = ((CraftEntity) entity).getHandle();
            EntityTrackerEntry entry = tracker.trackedEntities.get(other.getId());
            if (entry != null) {
                entry.clear(entityPlayer);
                entry.updatePlayer(entityPlayer);
            }
        }
    }

    @Override
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

    public boolean isHiddenByDefault(Entity ent) { // TODO: Backport?
        Set<UUID> hiding = hiddenEntitiesEntPl.get(ent.getUniqueId());
        return hiding != null && hiding.contains(DEFAULT_HIDE);
    }

    @Override
    public boolean isHidden(Player player, Entity entity) {
        if (isHiddenByDefault(entity)) {
            Set<UUID> hiding = hiddenEntitiesEntPl.get(entity.getUniqueId());
            return hiding == null || !hiding.contains(player.getUniqueId());
        }
        Set<UUID> hiding = hiddenEntitiesEntPl.get(entity.getUniqueId());
        return hiding != null && hiding.contains(player.getUniqueId());
    }

    @Override
    public void rotate(Entity entity, float yaw, float pitch) {
        // If this entity is a real player instead of a player type NPC,
        // it will appear to be online
        if (entity instanceof Player && ((Player) entity).isOnline()) {
            Location location = entity.getLocation();
            location.setYaw(yaw);
            location.setPitch(pitch);
            entity.teleport(location);
        }
        else if (entity instanceof LivingEntity) {
            if (entity instanceof EnderDragon) {
                yaw = normalizeYaw(yaw - 180);
            }
            look(entity, yaw, pitch);
        }
        else {
            net.minecraft.server.v1_13_R2.Entity handle = ((CraftEntity) entity).getHandle();
            handle.yaw = yaw;
            handle.pitch = pitch;
        }
    }

    @Override
    public float getBaseYaw(Entity entity) {
        net.minecraft.server.v1_13_R2.Entity handle = ((CraftEntity) entity).getHandle();
        return ((EntityLiving) handle).aR;
    }

    @Override
    public void look(Entity entity, float yaw, float pitch) {
        net.minecraft.server.v1_13_R2.Entity handle = ((CraftEntity) entity).getHandle();
        if (handle != null) {
            handle.yaw = yaw;
            if (handle instanceof EntityLiving) {
                EntityLiving livingHandle = (EntityLiving) handle;
                while (yaw < -180.0F) {
                    yaw += 360.0F;
                }
                while (yaw >= 180.0F) {
                    yaw -= 360.0F;
                }
                livingHandle.aR = yaw;
                if (!(handle instanceof EntityHuman)) {
                    livingHandle.aQ = yaw;
                }
                livingHandle.aS = yaw;
            }
            handle.pitch = pitch;
        }
    }

    private static MovingObjectPosition rayTrace(World world, Vector start, Vector end) {
        return ((CraftWorld) world).getHandle().rayTrace(new Vec3D(start.getX(), start.getY(), start.getZ()),
                new Vec3D(end.getX(), end.getY(), end.getZ()));
    }

    @Override
    public boolean canTrace(World world, Vector start, Vector end) {
        return rayTrace(world, start, end) == null;
    }

    @Override
    public MapTraceResult mapTrace(LivingEntity from, double range) {
        Location start = from.getEyeLocation();
        Vector startVec = start.toVector();
        double xzLen = Math.cos((start.getPitch() % 360) * (Math.PI / 180));
        double nx = xzLen * Math.sin(-start.getYaw() * (Math.PI / 180));
        double ny = Math.sin(start.getPitch() * (Math.PI / 180));
        double nz = xzLen * Math.cos(start.getYaw() * (Math.PI / 180));
        Vector endVec = startVec.clone().add(new Vector(nx, -ny, nz).multiply(range));
        MovingObjectPosition l = rayTrace(start.getWorld(), startVec, endVec);
        if (l == null || l.pos == null) {
            return null;
        }
        Vector finalVec = new Vector(l.pos.x, l.pos.y, l.pos.z);
        MapTraceResult mtr = new MapTraceResult();
        switch (l.direction) {
            case NORTH:
                mtr.angle = BlockFace.NORTH;
                break;
            case SOUTH:
                mtr.angle = BlockFace.SOUTH;
                break;
            case EAST:
                mtr.angle = BlockFace.EAST;
                break;
            case WEST:
                mtr.angle = BlockFace.WEST;
                break;
        }
        // wallPosition - ((end - start).normalize() * 0.072)
        Vector hit = finalVec.clone().subtract((endVec.clone().subtract(startVec)).normalize().multiply(0.072));
        mtr.hitLocation = new Location(start.getWorld(), hit.getX(), hit.getY(), hit.getZ());
        return mtr;
    }

    @Override
    public Location rayTrace(Location start, Vector direction, double range) {
        Vector startVec = start.toVector();
        MovingObjectPosition l = rayTrace(start.getWorld(), startVec, startVec.clone().add(direction.multiply(range)));
        if (l != null && l.pos != null) {
            return new Location(start.getWorld(), l.pos.x, l.pos.y, l.pos.z);
        }
        return null;
    }

    @Override
    public Location getImpactNormal(Location start, Vector direction, double range) {
        Vector startVec = start.toVector();
        MovingObjectPosition l = rayTrace(start.getWorld(), startVec, startVec.clone().add(direction.multiply(range)));
        if (l != null && l.direction != null) {
            return new Location(start.getWorld(), l.direction.getAdjacentX(), l.direction.getAdjacentY(), l.direction.getAdjacentZ());
        }
        return null;
    }

    @Override
    public Location eyeTrace(LivingEntity from, double range) {
        Location start = from.getEyeLocation();
        double xzLen = Math.cos((start.getPitch() % 360) * (Math.PI / 180));
        double nx = xzLen * Math.sin(-start.getYaw() * (Math.PI / 180));
        double ny = Math.sin(start.getPitch() * (Math.PI / 180));
        double nz = xzLen * Math.cos(start.getYaw() * (Math.PI / 180));
        return rayTrace(start, new Vector(nx, -ny, nz), range);
    }

    @Override
    public void faceLocation(Entity from, Location at) {
        if (from.getWorld() != at.getWorld()) {
            return;
        }
        Location origin = from instanceof LivingEntity ? ((LivingEntity) from).getEyeLocation()
                : from.getLocation().getBlock().getLocation().add(0.5, 0.5, 0.5);
        Location rotated = faceLocation(origin, at);
        rotate(from, rotated.getYaw(), rotated.getPitch());
    }

    @Override
    public void faceEntity(Entity entity, Entity target) {
        faceLocation(entity, target.getLocation());
    }

    @Override
    public void move(Entity entity, Vector vector) {
        ((CraftEntity) entity).getHandle().move(EnumMoveType.SELF, vector.getX(), vector.getY(), vector.getZ());
    }

    @Override
    public void teleport(Entity entity, Vector vector) {
        ((CraftEntity) entity).getHandle().setPosition(vector.getX(), vector.getY(), vector.getZ());
    }

    @Override
    public BoundingBox getBoundingBox(Entity entity) {
        AxisAlignedBB boundingBox = ((CraftEntity) entity).getHandle().getBoundingBox();
        Vector position = new Vector(boundingBox.minX, boundingBox.minY, boundingBox.minZ);
        Vector size = new Vector(boundingBox.maxX, boundingBox.maxY, boundingBox.maxZ);
        return new BoundingBox(position, size);
    }

    @Override
    public void setBoundingBox(Entity entity, BoundingBox boundingBox) {
        Vector low = boundingBox.getLow();
        Vector high = boundingBox.getHigh();
        ((CraftEntity) entity).getHandle().a(new AxisAlignedBB(low.getX(), low.getY(), low.getZ(),
                high.getX(), high.getY(), high.getZ()));
    }

    @Override
    public boolean isChestedHorse(Entity horse) {
        return horse instanceof ChestedHorse;
    }

    @Override
    public boolean isCarryingChest(Entity horse) {
        return horse instanceof ChestedHorse && ((ChestedHorse) horse).isCarryingChest();
    }

    @Override
    public void setCarryingChest(Entity horse, boolean carrying) {
        if (horse instanceof ChestedHorse) {
            ((ChestedHorse) horse).setCarryingChest(carrying);
        }
    }
}

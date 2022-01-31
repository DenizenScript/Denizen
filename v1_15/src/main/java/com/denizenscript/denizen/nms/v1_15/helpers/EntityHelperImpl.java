package com.denizenscript.denizen.nms.v1_15.helpers;

import com.denizenscript.denizen.Denizen;
import com.denizenscript.denizen.nms.NMSHandler;
import com.denizenscript.denizencore.utilities.ReflectionHelper;
import com.denizenscript.denizen.nms.v1_15.impl.jnbt.CompoundTagImpl;
import com.denizenscript.denizen.nms.interfaces.EntityHelper;
import com.denizenscript.denizen.nms.util.BoundingBox;
import com.denizenscript.denizen.nms.util.jnbt.CompoundTag;
import com.denizenscript.denizen.utilities.Utilities;
import com.denizenscript.denizen.utilities.debugging.Debug;
import net.minecraft.server.v1_15_R1.*;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.block.BlockFace;
import org.bukkit.craftbukkit.v1_15_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_15_R1.block.CraftBlock;
import org.bukkit.craftbukkit.v1_15_R1.entity.*;
import org.bukkit.craftbukkit.v1_15_R1.inventory.CraftItemStack;
import org.bukkit.entity.*;
import org.bukkit.entity.Entity;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import java.lang.invoke.MethodHandle;
import java.lang.reflect.Field;
import java.util.*;

public class EntityHelperImpl extends EntityHelper {

    public static final Field RECIPE_BOOK_DISCOVERED_SET = ReflectionHelper.getFields(RecipeBook.class).get("a");

    public static final MethodHandle ENTITY_HOVER_TEXT_GETTER = ReflectionHelper.getMethodHandle(net.minecraft.server.v1_15_R1.Entity.class, "bS");

    public static final MethodHandle ENTITY_SETPOSE = ReflectionHelper.getMethodHandle(net.minecraft.server.v1_15_R1.Entity.class, "setPose", EntityPose.class);

    @Override
    public double getAbsorption(LivingEntity entity) {
        return entity.getAbsorptionAmount();
    }

    @Override
    public void setAbsorption(LivingEntity entity, double value) {
        entity.setAbsorptionAmount(value);
    }

    @Override
    public void setSneaking(Entity player, boolean sneak) {
        if (player instanceof Player) {
            ((Player) player).setSneaking(sneak);
        }
        EntityPose pose = sneak ? EntityPose.CROUCHING : EntityPose.STANDING;
        try {
            ENTITY_SETPOSE.invoke(((CraftEntity) player).getHandle(), pose);
        }
        catch (Throwable ex) {
            Debug.echoError(ex);
        }
    }

    @Override
    public double getDamageTo(LivingEntity attacker, Entity target) {
        EnumMonsterType monsterType;
        if (target instanceof LivingEntity) {
            monsterType = ((CraftLivingEntity) target).getHandle().getMonsterType();
        }
        else {
            monsterType = EnumMonsterType.UNDEFINED;
        }
        double damage = 0;
        AttributeInstance attrib = attacker.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE);
        if (attrib != null) {
            damage = attrib.getValue();
        }
        if (attacker.getEquipment() != null && attacker.getEquipment().getItemInMainHand() != null) {
            damage += EnchantmentManager.a(CraftItemStack.asNMSCopy(attacker.getEquipment().getItemInMainHand()), monsterType);
        }
        if (damage <= 0) {
            return 0;
        }
        if (target != null) {
            DamageSource source;
            if (attacker instanceof Player) {
                source = DamageSource.playerAttack(((CraftPlayer) attacker).getHandle());
            }
            else {
                source = DamageSource.mobAttack(((CraftLivingEntity) attacker).getHandle());
            }
            net.minecraft.server.v1_15_R1.Entity nmsTarget = ((CraftEntity) target).getHandle();
            if (nmsTarget.isInvulnerable(source)) {
                return 0;
            }
            if (!(nmsTarget instanceof EntityLiving)) {
                return damage;
            }
            EntityLiving livingTarget = (EntityLiving) nmsTarget;
            damage = CombatMath.a((float) damage, (float) livingTarget.getArmorStrength(), (float) livingTarget.getAttributeInstance(GenericAttributes.ARMOR_TOUGHNESS).getValue());
            int enchantDamageModifier = EnchantmentManager.a(livingTarget.getArmorItems(), source);
            if (enchantDamageModifier > 0) {
                damage = CombatMath.a((float) damage, (float) enchantDamageModifier);
            }
        }
        return damage;
    }

    @Override
    public String getRawHoverText(Entity entity) {
        try {
            ChatHoverable hoverable = (ChatHoverable) ENTITY_HOVER_TEXT_GETTER.invoke(((CraftEntity) entity).getHandle());
            return hoverable.b().getText();
        }
        catch (Throwable ex) {
            ex.printStackTrace();
            return null;
        }
    }

    public List<String> getDiscoveredRecipes(Player player) {
        try {
            RecipeBookServer book = ((CraftPlayer) player).getHandle().B();
            Set<MinecraftKey> set = (Set<MinecraftKey>) RECIPE_BOOK_DISCOVERED_SET.get(book);
            List<String> output = new ArrayList<>();
            for (MinecraftKey key : set) {
                output.add(key.toString());
            }
            return output;
        }
        catch (Throwable ex) {
            Debug.echoError(ex);
        }
        return null;
    }

    @Override
    public String getArrowPickupStatus(Entity entity) {
        return ((Arrow) entity).getPickupStatus().name();
    }

    @Override
    public void setArrowPickupStatus(Entity entity, String status) {
        ((Arrow) entity).setPickupStatus(AbstractArrow.PickupStatus.valueOf(status));
    }

    @Override
    public double getArrowDamage(Entity arrow) {
        return ((Arrow) arrow).getDamage();
    }

    @Override
    public void setArrowDamage(Entity arrow, double damage) {
        ((Arrow) arrow).setDamage(damage);
    }

    @Override
    public void setRiptide(Entity entity, boolean state) {
        ((CraftLivingEntity) entity).getHandle().r(state ? 0 : 1);
    }

    @Override
    public int getBodyArrows(Entity entity) {
        return ((CraftLivingEntity) entity).getHandle().getArrowCount();
    }

    @Override
    public void setBodyArrows(Entity entity, int numArrows) {
        ((CraftLivingEntity) entity).getHandle().setArrowCount(numArrows);
    }

    @Override
    public Entity getFishHook(PlayerFishEvent event) {
        return event.getHook();
    }

    @Override
    public ItemStack getItemFromTrident(Entity entity) {
        return CraftItemStack.asBukkitCopy(((CraftTrident) entity).getHandle().trident);
    }

    @Override
    public void setItemForTrident(Entity entity, ItemStack item) {
        ((CraftTrident) entity).getHandle().trident = CraftItemStack.asNMSCopy(item);
    }

    @Override
    public void forceInteraction(Player player, Location location) {
        CraftPlayer craftPlayer = (CraftPlayer) player;
        BlockPosition pos = new BlockPosition(location.getBlockX(), location.getBlockY(), location.getBlockZ());
        ((CraftBlock) location.getBlock()).getNMS().interact(((CraftWorld) location.getWorld()).getHandle(),
                craftPlayer != null ? craftPlayer.getHandle() : null, EnumHand.MAIN_HAND,
                new MovingObjectPositionBlock(new Vec3D(0, 0, 0), null, pos, false));
    }

    @Override
    public Entity getEntity(World world, UUID uuid) {
        net.minecraft.server.v1_15_R1.Entity entity = ((CraftWorld) world).getHandle().getEntity(uuid);
        return entity == null ? null : entity.getBukkitEntity();
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
        return CompoundTagImpl.fromNMSTag(compound);
    }

    @Override
    public void setNbtData(Entity entity, CompoundTag compoundTag) {
        ((CraftEntity) entity).getHandle().f(((CompoundTagImpl) compoundTag).toNMSTag());
    }

    /*
        Entity Movement
     */

    private final static Map<UUID, BukkitTask> followTasks = new HashMap<>();

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
        net.minecraft.server.v1_15_R1.Entity nmsEntity = ((CraftEntity) entity).getHandle();
        if (!(nmsEntity instanceof EntityInsentient)) {
            return;
        }
        ((EntityInsentient) nmsEntity).getNavigation().o();
    }

    @Override
    public double getSpeed(Entity entity) {
        net.minecraft.server.v1_15_R1.Entity nmsEntityEntity = ((CraftEntity) entity).getHandle();
        if (!(nmsEntityEntity instanceof EntityInsentient)) {
            return 0.0;
        }
        EntityInsentient nmsEntity = (EntityInsentient) nmsEntityEntity;
        return nmsEntity.getAttributeInstance(GenericAttributes.MOVEMENT_SPEED).getBaseValue();
    }

    @Override
    public void setSpeed(Entity entity, double speed) {
        net.minecraft.server.v1_15_R1.Entity nmsEntityEntity = ((CraftEntity) entity).getHandle();
        if (!(nmsEntityEntity instanceof EntityInsentient)) {
            return;
        }
        EntityInsentient nmsEntity = (EntityInsentient) nmsEntityEntity;
        nmsEntity.getAttributeInstance(GenericAttributes.MOVEMENT_SPEED).setValue(speed);
    }

    @Override
    public void follow(final Entity target, final Entity follower, final double speed, final double lead,
                       final double maxRange, final boolean allowWander, final boolean teleport) {
        if (target == null || follower == null) {
            return;
        }

        final net.minecraft.server.v1_15_R1.Entity nmsEntityFollower = ((CraftEntity) follower).getHandle();
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
                        path = followerNavigation.a(targetLocation.getX(), targetLocation.getY(), targetLocation.getZ(), 0);
                        if (path != null) {
                            followerNavigation.a(path, 1D);
                            followerNavigation.a(2D);
                        }
                    }
                }
                else if (!inRadius && !Utilities.checkLocation(targetLocation, follower.getLocation(), lead)) {
                    path = followerNavigation.a(targetLocation.getX(), targetLocation.getY(), targetLocation.getZ(), 0);
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
    public void walkTo(final LivingEntity entity, Location location, Double speed, final Runnable callback) {
        if (entity == null || location == null) {
            return;
        }

        net.minecraft.server.v1_15_R1.Entity nmsEntityEntity = ((CraftEntity) entity).getHandle();
        if (!(nmsEntityEntity instanceof EntityInsentient)) {
            return;
        }
        final EntityInsentient nmsEntity = (EntityInsentient) nmsEntityEntity;
        final NavigationAbstract entityNavigation = nmsEntity.getNavigation();

        final PathEntity path;
        final boolean aiDisabled = !entity.hasAI();
        if (aiDisabled) {
            entity.setAI(true);
            nmsEntity.onGround = true;
        }
        path = entityNavigation.a(location.getX(), location.getY(), location.getZ(), 0);
        if (path != null) {
            entityNavigation.a(path, 1D);
            entityNavigation.a(2D);
            final double oldSpeed = nmsEntity.getAttributeInstance(GenericAttributes.MOVEMENT_SPEED).getBaseValue();
            if (speed != null) {
                nmsEntity.getAttributeInstance(GenericAttributes.MOVEMENT_SPEED).setValue(speed);
            }
            new BukkitRunnable() {
                @Override
                public void run() {
                    if (!entity.isValid()) {
                        if (callback != null) {
                            callback.run();
                        }
                        cancel();
                        return;
                    }
                    if (aiDisabled && entity instanceof Wolf) {
                        ((Wolf) entity).setAngry(false);
                    }
                    if (entityNavigation.n() || path.b()) {
                        if (callback != null) {
                            callback.run();
                        }
                        nmsEntity.getAttributeInstance(GenericAttributes.MOVEMENT_SPEED).setValue(oldSpeed);
                        if (aiDisabled) {
                            entity.setAI(false);
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

    @Override
    public List<Player> getPlayersThatSee(Entity entity) {
        PlayerChunkMap tracker = ((WorldServer) ((CraftEntity) entity).getHandle().world).getChunkProvider().playerChunkMap;
        PlayerChunkMap.EntityTracker entityTracker = tracker.trackedEntities.get(entity.getEntityId());
        ArrayList<Player> output = new ArrayList<>();
        if (entityTracker == null) {
            return output;
        }
        for (EntityPlayer player : entityTracker.trackedPlayers) {
            output.add(player.getBukkitEntity());
        }
        return output;
    }

    /*
        Hide Entity
     */

    @Override
    public void sendHidePacket(Player pl, Entity entity) {
        if (entity instanceof Player) {
            pl.hidePlayer(Denizen.getInstance(), (Player) entity);
            return;
        }
        CraftPlayer craftPlayer = (CraftPlayer) pl;
        EntityPlayer entityPlayer = craftPlayer.getHandle();
        if (entityPlayer.playerConnection != null && !craftPlayer.equals(entity)) {
            PlayerChunkMap tracker = ((WorldServer) craftPlayer.getHandle().world).getChunkProvider().playerChunkMap;
            net.minecraft.server.v1_15_R1.Entity other = ((CraftEntity) entity).getHandle();
            PlayerChunkMap.EntityTracker entry = tracker.trackedEntities.get(other.getId());
            if (entry != null) {
                entry.clear(entityPlayer);
            }
            if (Denizen.supportsPaper) { // Workaround for Paper issue
                entityPlayer.playerConnection.sendPacket(new PacketPlayOutEntityDestroy(other.getId()));
            }
        }
    }

    @Override
    public void sendShowPacket(Player pl, Entity entity) {
        if (entity instanceof Player) {
            pl.showPlayer(Denizen.getInstance(), (Player) entity);
            return;
        }
        CraftPlayer craftPlayer = (CraftPlayer) pl;
        EntityPlayer entityPlayer = craftPlayer.getHandle();
        if (entityPlayer.playerConnection != null && !craftPlayer.equals(entity)) {
            PlayerChunkMap tracker = ((WorldServer) craftPlayer.getHandle().world).getChunkProvider().playerChunkMap;
            net.minecraft.server.v1_15_R1.Entity other = ((CraftEntity) entity).getHandle();
            PlayerChunkMap.EntityTracker entry = tracker.trackedEntities.get(other.getId());
            if (entry != null) {
                entry.clear(entityPlayer);
                entry.updatePlayer(entityPlayer);
            }
        }
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
            net.minecraft.server.v1_15_R1.Entity handle = ((CraftEntity) entity).getHandle();
            handle.yaw = yaw;
            handle.pitch = pitch;
        }
    }

    @Override
    public float getBaseYaw(Entity entity) {
        net.minecraft.server.v1_15_R1.Entity handle = ((CraftEntity) entity).getHandle();
        return ((EntityLiving) handle).aL;
    }

    @Override
    public void look(Entity entity, float yaw, float pitch) {
        net.minecraft.server.v1_15_R1.Entity handle = ((CraftEntity) entity).getHandle();
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
                livingHandle.aL = yaw;
                if (!(handle instanceof EntityHuman)) {
                    livingHandle.aK = yaw;
                }
                livingHandle.aM = yaw;
            }
            handle.pitch = pitch;
        }
    }

    private static MovingObjectPosition rayTrace(World world, Vector start, Vector end) {
        try {
            NMSHandler.getChunkHelper().changeChunkServerThread(world);
            return ((CraftWorld) world).getHandle().rayTrace(new RayTrace(new Vec3D(start.getX(), start.getY(), start.getZ()),
                    new Vec3D(end.getX(), end.getY(), end.getZ()),
                    // TODO: 1.14 - check if these collision options are reasonable (maybe provide the options for this method?)
                    RayTrace.BlockCollisionOption.OUTLINE, RayTrace.FluidCollisionOption.NONE, null));
        }
        finally {
            NMSHandler.getChunkHelper().restoreServerThread(world);
        }
    }

    @Override
    public boolean canTrace(World world, Vector start, Vector end) {
        MovingObjectPosition pos = rayTrace(world, start, end);
        if (pos == null) {
            return true;
        }
        return pos.getType() == MovingObjectPosition.EnumMovingObjectType.MISS;
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
        if (!(l instanceof MovingObjectPositionBlock) || l.getPos() == null) {
            return null;
        }
        Vector finalVec = new Vector(l.getPos().x, l.getPos().y, l.getPos().z);
        MapTraceResult mtr = new MapTraceResult();
        switch (((MovingObjectPositionBlock) l).getDirection()) {
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
    public void snapPositionTo(Entity entity, Vector vector) {
        ((CraftEntity) entity).getHandle().setPositionRaw(vector.getX(), vector.getY(), vector.getZ());
    }

    @Override
    public void move(Entity entity, Vector vector) {
        ((CraftEntity) entity).getHandle().move(EnumMoveType.SELF, new Vec3D(vector.getX(), vector.getY(), vector.getZ()));
    }

    @Override
    public void teleport(Entity entity, Location vector) {
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

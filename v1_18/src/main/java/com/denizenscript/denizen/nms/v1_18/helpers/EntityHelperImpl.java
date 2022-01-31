package com.denizenscript.denizen.nms.v1_18.helpers;

import com.denizenscript.denizen.Denizen;
import com.denizenscript.denizen.nms.NMSHandler;
import com.denizenscript.denizen.nms.v1_18.ReflectionMappingsInfo;
import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizencore.utilities.ReflectionHelper;
import com.denizenscript.denizen.nms.v1_18.impl.jnbt.CompoundTagImpl;
import com.denizenscript.denizen.nms.interfaces.EntityHelper;
import com.denizenscript.denizen.nms.util.BoundingBox;
import com.denizenscript.denizen.nms.util.jnbt.CompoundTag;
import com.denizenscript.denizen.utilities.Utilities;
import com.denizenscript.denizen.utilities.debugging.Debug;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.game.ClientboundRemoveEntitiesPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.network.ServerPlayerConnection;
import net.minecraft.stats.RecipeBook;
import net.minecraft.stats.ServerRecipeBook;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.CombatRules;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.block.BlockFace;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.block.data.BlockData;
import org.bukkit.craftbukkit.v1_18_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_18_R1.block.CraftBlock;
import org.bukkit.craftbukkit.v1_18_R1.block.CraftCreatureSpawner;
import org.bukkit.craftbukkit.v1_18_R1.block.data.CraftBlockData;
import org.bukkit.craftbukkit.v1_18_R1.entity.*;
import org.bukkit.craftbukkit.v1_18_R1.event.CraftEventFactory;
import org.bukkit.craftbukkit.v1_18_R1.inventory.CraftItemStack;
import org.bukkit.entity.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.EntityDamageEvent;
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

    public static final Field RECIPE_BOOK_DISCOVERED_SET = ReflectionHelper.getFields(RecipeBook.class).get(ReflectionMappingsInfo.RecipeBook_known);

    public static final MethodHandle ENTITY_ONGROUND_SETTER = ReflectionHelper.getFinalSetter(net.minecraft.world.entity.Entity.class, ReflectionMappingsInfo.Entity_onGround);

    public static final EntityDataAccessor<Boolean> ENTITY_ENDERMAN_DATAWATCHER_SCREAMING = ReflectionHelper.getFieldValue(EnderMan.class, ReflectionMappingsInfo.EnderMan_DATA_CREEPY, null);

    @Override
    public void setInvisible(Entity entity, boolean invisible) {
        ((CraftEntity) entity).getHandle().setInvisible(invisible);
    }

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
        Pose pose = sneak ? Pose.CROUCHING : Pose.STANDING;
        ((CraftEntity) player).getHandle().setPose(pose);
    }

    @Override
    public void setSleeping(Entity player, boolean sleep) {
        Pose pose = sleep ? Pose.SLEEPING : Pose.STANDING;
        ((CraftEntity) player).getHandle().setPose(pose);
    }

    @Override
    public double getDamageTo(LivingEntity attacker, Entity target) {
        MobType monsterType;
        if (target instanceof LivingEntity) {
            monsterType = ((CraftLivingEntity) target).getHandle().getMobType();
        }
        else {
            monsterType = MobType.UNDEFINED;
        }
        double damage = 0;
        AttributeInstance attrib = attacker.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE);
        if (attrib != null) {
            damage = attrib.getValue();
        }
        if (attacker.getEquipment() != null && attacker.getEquipment().getItemInMainHand() != null) {
            damage += EnchantmentHelper.getDamageBonus(CraftItemStack.asNMSCopy(attacker.getEquipment().getItemInMainHand()), monsterType);
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
            net.minecraft.world.entity.Entity nmsTarget = ((CraftEntity) target).getHandle();
            if (nmsTarget.isInvulnerableTo(source)) {
                return 0;
            }
            if (!(nmsTarget instanceof net.minecraft.world.entity.LivingEntity)) {
                return damage;
            }
            net.minecraft.world.entity.LivingEntity livingTarget = (net.minecraft.world.entity.LivingEntity) nmsTarget;
            damage = CombatRules.getDamageAfterAbsorb((float) damage, (float) livingTarget.getArmorValue(), (float) livingTarget.getAttributeValue(Attributes.ARMOR_TOUGHNESS));
            int enchantDamageModifier = EnchantmentHelper.getDamageProtection(livingTarget.getArmorSlots(), source);
            if (enchantDamageModifier > 0) {
                damage = CombatRules.getDamageAfterMagicAbsorb((float) damage, (float) enchantDamageModifier);
            }
        }
        return damage;
    }

    @Override
    public String getRawHoverText(Entity entity) {
        throw new UnsupportedOperationException();
    }

    public List<String> getDiscoveredRecipes(Player player) {
        try {
            ServerRecipeBook book = ((CraftPlayer) player).getHandle().getRecipeBook();
            Set<ResourceLocation> set = (Set<ResourceLocation>) RECIPE_BOOK_DISCOVERED_SET.get(book);
            List<String> output = new ArrayList<>();
            for (ResourceLocation key : set) {
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
        ((CraftLivingEntity) entity).getHandle().startAutoSpinAttack(state ? 0 : 1);
    }

    @Override
    public Entity getFishHook(PlayerFishEvent event) {
        return event.getHook();
    }

    @Override
    public ItemStack getItemFromTrident(Entity entity) {
        return CraftItemStack.asBukkitCopy(((CraftTrident) entity).getHandle().tridentItem);
    }

    @Override
    public void setItemForTrident(Entity entity, ItemStack item) {
        ((CraftTrident) entity).getHandle().tridentItem = CraftItemStack.asNMSCopy(item);
    }

    @Override
    public void forceInteraction(Player player, Location location) {
        CraftPlayer craftPlayer = (CraftPlayer) player;
        BlockPos pos = new BlockPos(location.getBlockX(), location.getBlockY(), location.getBlockZ());
        ((CraftBlock) location.getBlock()).getNMS().use(((CraftWorld) location.getWorld()).getHandle(),
                craftPlayer != null ? craftPlayer.getHandle() : null, InteractionHand.MAIN_HAND,
                new BlockHitResult(new Vec3(0, 0, 0), null, pos, false));
    }

    @Override
    public Entity getEntity(World world, UUID uuid) {
        net.minecraft.world.entity.Entity entity = ((CraftWorld) world).getHandle().getEntity(uuid);
        return entity == null ? null : entity.getBukkitEntity();
    }

    @Override
    public void setTarget(Creature entity, LivingEntity target) {
        net.minecraft.world.entity.LivingEntity nmsTarget = target != null ? ((CraftLivingEntity) target).getHandle() : null;
        ((CraftCreature) entity).getHandle().setTarget(nmsTarget, EntityTargetEvent.TargetReason.CUSTOM, true);
        entity.setTarget(target);
    }

    @Override
    public CompoundTag getNbtData(Entity entity) {
        net.minecraft.nbt.CompoundTag compound = new net.minecraft.nbt.CompoundTag();
        ((CraftEntity) entity).getHandle().saveAsPassenger(compound);
        return CompoundTagImpl.fromNMSTag(compound);
    }

    @Override
    public void setNbtData(Entity entity, CompoundTag compoundTag) {
        ((CraftEntity) entity).getHandle().load(((CompoundTagImpl) compoundTag).toNMSTag());
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
        net.minecraft.world.entity.Entity nmsEntity = ((CraftEntity) entity).getHandle();
        if (!(nmsEntity instanceof Mob)) {
            return;
        }
        ((Mob) nmsEntity).getNavigation().stop();
    }

    @Override
    public double getSpeed(Entity entity) {
        net.minecraft.world.entity.Entity nmsEntityEntity = ((CraftEntity) entity).getHandle();
        if (!(nmsEntityEntity instanceof Mob)) {
            return 0.0;
        }
        Mob nmsEntity = (Mob) nmsEntityEntity;
        return nmsEntity.getAttribute(Attributes.MOVEMENT_SPEED).getBaseValue();
    }

    @Override
    public void setSpeed(Entity entity, double speed) {
        net.minecraft.world.entity.Entity nmsEntityEntity = ((CraftEntity) entity).getHandle();
        if (!(nmsEntityEntity instanceof Mob)) {
            return;
        }
        Mob nmsEntity = (Mob) nmsEntityEntity;
        nmsEntity.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(speed);
    }

    @Override
    public void follow(final Entity target, final Entity follower, final double speed, final double lead,
                       final double maxRange, final boolean allowWander, final boolean teleport) {
        if (target == null || follower == null) {
            return;
        }

        final net.minecraft.world.entity.Entity nmsEntityFollower = ((CraftEntity) follower).getHandle();
        if (!(nmsEntityFollower instanceof Mob)) {
            return;
        }
        final Mob nmsFollower = (Mob) nmsEntityFollower;
        final PathNavigation followerNavigation = nmsFollower.getNavigation();

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
                followerNavigation.setSpeedModifier(2D);
                Location targetLocation = target.getLocation();
                Path path;

                if (hasMax && !Utilities.checkLocation(targetLocation, follower.getLocation(), maxRange)
                        && !target.isDead() && target.isOnGround()) {
                    if (!inRadius) {
                        if (teleport) {
                            follower.teleport(Utilities.getWalkableLocationNear(targetLocation, locationNearInt));
                        }
                        else {
                            cancel();
                        }
                    }
                    else {
                        inRadius = false;
                        path = followerNavigation.createPath(targetLocation.getX(), targetLocation.getY(), targetLocation.getZ(), 0);
                        if (path != null) {
                            followerNavigation.moveTo(path, 1D);
                            followerNavigation.setSpeedModifier(2D);
                        }
                    }
                }
                else if (!inRadius && !Utilities.checkLocation(targetLocation, follower.getLocation(), lead)) {
                    path = followerNavigation.createPath(targetLocation.getX(), targetLocation.getY(), targetLocation.getZ(), 0);
                    if (path != null) {
                        followerNavigation.moveTo(path, 1D);
                        followerNavigation.setSpeedModifier(2D);
                    }
                }
                else {
                    inRadius = true;
                }
                if (inRadius && !allowWander) {
                    followerNavigation.stop();
                }
                nmsFollower.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(speed);
            }
        }.runTaskTimer(NMSHandler.getJavaPlugin(), 0, 10));
    }

    @Override
    public void walkTo(final LivingEntity entity, Location location, Double speed, final Runnable callback) {
        if (entity == null || location == null) {
            return;
        }
        net.minecraft.world.entity.Entity nmsEntityEntity = ((CraftEntity) entity).getHandle();
        if (!(nmsEntityEntity instanceof Mob)) {
            return;
        }
        final Mob nmsEntity = (Mob) nmsEntityEntity;
        final PathNavigation entityNavigation = nmsEntity.getNavigation();
        final Path path;
        final boolean aiDisabled = !entity.hasAI();
        if (aiDisabled) {
            entity.setAI(true);
            try {
                ENTITY_ONGROUND_SETTER.invoke(nmsEntity, true);
            }
            catch (Throwable ex) {
                Debug.echoError(ex);
            }
        }
        path = entityNavigation.createPath(location.getX(), location.getY(), location.getZ(), 1);
        if (path != null) {
            nmsEntity.goalSelector.enableControlFlag(Goal.Flag.MOVE);
            entityNavigation.moveTo(path, 1D);
            entityNavigation.setSpeedModifier(2D);
            final double oldSpeed = nmsEntity.getAttribute(Attributes.MOVEMENT_SPEED).getBaseValue();
            if (speed != null) {
                nmsEntity.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(speed);
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
                    if (entityNavigation.isDone() || path.isDone()) {
                        if (callback != null) {
                            callback.run();
                        }
                        if (speed != null) {
                            nmsEntity.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(oldSpeed);
                        }
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
        ChunkMap tracker = ((ServerLevel) ((CraftEntity) entity).getHandle().level).getChunkSource().chunkMap;
        ChunkMap.TrackedEntity entityTracker = tracker.entityMap.get(entity.getEntityId());
        ArrayList<Player> output = new ArrayList<>();
        if (entityTracker == null) {
            return output;
        }
        for (ServerPlayerConnection player : entityTracker.seenBy) {
            output.add(player.getPlayer().getBukkitEntity());
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
        ServerPlayer entityPlayer = craftPlayer.getHandle();
        if (entityPlayer.connection != null && !craftPlayer.equals(entity)) {
            ChunkMap tracker = ((ServerLevel) craftPlayer.getHandle().level).getChunkSource().chunkMap;
            net.minecraft.world.entity.Entity other = ((CraftEntity) entity).getHandle();
            ChunkMap.TrackedEntity entry = tracker.entityMap.get(other.getId());
            if (entry != null) {
                entry.removePlayer(entityPlayer);
            }
            if (Denizen.supportsPaper) { // Workaround for Paper issue
                entityPlayer.connection.send(new ClientboundRemoveEntitiesPacket(other.getId()));
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
        ServerPlayer entityPlayer = craftPlayer.getHandle();
        if (entityPlayer.connection != null && !craftPlayer.equals(entity)) {
            ChunkMap tracker = ((ServerLevel) craftPlayer.getHandle().level).getChunkSource().chunkMap;
            net.minecraft.world.entity.Entity other = ((CraftEntity) entity).getHandle();
            ChunkMap.TrackedEntity entry = tracker.entityMap.get(other.getId());
            if (entry != null) {
                entry.removePlayer(entityPlayer);
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
            teleport(entity, location);
        }
        else if (entity instanceof LivingEntity) {
            if (entity instanceof EnderDragon) {
                yaw = normalizeYaw(yaw - 180);
            }
            look(entity, yaw, pitch);
        }
        else {
            net.minecraft.world.entity.Entity handle = ((CraftEntity) entity).getHandle();
            handle.setYRot(yaw - 360);
            handle.setXRot(pitch);
        }
    }

    @Override
    public float getBaseYaw(Entity entity) {
        net.minecraft.world.entity.Entity handle = ((CraftEntity) entity).getHandle();
        return ((net.minecraft.world.entity.LivingEntity) handle).yBodyRot;
    }

    @Override
    public void look(Entity entity, float yaw, float pitch) {
        net.minecraft.world.entity.Entity handle = ((CraftEntity) entity).getHandle();
        if (handle != null) {
            handle.setYRot(yaw);
            if (handle instanceof net.minecraft.world.entity.LivingEntity) {
                net.minecraft.world.entity.LivingEntity livingHandle = (net.minecraft.world.entity.LivingEntity) handle;
                while (yaw < -180.0F) {
                    yaw += 360.0F;
                }
                while (yaw >= 180.0F) {
                    yaw -= 360.0F;
                }
                livingHandle.yBodyRotO = yaw;
                if (!(handle instanceof net.minecraft.world.entity.player.Player)) {
                    livingHandle.setYBodyRot(yaw);
                }
                livingHandle.setYHeadRot(yaw);
            }
            handle.setXRot(pitch);
        }
        else {
            Debug.echoError("Cannot set look direction for unspawned entity " + entity.getUniqueId());
        }
    }

    private static HitResult rayTrace(World world, Vector start, Vector end) {
        try {
            NMSHandler.getChunkHelper().changeChunkServerThread(world);
            return ((CraftWorld) world).getHandle().clip(new ClipContext(new Vec3(start.getX(), start.getY(), start.getZ()),
                    new Vec3(end.getX(), end.getY(), end.getZ()),
                    ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, null));
        }
        finally {
            NMSHandler.getChunkHelper().restoreServerThread(world);
        }
    }

    @Override
    public boolean canTrace(World world, Vector start, Vector end) {
        HitResult pos = rayTrace(world, start, end);
        if (pos == null) {
            return true;
        }
        return pos.getType() == HitResult.Type.MISS;
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
        HitResult l = rayTrace(start.getWorld(), startVec, endVec);
        if (!(l instanceof BlockHitResult) || l.getLocation() == null) {
            return null;
        }
        Vector finalVec = new Vector(l.getLocation().x, l.getLocation().y, l.getLocation().z);
        MapTraceResult mtr = new MapTraceResult();
        switch (((BlockHitResult) l).getDirection()) {
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
        ((CraftEntity) entity).getHandle().setPosRaw(vector.getX(), vector.getY(), vector.getZ());
    }

    @Override
    public void move(Entity entity, Vector vector) {
        ((CraftEntity) entity).getHandle().move(MoverType.SELF, new Vec3(vector.getX(), vector.getY(), vector.getZ()));
    }

    @Override
    public void teleport(Entity entity, Location loc) {
        net.minecraft.world.entity.Entity nmsEntity = ((CraftEntity) entity).getHandle();
        nmsEntity.setYRot(loc.getYaw());
        nmsEntity.setXRot(loc.getPitch());
        if (nmsEntity instanceof ServerPlayer) {
            nmsEntity.teleportTo(loc.getX(), loc.getY(), loc.getZ());
        }
        nmsEntity.setPos(loc.getX(), loc.getY(), loc.getZ());
    }

    @Override
    public BoundingBox getBoundingBox(Entity entity) {
        AABB boundingBox = ((CraftEntity) entity).getHandle().getBoundingBox();
        Vector position = new Vector(boundingBox.minX, boundingBox.minY, boundingBox.minZ);
        Vector size = new Vector(boundingBox.maxX, boundingBox.maxY, boundingBox.maxZ);
        return new BoundingBox(position, size);
    }

    @Override
    public void setBoundingBox(Entity entity, BoundingBox boundingBox) {
        Vector low = boundingBox.getLow();
        Vector high = boundingBox.getHigh();
        ((CraftEntity) entity).getHandle().setBoundingBox(new AABB(low.getX(), low.getY(), low.getZ(), high.getX(), high.getY(), high.getZ()));
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

    @Override
    public void setTicksLived(Entity entity, int ticks) {
        // Bypass Spigot's must-be-at-least-1-tick requirement, as negative tick counts are useful
        ((CraftEntity) entity).getHandle().tickCount = ticks;
        if (entity instanceof CraftFallingBlock) {
            ((CraftFallingBlock) entity).getHandle().time = ticks;
        }
        else if (entity instanceof CraftItem) {
            ((ItemEntity) ((CraftItem) entity).getHandle()).age = ticks;
        }
    }

    @Override
    public int getShulkerPeek(Entity entity) {
        return ((CraftShulker) entity).getHandle().getRawPeekAmount();
    }

    @Override
    public void setShulkerPeek(Entity entity, int peek) {
        ((CraftShulker) entity).getHandle().setRawPeekAmount(peek);
    }

    @Override
    public void setHeadAngle(Entity entity, float angle) {
        net.minecraft.world.entity.LivingEntity handle = ((CraftLivingEntity) entity).getHandle();
        handle.yHeadRot = angle;
        handle.setYHeadRot(angle);
    }

    @Override
    public void setGhastAttacking(Entity entity, boolean attacking) {
        ((CraftGhast) entity).getHandle().setCharging(attacking);
    }

    @Override
    public void setEndermanAngry(Entity entity, boolean angry) {
        ((CraftEnderman) entity).getHandle().getEntityData().set(ENTITY_ENDERMAN_DATAWATCHER_SCREAMING, angry);
    }

    public static class FakeDamageSrc extends DamageSource { public DamageSource real; public FakeDamageSrc(DamageSource src) { super("fake"); real = src; } }

    public static DamageSource getSourceFor(net.minecraft.world.entity.Entity nmsSource, EntityDamageEvent.DamageCause cause) {
        DamageSource src = DamageSource.GENERIC;
        if (nmsSource != null) {
            if (nmsSource instanceof net.minecraft.world.entity.player.Player) {
                src = DamageSource.playerAttack((net.minecraft.world.entity.player.Player) nmsSource);
            }
            else if (nmsSource instanceof net.minecraft.world.entity.LivingEntity) {
                src = DamageSource.mobAttack((net.minecraft.world.entity.LivingEntity) nmsSource);
            }
        }
        if (cause == null) {
            return src;
        }
        switch (cause) {
            case CONTACT:
                return DamageSource.CACTUS;
            case ENTITY_ATTACK:
                return DamageSource.mobAttack(nmsSource instanceof net.minecraft.world.entity.LivingEntity ? (net.minecraft.world.entity.LivingEntity) nmsSource : null);
            case ENTITY_SWEEP_ATTACK:
                if (src != DamageSource.GENERIC) {
                    src.sweep();
                }
                return src;
            case PROJECTILE:
                return DamageSource.thrown(nmsSource, nmsSource.getBukkitEntity() instanceof Projectile
                        && ((Projectile) nmsSource.getBukkitEntity()).getShooter() instanceof Entity ? ((CraftEntity) ((Projectile) nmsSource.getBukkitEntity()).getShooter()).getHandle() : null);
            case SUFFOCATION:
                return DamageSource.IN_WALL;
            case FALL:
                return DamageSource.FALL;
            case FIRE:
                return DamageSource.IN_FIRE;
            case FIRE_TICK:
                return DamageSource.ON_FIRE;
            case MELTING:
                return CraftEventFactory.MELTING;
            case LAVA:
                return DamageSource.LAVA;
            case DROWNING:
                return DamageSource.DROWN;
            case BLOCK_EXPLOSION:
                return DamageSource.explosion(nmsSource instanceof TNTPrimed && ((TNTPrimed) nmsSource).getSource() instanceof net.minecraft.world.entity.LivingEntity ? (net.minecraft.world.entity.LivingEntity) ((TNTPrimed) nmsSource).getSource() : null);
            case ENTITY_EXPLOSION:
                return DamageSource.explosion(nmsSource instanceof net.minecraft.world.entity.LivingEntity ? (net.minecraft.world.entity.LivingEntity) nmsSource : null);
            case VOID:
                return DamageSource.OUT_OF_WORLD;
            case LIGHTNING:
                return DamageSource.LIGHTNING_BOLT;
            case STARVATION:
                return DamageSource.STARVE;
            case POISON:
                return CraftEventFactory.POISON;
            case MAGIC:
                return DamageSource.MAGIC;
            case WITHER:
                return DamageSource.WITHER;
            case FALLING_BLOCK:
                return DamageSource.FALLING_BLOCK;
            case THORNS:
                return DamageSource.thorns(nmsSource);
            case DRAGON_BREATH:
                return DamageSource.DRAGON_BREATH;
            case CUSTOM:
                return DamageSource.GENERIC;
            case FLY_INTO_WALL:
                return DamageSource.FLY_INTO_WALL;
            case HOT_FLOOR:
                return DamageSource.HOT_FLOOR;
            case CRAMMING:
                return DamageSource.CRAMMING;
            case DRYOUT:
                return DamageSource.DRY_OUT;
            //case SUICIDE:
            default:
                return new FakeDamageSrc(src);
        }
    }

    @Override
    public void damage(LivingEntity target, float amount, Entity source, EntityDamageEvent.DamageCause cause) {
        if (target == null) {
            return;
        }
        net.minecraft.world.entity.LivingEntity nmsTarget = ((CraftLivingEntity) target).getHandle();
        net.minecraft.world.entity.Entity nmsSource = source == null ? null : ((CraftEntity) source).getHandle();
        CraftEventFactory.entityDamage = nmsSource;
        try {
            DamageSource src = getSourceFor(nmsSource, cause);
            if (src instanceof FakeDamageSrc) {
                src = ((FakeDamageSrc) src).real;
                EntityDamageEvent ede = fireFakeDamageEvent(target, source, cause, amount);
                if (ede.isCancelled()) {
                    return;
                }
            }
            nmsTarget.hurt(src, amount);
        }
        finally {
            CraftEventFactory.entityDamage = null;
        }
    }

    @Override
    public void setLastHurtBy(LivingEntity mob, LivingEntity damager) {
        ((CraftLivingEntity) mob).getHandle().setLastHurtByMob(((CraftLivingEntity) damager).getHandle());
    }

    public static final MethodHandle FALLINGBLOCK_TYPE_SETTER = ReflectionHelper.getFinalSetterForFirstOfType(net.minecraft.world.entity.item.FallingBlockEntity.class, BlockState.class);

    @Override
    public void setFallingBlockType(FallingBlock entity, BlockData block) {
        BlockState state = ((CraftBlockData) block).getState();
        FallingBlockEntity nmsEntity = ((CraftFallingBlock) entity).getHandle();
        try {
            FALLINGBLOCK_TYPE_SETTER.invoke(nmsEntity, state);
        }
        catch (Throwable ex) {
            Debug.echoError(ex);
        }
    }

    @Override
    public EntityTag getMobSpawnerDisplayEntity(CreatureSpawner spawner) {
        SpawnerBlockEntity nmsSpawner = BlockHelperImpl.getTE((CraftCreatureSpawner) spawner);
        net.minecraft.world.entity.Entity nmsEntity = nmsSpawner.getSpawner().getOrCreateDisplayEntity(((CraftWorld) spawner.getWorld()).getHandle());
        return new EntityTag(nmsEntity.getBukkitEntity());
    }

    @Override
    public void setFireworkLifetime(Firework firework, int ticks) {
        ((CraftFirework) firework).getHandle().lifetime = ticks;
    }

    @Override
    public int getFireworkLifetime(Firework firework) {
        return ((CraftFirework) firework).getHandle().lifetime;
    }

    public static final Field ZOMBIE_INWATERTIME = ReflectionHelper.getFields(net.minecraft.world.entity.monster.Zombie.class).get(ReflectionMappingsInfo.Zombie_inWaterTime);

    @Override
    public int getInWaterTime(Zombie zombie) {
        try {
            return ZOMBIE_INWATERTIME.getInt(((CraftZombie) zombie).getHandle());
        }
        catch (Throwable ex) {
            Debug.echoError(ex);
            return 0;
        }
    }

    @Override
    public void setInWaterTime(Zombie zombie, int ticks) {
        try {
            ZOMBIE_INWATERTIME.setInt(((CraftZombie) zombie).getHandle(), ticks);
        }
        catch (Throwable ex) {
            Debug.echoError(ex);
        }
    }
}

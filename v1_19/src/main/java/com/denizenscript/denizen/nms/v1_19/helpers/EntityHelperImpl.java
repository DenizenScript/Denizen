package com.denizenscript.denizen.nms.v1_19.helpers;

import com.denizenscript.denizen.Denizen;
import com.denizenscript.denizen.nms.NMSHandler;
import com.denizenscript.denizen.nms.interfaces.EntityHelper;
import com.denizenscript.denizen.nms.util.jnbt.CompoundTag;
import com.denizenscript.denizen.nms.v1_19.ReflectionMappingsInfo;
import com.denizenscript.denizen.nms.v1_19.impl.jnbt.CompoundTagImpl;
import com.denizenscript.denizen.nms.v1_19.impl.network.handlers.DenizenNetworkManagerImpl;
import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizen.utilities.Utilities;
import com.denizenscript.denizen.utilities.packets.NetworkInterceptHelper;
import com.denizenscript.denizencore.utilities.ReflectionHelper;
import com.denizenscript.denizencore.utilities.debugging.Debug;
import io.netty.buffer.Unpooled;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.game.ClientboundMoveEntityPacket;
import net.minecraft.network.protocol.game.ClientboundPlayerLookAtPacket;
import net.minecraft.network.protocol.game.ClientboundRemoveEntitiesPacket;
import net.minecraft.network.protocol.game.ClientboundTeleportEntityPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.server.dedicated.DedicatedPlayerList;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.server.level.ServerEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerPlayerConnection;
import net.minecraft.server.players.PlayerList;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.CombatRules;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageSources;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
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
import org.bukkit.block.CreatureSpawner;
import org.bukkit.block.data.BlockData;
import org.bukkit.craftbukkit.v1_19_R3.CraftServer;
import org.bukkit.craftbukkit.v1_19_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_19_R3.block.CraftBlock;
import org.bukkit.craftbukkit.v1_19_R3.block.CraftCreatureSpawner;
import org.bukkit.craftbukkit.v1_19_R3.block.data.CraftBlockData;
import org.bukkit.craftbukkit.v1_19_R3.entity.*;
import org.bukkit.craftbukkit.v1_19_R3.event.CraftEventFactory;
import org.bukkit.craftbukkit.v1_19_R3.inventory.CraftItemStack;
import org.bukkit.entity.*;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;

import java.lang.invoke.MethodHandle;
import java.lang.reflect.Field;
import java.util.*;

public class EntityHelperImpl extends EntityHelper {

    public static final MethodHandle ENTITY_ONGROUND_SETTER = ReflectionHelper.getFinalSetter(net.minecraft.world.entity.Entity.class, ReflectionMappingsInfo.Entity_onGround, boolean.class);

    public static final EntityDataAccessor<Boolean> ENTITY_ENDERMAN_DATAWATCHER_SCREAMING = ReflectionHelper.getFieldValue(EnderMan.class, ReflectionMappingsInfo.EnderMan_DATA_CREEPY, null);

    @Override
    public void setInvisible(Entity entity, boolean invisible) {
        ((CraftEntity) entity).getHandle().setInvisible(invisible);
    }

    @Override
    public boolean isInvisible(Entity entity) {
        return ((CraftEntity) entity).getHandle().isInvisible();
    }

    @Override
    public void setPose(Entity entity, Pose pose) {
        ((CraftEntity) entity).getHandle().setPose(net.minecraft.world.entity.Pose.values()[pose.ordinal()]);
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
            net.minecraft.world.entity.Entity nmsTarget = ((CraftEntity) target).getHandle();
            if (attacker instanceof Player) {
                source = nmsTarget.level.damageSources().playerAttack(((CraftPlayer) attacker).getHandle());
            }
            else {
                source = nmsTarget.level.damageSources().mobAttack(((CraftLivingEntity) attacker).getHandle());
            }
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

    public static final MethodHandle LIVINGENTITY_AUTOSPINATTACK_SETTER = ReflectionHelper.getFinalSetter(net.minecraft.world.entity.LivingEntity.class, ReflectionMappingsInfo.LivingEntity_autoSpinAttackTicks);
    public static final MethodHandle LIVINGENTITY_SETLIVINGENTITYFLAG = ReflectionHelper.getMethodHandle(net.minecraft.world.entity.LivingEntity.class, ReflectionMappingsInfo.LivingEntity_setLivingEntityFlag_method, int.class, boolean.class);

    @Override
    public void setRiptide(Entity entity, boolean state) {
        try {
            net.minecraft.world.entity.LivingEntity nmsEntity = ((CraftLivingEntity) entity).getHandle();
            LIVINGENTITY_AUTOSPINATTACK_SETTER.invoke(nmsEntity, state ? 0 : 1);
            LIVINGENTITY_SETLIVINGENTITYFLAG.invoke(nmsEntity, 4, true);
        }
        catch (Throwable ex) {
            Debug.echoError(ex);
        }
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
        net.minecraft.world.entity.Entity nmsEntity = ((CraftEntity) entity).getHandle();
        if (!(nmsEntity instanceof final Mob nmsMob)) {
            return;
        }
        final PathNavigation entityNavigation = nmsMob.getNavigation();
        final Path path;
        final boolean aiDisabled = !entity.hasAI();
        if (aiDisabled) {
            entity.setAI(true);
            try {
                ENTITY_ONGROUND_SETTER.invoke(nmsMob, true);
            }
            catch (Throwable ex) {
                Debug.echoError(ex);
            }
        }
        path = entityNavigation.createPath(location.getX(), location.getY(), location.getZ(), 1);
        if (path != null) {
            nmsMob.goalSelector.enableControlFlag(Goal.Flag.MOVE);
            entityNavigation.moveTo(path, 1D);
            final double oldSpeed = nmsMob.getAttribute(Attributes.MOVEMENT_SPEED).getBaseValue();
            if (speed != null) {
                nmsMob.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(speed);
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
                    if (aiDisabled && entity instanceof Wolf wolf) {
                        wolf.setAngry(false);
                    }
                    if (entityNavigation.isDone() || path.isDone()) {
                        if (callback != null) {
                            callback.run();
                        }
                        if (speed != null) {
                            nmsMob.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(oldSpeed);
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

    @Override
    public void sendAllUpdatePackets(Entity entity) {
        ChunkMap tracker = ((ServerLevel) ((CraftEntity) entity).getHandle().level).getChunkSource().chunkMap;
        ChunkMap.TrackedEntity entityTracker = tracker.entityMap.get(entity.getEntityId());
        if (entityTracker == null) {
            return;
        }
        try {
            ServerEntity serverEntity = (ServerEntity) PacketHelperImpl.ENTITY_TRACKER_ENTRY_GETTER.get(entityTracker);
            serverEntity.sendChanges();
        }
        catch (Throwable ex) {
            Debug.echoError(ex);
        }
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
            NetworkInterceptHelper.enable();
            float relYaw = (yaw - entity.getLocation().getYaw()) % 360;
            if (relYaw > 180) {
                relYaw -= 360;
            }
            final float actualRelYaw = relYaw;
            float relPitch = pitch - entity.getLocation().getPitch();
            NMSHandler.packetHelper.sendRelativeLookPacket((Player) entity, actualRelYaw, relPitch);
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
    public float getBaseYaw(LivingEntity entity) {
        return ((CraftLivingEntity) entity).getHandle().yBodyRot;
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
            NMSHandler.chunkHelper.changeChunkServerThread(world);
            return ((CraftWorld) world).getHandle().clip(new ClipContext(new Vec3(start.getX(), start.getY(), start.getZ()),
                    new Vec3(end.getX(), end.getY(), end.getZ()),
                    ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, null));
        }
        finally {
            NMSHandler.chunkHelper.restoreServerThread(world);
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
    public void snapPositionTo(Entity entity, Vector vector) {
        ((CraftEntity) entity).getHandle().setPosRaw(vector.getX(), vector.getY(), vector.getZ());
    }

    @Override
    public void move(Entity entity, Vector vector) {
        ((CraftEntity) entity).getHandle().move(MoverType.SELF, new Vec3(vector.getX(), vector.getY(), vector.getZ()));
    }

    @Override
    public boolean internalLook(Player player, Location at) {
        ClientboundPlayerLookAtPacket packet = new ClientboundPlayerLookAtPacket(EntityAnchorArgument.Anchor.EYES, at.getX(), at.getY(), at.getZ());
        PacketHelperImpl.send(player, packet);
        return true;
    }

    public static long entityToPacket(double x) {
        return Mth.lfloor(x * 4096.0D);
    }

    @Override
    public void fakeMove(Entity entity, Vector vector) {
        long x = entityToPacket(vector.getX());
        long y = entityToPacket(vector.getY());
        long z = entityToPacket(vector.getZ());
        ClientboundMoveEntityPacket packet = new ClientboundMoveEntityPacket.Pos(entity.getEntityId(), (short) x, (short) y, (short) z, entity.isOnGround());
        for (Player player : getPlayersThatSee(entity)) {
            PacketHelperImpl.send(player, packet);
        }
    }

    @Override
    public void fakeTeleport(Entity entity, Location location) {
        FriendlyByteBuf packetData = new FriendlyByteBuf(Unpooled.buffer());
        // Referenced from ClientboundTeleportEntityPacket source
        packetData.writeVarInt(entity.getEntityId());
        packetData.writeDouble(location.getX());
        packetData.writeDouble(location.getY());
        packetData.writeDouble(location.getZ());
        packetData.writeByte((byte)((int)(location.getYaw() * 256.0F / 360.0F)));
        packetData.writeByte((byte)((int)(location.getPitch() * 256.0F / 360.0F)));
        packetData.writeBoolean(entity.isOnGround());
        ClientboundTeleportEntityPacket packet = new ClientboundTeleportEntityPacket(packetData);
        for (Player player : getPlayersThatSee(entity)) {
            PacketHelperImpl.send(player, packet);
        }
    }

    @Override
    public void clientResetLoc(Entity entity) {
        ClientboundTeleportEntityPacket packet = new ClientboundTeleportEntityPacket(((CraftEntity) entity).getHandle());
        for (Player player : getPlayersThatSee(entity)) {
            PacketHelperImpl.send(player, packet);
        }
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
    public void setBoundingBox(Entity entity, BoundingBox box) {
        ((CraftEntity) entity).getHandle().setBoundingBox(new AABB(box.getMinX(), box.getMinY(), box.getMinZ(), box.getMaxX(), box.getMaxY(), box.getMaxZ()));
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
    public void setHeadAngle(LivingEntity entity, float angle) {
        net.minecraft.world.entity.LivingEntity handle = ((CraftLivingEntity) entity).getHandle();
        handle.yHeadRot = angle;
        handle.setYHeadRot(angle);
    }

    @Override
    public void setEndermanAngry(Enderman enderman, boolean angry) {
        ((CraftEnderman) enderman).getHandle().getEntityData().set(ENTITY_ENDERMAN_DATAWATCHER_SCREAMING, angry);
    }

    public static class FakeDamageSrc extends DamageSource { public DamageSource real; public FakeDamageSrc(DamageSource src) { super(null); real = src; } }

    public static DamageSources backupDamageSources;

    public static DamageSources getReusableDamageSources() {
        if (backupDamageSources == null) {
            backupDamageSources = ((CraftWorld) Bukkit.getWorlds().get(0)).getHandle().damageSources();
        }
        return backupDamageSources;
    }

    public static DamageSource getSourceFor(net.minecraft.world.entity.Entity nmsSource, EntityDamageEvent.DamageCause cause, net.minecraft.world.entity.Entity nmsSourceProvider) {
        DamageSources sources = nmsSourceProvider == null ? getReusableDamageSources() : nmsSourceProvider.level.damageSources();
        DamageSource src = sources.generic();
        if (nmsSource != null) {
            if (nmsSource instanceof net.minecraft.world.entity.player.Player) {
                src = nmsSource.level.damageSources().playerAttack((net.minecraft.world.entity.player.Player) nmsSource);
            }
            else if (nmsSource instanceof net.minecraft.world.entity.LivingEntity) {
                src = nmsSource.level.damageSources().mobAttack((net.minecraft.world.entity.LivingEntity) nmsSource);
            }
        }
        if (cause == null) {
            return src;
        }
        switch (cause) {
            case CONTACT:
                return sources.cactus();
            case ENTITY_ATTACK:
                return sources.mobAttack(nmsSource instanceof net.minecraft.world.entity.LivingEntity ? (net.minecraft.world.entity.LivingEntity) nmsSource : null);
            case ENTITY_SWEEP_ATTACK:
                if (src != sources.generic()) {
                    src.sweep();
                }
                return src;
            case PROJECTILE:
                return sources.thrown(nmsSource, nmsSource != null && nmsSource.getBukkitEntity() instanceof Projectile
                        && ((Projectile) nmsSource.getBukkitEntity()).getShooter() instanceof Entity ? ((CraftEntity) ((Projectile) nmsSource.getBukkitEntity()).getShooter()).getHandle() : null);
            case SUFFOCATION:
                return sources.inWall();
            case FALL:
                return sources.fall();
            case FIRE:
                return sources.inFire();
            case FIRE_TICK:
                return sources.onFire();
            case MELTING:
                return sources.melting;
            case LAVA:
                return sources.lava();
            case DROWNING:
                return sources.drown();
            case BLOCK_EXPLOSION:
                return sources.explosion(nmsSource instanceof TNTPrimed && ((TNTPrimed) nmsSource).getSource() instanceof net.minecraft.world.entity.LivingEntity ? (net.minecraft.world.entity.LivingEntity) ((TNTPrimed) nmsSource).getSource() : null, null);
            case ENTITY_EXPLOSION:
                return sources.explosion(nmsSource, null);
            case VOID:
                return sources.outOfWorld();
            case LIGHTNING:
                return sources.lightningBolt();
            case STARVATION:
                return sources.starve();
            case POISON:
                return sources.poison;
            case MAGIC:
                return sources.magic();
            case WITHER:
                return sources.wither();
            case FALLING_BLOCK:
                return sources.fallingBlock(nmsSource);
            case THORNS:
                return sources.thorns(nmsSource);
            case DRAGON_BREATH:
                return sources.dragonBreath();
            case CUSTOM:
                return sources.generic();
            case FLY_INTO_WALL:
                return sources.flyIntoWall();
            case HOT_FLOOR:
                return sources.hotFloor();
            case CRAMMING:
                return sources.cramming();
            case DRYOUT:
                return sources.dryOut();
            //case SUICIDE:
            default:
                return new FakeDamageSrc(src);
        }
    }

    @Override
    public void damage(LivingEntity target, float amount, EntityTag source, Location sourceLoc, EntityDamageEvent.DamageCause cause) {
        if (target == null) {
            return;
        }
        net.minecraft.world.entity.LivingEntity nmsTarget = ((CraftLivingEntity) target).getHandle();
        net.minecraft.world.entity.Entity nmsSource = source == null ? null : ((CraftEntity) source.getBukkitEntity()).getHandle();
        CraftEventFactory.entityDamage = nmsSource;
        CraftEventFactory.blockDamage = sourceLoc == null ? null : sourceLoc.getBlock();
        try {
            DamageSource src = getSourceFor(nmsSource, cause, nmsTarget);
            if (src instanceof FakeDamageSrc) {
                src = ((FakeDamageSrc) src).real;
                EntityDamageEvent ede = fireFakeDamageEvent(target, source, sourceLoc, cause, amount);
                if (ede.isCancelled()) {
                    return;
                }
            }
            nmsTarget.hurt(src, amount);
        }
        finally {
            CraftEventFactory.entityDamage = null;
            CraftEventFactory.blockDamage = null;
        }
    }

    @Override
    public void setLastHurtBy(LivingEntity mob, LivingEntity damager) {
        ((CraftLivingEntity) mob).getHandle().setLastHurtByMob(((CraftLivingEntity) damager).getHandle());
    }

    public static final MethodHandle FALLINGBLOCK_TYPE_SETTER = ReflectionHelper.getFinalSetterForFirstOfType(net.minecraft.world.entity.item.FallingBlockEntity.class, BlockState.class);

    @Override
    public void setFallingBlockType(FallingBlock fallingBlock, BlockData block) {
        BlockState state = ((CraftBlockData) block).getState();
        FallingBlockEntity nmsEntity = ((CraftFallingBlock) fallingBlock).getHandle();
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
        ServerLevel level = ((CraftWorld) spawner.getWorld()).getHandle();
        net.minecraft.world.entity.Entity nmsEntity = nmsSpawner.getSpawner().getOrCreateDisplayEntity(level, level.random, nmsSpawner.getBlockPos());
        return new EntityTag(nmsEntity.getBukkitEntity());
    }

    public static final Field ZOMBIE_INWATERTIME = ReflectionHelper.getFields(net.minecraft.world.entity.monster.Zombie.class).get(ReflectionMappingsInfo.Zombie_inWaterTime, int.class);

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

    public static final MethodHandle TRACKING_RANGE_SETTER = ReflectionHelper.getFinalSetterForFirstOfType(ChunkMap.TrackedEntity.class, int.class);

    @Override
    public void setTrackingRange(Entity entity, int range) {
        try {
            ChunkMap map = ((CraftWorld) entity.getWorld()).getHandle().getChunkSource().chunkMap;
            ChunkMap.TrackedEntity entry = map.entityMap.get(entity.getEntityId());
            if (entry != null) {
                TRACKING_RANGE_SETTER.invoke(entry, range);
            }
        }
        catch (Throwable ex) {
            Debug.echoError(ex);
        }
    }

    @Override
    public boolean isAggressive(org.bukkit.entity.Mob mob) {
        return ((CraftMob) mob).getHandle().isAggressive();
    }

    @Override
    public void setAggressive(org.bukkit.entity.Mob mob, boolean aggressive) {
        ((CraftMob) mob).getHandle().setAggressive(aggressive);
    }

    public static final Field ENTITY_REMOVALREASON = ReflectionHelper.getFields(net.minecraft.world.entity.Entity.class).getFirstOfType(net.minecraft.world.entity.Entity.RemovalReason.class);
    public static final MethodHandle PLAYERLIST_REMOVE = ReflectionHelper.getMethodHandle(PlayerList.class, "remove", ServerPlayer.class);

    @Override
    public void setUUID(Entity entity, UUID id) {
        try {
            net.minecraft.world.entity.Entity nmsEntity = ((CraftEntity) entity).getHandle();
            nmsEntity.stopRiding();
            nmsEntity.getPassengers().forEach(net.minecraft.world.entity.Entity::stopRiding);
            Level level = nmsEntity.level;
            DedicatedPlayerList playerList = ((CraftServer) Bukkit.getServer()).getHandle();
            if (nmsEntity instanceof ServerPlayer) {
                PLAYERLIST_REMOVE.invoke(playerList, nmsEntity);
            }
            else {
                nmsEntity.remove(net.minecraft.world.entity.Entity.RemovalReason.DISCARDED);
            }
            ENTITY_REMOVALREASON.set(nmsEntity, null);
            nmsEntity.setUUID(id);
            if (nmsEntity instanceof ServerPlayer) {
                playerList.placeNewPlayer(DenizenNetworkManagerImpl.getConnection((ServerPlayer) nmsEntity), (ServerPlayer) nmsEntity);
            }
            else {
                level.addFreshEntity(nmsEntity);
            }
        }
        catch (Throwable ex) {
            Debug.echoError(ex);
        }
    }

    @Override
    public float getStepHeight(Entity entity) {
        return ((CraftEntity) entity).getHandle().maxUpStep();
    }

    @Override
    public void setStepHeight(Entity entity, float stepHeight) {
        ((CraftEntity) entity).getHandle().setMaxUpStep(stepHeight);
    }

    @Override
    public void openHorseInventory(Player player, AbstractHorse horse) {
        net.minecraft.world.entity.animal.horse.AbstractHorse nmsHorse = ((CraftAbstractHorse) horse).getHandle();
        ((CraftPlayer) player).getHandle().openHorseInventory(nmsHorse, nmsHorse.inventory);
    }
}

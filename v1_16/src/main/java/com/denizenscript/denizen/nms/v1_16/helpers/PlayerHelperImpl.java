package com.denizenscript.denizen.nms.v1_16.helpers;

import com.denizenscript.denizen.Denizen;
import com.denizenscript.denizen.nms.v1_16.Handler;
import com.denizenscript.denizen.nms.v1_16.impl.ImprovedOfflinePlayerImpl;
import com.denizenscript.denizen.nms.v1_16.impl.network.handlers.AbstractListenerPlayInImpl;
import com.denizenscript.denizen.nms.v1_16.impl.network.handlers.DenizenNetworkManagerImpl;
import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizen.objects.PlayerTag;
import com.denizenscript.denizen.utilities.FormattedTextHelper;
import com.denizenscript.denizencore.objects.Mechanism;
import com.mojang.authlib.GameProfile;
import com.denizenscript.denizen.nms.abstracts.ImprovedOfflinePlayer;
import com.denizenscript.denizen.nms.interfaces.PlayerHelper;
import com.denizenscript.denizencore.utilities.ReflectionHelper;
import com.denizenscript.denizencore.utilities.debugging.Debug;
import net.minecraft.server.v1_16_R3.*;
import org.bukkit.*;
import org.bukkit.Chunk;
import org.bukkit.SoundCategory;
import org.bukkit.boss.BossBar;
import org.bukkit.craftbukkit.v1_16_R3.CraftServer;
import org.bukkit.craftbukkit.v1_16_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_16_R3.boss.CraftBossBar;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftPlayer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.lang.reflect.Field;
import java.util.*;

public class PlayerHelperImpl extends PlayerHelper {

    public static final Field ATTACK_COOLDOWN_TICKS = ReflectionHelper.getFields(EntityLiving.class).get("aD");

    public static final Map<String, Field> PLAYER_CONNECTION_FIELDS = ReflectionHelper.getFields(PlayerConnection.class);
    public static final Field FLY_TICKS = PLAYER_CONNECTION_FIELDS.get("C");
    public static final Field VEHICLE_FLY_TICKS = PLAYER_CONNECTION_FIELDS.get("E");

    public static final DataWatcherObject<Byte> ENTITY_HUMAN_SKINLAYERS_DATAWATCHER;

    static {
        DataWatcherObject<Byte> skinlayers = null;
        try {
            skinlayers = (DataWatcherObject<Byte>) ReflectionHelper.getFields(EntityHuman.class).get("bi").get(null);
        }
        catch (Throwable ex) {
            ex.printStackTrace();
        }
        ENTITY_HUMAN_SKINLAYERS_DATAWATCHER = skinlayers;
    }

    @Override
    public void stopSound(Player player, String sound, SoundCategory category) {
        MinecraftKey soundKey = sound == null ? null : new MinecraftKey(sound);
        ((CraftPlayer) player).getHandle().playerConnection.sendPacket(new PacketPlayOutStopSound(soundKey, net.minecraft.server.v1_16_R3.SoundCategory.valueOf(category.name())));
    }

    @Override
    public void deTrackEntity(Player player, Entity entity) {
        EntityPlayer nmsPlayer = ((CraftPlayer) player).getHandle();
        WorldServer world = (WorldServer) nmsPlayer.world;
        PlayerChunkMap.EntityTracker tracker = world.getChunkProvider().playerChunkMap.trackedEntities.get(entity.getEntityId());
        if (tracker == null) {
            return;
        }
        sendEntityDestroy(player, entity);
        tracker.clear(nmsPlayer);
    }

    @Override
    public EntityTag sendEntitySpawn(List<PlayerTag> players, EntityType entityType, Location location, ArrayList<Mechanism> mechanisms, int customId, UUID customUUID, boolean autoTrack) {
        CraftWorld world = ((CraftWorld) location.getWorld());
        net.minecraft.server.v1_16_R3.Entity nmsEntity = world.createEntity(location,  entityType.getEntityClass());
        if (customUUID != null) {
            nmsEntity.e(customId);
            nmsEntity.a_(customUUID);
        }
        EntityTag entity = new EntityTag(nmsEntity.getBukkitEntity());
        for (Mechanism mechanism : mechanisms) {
            entity.safeAdjust(mechanism);
        }
        nmsEntity.dead = false;
        EntityTag entTag = new EntityTag(entity.getBukkitEntity());
        for (PlayerTag player : players) {
            EntityPlayer nmsPlayer = ((CraftPlayer) player.getPlayerEntity()).getHandle();
            PlayerConnection conn = nmsPlayer.playerConnection;
            final EntityTrackerEntry tracker = new EntityTrackerEntry(world.getHandle(), nmsEntity, 1, true, conn::sendPacket, Collections.singleton(nmsPlayer));
            tracker.b(nmsPlayer);
            if (autoTrack) {
                new BukkitRunnable() {
                    boolean wasOnline = true;
                    @Override
                    public void run() {
                        if (!entTag.isFakeValid) {
                            cancel();
                            return;
                        }
                        if (player.isOnline()) {
                            if (!wasOnline) {
                                tracker.b(nmsPlayer);
                                wasOnline = true;
                            }
                            tracker.a();
                        }
                    }
                }.runTaskTimer(Denizen.getInstance(), 1, 1);
            }
        }
        return entTag;
    }

    @Override
    public void sendEntityDestroy(Player player, Entity entity) {
        ((CraftPlayer) player).getHandle().playerConnection.sendPacket(new PacketPlayOutEntityDestroy(entity.getEntityId()));
    }

    @Override
    public int getFlyKickCooldown(Player player) {
        PlayerConnection conn = ((CraftPlayer) player).getHandle().playerConnection;
        if (conn instanceof AbstractListenerPlayInImpl) {
            conn = ((AbstractListenerPlayInImpl) conn).oldListener;
        }
        try {
            return Math.max(80 - Math.max(FLY_TICKS.getInt(conn), VEHICLE_FLY_TICKS.getInt(conn)), 0);
        }
        catch (IllegalAccessException e) {
            Debug.echoError(e);
        }
        return 80;
    }

    @Override
    public void setFlyKickCooldown(Player player, int ticks) {
        ticks = 80 - ticks;
        PlayerConnection conn = ((CraftPlayer) player).getHandle().playerConnection;
        if (conn instanceof AbstractListenerPlayInImpl) {
            conn = ((AbstractListenerPlayInImpl) conn).oldListener;
        }
        try {
            FLY_TICKS.setInt(conn, ticks);
            VEHICLE_FLY_TICKS.setInt(conn, ticks);
        }
        catch (IllegalAccessException e) {
            Debug.echoError(e);
        }
    }

    @Override
    public int ticksPassedDuringCooldown(Player player) {
        try {
            return ATTACK_COOLDOWN_TICKS.getInt(((CraftPlayer) player).getHandle());
        }
        catch (IllegalAccessException e) {
            Debug.echoError(e);
        }
        return -1;
    }

    @Override
    public float getMaxAttackCooldownTicks(Player player) {
        return ((CraftPlayer) player).getHandle().eR() + 3;
    }

    @Override
    public float getAttackCooldownPercent(Player player) {
        return ((CraftPlayer) player).getHandle().getAttackCooldown(0.5f);
    }

    @Override
    public void setAttackCooldown(Player player, int ticks) {
        // Theoretically the a(EnumHand) method sets the ATTACK_COOLDOWN_TICKS field to 0 and performs an
        // animation, but I'm unable to confirm if the animation actually triggers.
        //((CraftPlayer) player).getHandle().a(EnumHand.MAIN_HAND);
        try {
            ATTACK_COOLDOWN_TICKS.setInt(((CraftPlayer) player).getHandle(), ticks);
        }
        catch (IllegalAccessException e) {
            Debug.echoError(e);
        }

    }

    @Override
    public boolean hasChunkLoaded(Player player, Chunk chunk) {
        return ((CraftWorld) chunk.getWorld()).getHandle().getChunkProvider().playerChunkMap
                .a(new ChunkCoordIntPair(chunk.getX(), chunk.getZ()), false)
                .anyMatch(entityPlayer -> entityPlayer.getUniqueID().equals(player.getUniqueId()));
    }

    @Override
    public int getPing(Player player) {
        return ((CraftPlayer) player).getHandle().ping;
    }

    @Override
    public void setTemporaryOp(Player player, boolean op) {
        MinecraftServer server = ((CraftServer) Bukkit.getServer()).getServer();
        GameProfile profile = ((CraftPlayer) player).getProfile();
        OpList opList = server.getPlayerList().getOPs();
        if (op) {
            int permLevel = server.g();
            opList.add(new OpListEntry(profile, permLevel, opList.b(profile)));
        }
        else {
            opList.remove(profile);
        }
        player.recalculatePermissions();
    }

    @Override
    public void showEndCredits(Player player) {
        ((CraftPlayer) player).getHandle().viewingCredits = true;
        ((CraftPlayer) player).getHandle().playerConnection.sendPacket(new PacketPlayOutGameStateChange(PacketPlayOutGameStateChange.e, 1f));
    }

    @Override
    public ImprovedOfflinePlayer getOfflineData(UUID uuid) {
        return new ImprovedOfflinePlayerImpl(uuid);
    }

    @Override
    public ImprovedOfflinePlayer getOfflineData(OfflinePlayer offlinePlayer) {
        return new ImprovedOfflinePlayerImpl(offlinePlayer.getUniqueId());
    }

    @Override
    public void resendRecipeDetails(Player player) {
        Collection<IRecipe<?>> recipes = ((CraftServer) Bukkit.getServer()).getServer().getCraftingManager().b();
        PacketPlayOutRecipeUpdate updatePacket = new PacketPlayOutRecipeUpdate(recipes);
        ((CraftPlayer) player).getHandle().playerConnection.sendPacket(updatePacket);
    }

    @Override
    public void resendDiscoveredRecipes(Player player) {
        RecipeBookServer recipeBook = ((CraftPlayer) player).getHandle().getRecipeBook();
        recipeBook.a(((CraftPlayer) player).getHandle());
    }

    @Override
    public void quietlyAddRecipe(Player player, NamespacedKey key) {
        RecipeBookServer recipeBook = ((CraftPlayer) player).getHandle().getRecipeBook();
        IRecipe<?> recipe = ItemHelperImpl.getNMSRecipe(key);
        if (recipe == null) {
            Debug.echoError("Cannot add recipe '" + key + "': it does not exist.");
            return;
        }
        recipeBook.a(recipe);
        recipeBook.f(recipe);
    }

    @Override
    public String getPlayerBrand(Player player) {
        return ((DenizenNetworkManagerImpl) ((CraftPlayer) player).getHandle().playerConnection.networkManager).packetListener.brand;
    }

    @Override
    public byte getSkinLayers(Player player) {
        return ((CraftPlayer) player).getHandle().getDataWatcher().get(ENTITY_HUMAN_SKINLAYERS_DATAWATCHER);
    }

    @Override
    public void setSkinLayers(Player player, byte flags) {
        ((CraftPlayer) player).getHandle().getDataWatcher().set(ENTITY_HUMAN_SKINLAYERS_DATAWATCHER, flags);
    }

    @Override
    public void setBossBarTitle(BossBar bar, String title) {
        ((CraftBossBar) bar).getHandle().title = Handler.componentToNMS(FormattedTextHelper.parse(title));
        ((CraftBossBar) bar).getHandle().sendUpdate(PacketPlayOutBoss.Action.UPDATE_NAME);
    }
}

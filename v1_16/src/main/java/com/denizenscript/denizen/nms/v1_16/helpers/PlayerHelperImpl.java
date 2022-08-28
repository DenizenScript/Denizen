package com.denizenscript.denizen.nms.v1_16.helpers;

import com.denizenscript.denizen.Denizen;
import com.denizenscript.denizen.nms.abstracts.ImprovedOfflinePlayer;
import com.denizenscript.denizen.nms.enums.CustomEntityType;
import com.denizenscript.denizen.nms.interfaces.PlayerHelper;
import com.denizenscript.denizen.nms.v1_16.Handler;
import com.denizenscript.denizen.nms.v1_16.impl.ImprovedOfflinePlayerImpl;
import com.denizenscript.denizen.nms.v1_16.impl.entities.CraftFakePlayerImpl;
import com.denizenscript.denizen.nms.v1_16.impl.entities.EntityItemProjectileImpl;
import com.denizenscript.denizen.nms.v1_16.impl.network.handlers.AbstractListenerPlayInImpl;
import com.denizenscript.denizen.nms.v1_16.impl.network.handlers.DenizenNetworkManagerImpl;
import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizen.objects.ItemTag;
import com.denizenscript.denizen.objects.LocationTag;
import com.denizenscript.denizen.objects.PlayerTag;
import com.denizenscript.denizen.utilities.FormattedTextHelper;
import com.denizenscript.denizen.utilities.entity.DenizenEntityType;
import com.denizenscript.denizen.utilities.entity.FakeEntity;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.utilities.ReflectionHelper;
import com.denizenscript.denizencore.utilities.debugging.Debug;
import com.mojang.authlib.GameProfile;
import net.md_5.bungee.api.ChatColor;
import net.minecraft.server.v1_16_R3.*;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.SoundCategory;
import org.bukkit.*;
import org.bukkit.boss.BossBar;
import org.bukkit.craftbukkit.v1_16_R3.CraftServer;
import org.bukkit.craftbukkit.v1_16_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_16_R3.boss.CraftBossBar;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_16_R3.inventory.CraftItemStack;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
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
        net.minecraft.server.v1_16_R3.SoundCategory nmsCategory = category == null ? null : net.minecraft.server.v1_16_R3.SoundCategory.valueOf(category.name());
        ((CraftPlayer) player).getHandle().playerConnection.sendPacket(new PacketPlayOutStopSound(soundKey, nmsCategory));
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

    public static class TrackerData {
        public PlayerTag player;
        public EntityTrackerEntry tracker;
    }

    @Override
    public FakeEntity sendEntitySpawn(List<PlayerTag> players, DenizenEntityType entityType, LocationTag location, ArrayList<Mechanism> mechanisms, int customId, UUID customUUID, boolean autoTrack) {
        CraftWorld world = ((CraftWorld) location.getWorld());
        net.minecraft.server.v1_16_R3.Entity nmsEntity;
        if (entityType.isCustom()) {
            if (entityType.customEntityType == CustomEntityType.ITEM_PROJECTILE) {
                org.bukkit.inventory.ItemStack itemStack = new ItemStack(Material.STONE);
                for (Mechanism mechanism : mechanisms) {
                    if (mechanism.matches("item") && mechanism.requireObject(ItemTag.class)) {
                        itemStack = mechanism.valueAsType(ItemTag.class).getItemStack();
                    }
                }
                nmsEntity = new EntityItemProjectileImpl(world.getHandle(), location, CraftItemStack.asNMSCopy(itemStack));
            }
            else if (entityType.customEntityType == CustomEntityType.FAKE_PLAYER) {
                String name = null;
                String skin = null;
                for (Mechanism mechanism : new ArrayList<>(mechanisms)) {
                    if (mechanism.matches("name")) {
                        name = mechanism.getValue().asString();
                        mechanisms.remove(mechanism);
                    }
                    else if (mechanism.matches("skin")) {
                        skin = mechanism.getValue().asString();
                        mechanisms.remove(mechanism);
                    }
                    if (name != null && skin != null) {
                        break;
                    }
                }
                nmsEntity = ((CraftFakePlayerImpl) CustomEntityHelperImpl.spawnFakePlayer(location, name, skin, false)).getHandle();
            }
            else {
                throw new IllegalArgumentException("entityType");
            }
        }
        else {
            nmsEntity = world.createEntity(location, entityType.getBukkitEntityType().getEntityClass());
        }
        if (customUUID != null) {
            nmsEntity.e(customId);
            nmsEntity.a_(customUUID);
        }
        EntityTag entity = new EntityTag(nmsEntity.getBukkitEntity());
        for (Mechanism mechanism : mechanisms) {
            entity.safeAdjustDuplicate(mechanism);
        }
        nmsEntity.dead = false;
        FakeEntity fake = new FakeEntity(players, location, entity.getBukkitEntity().getEntityId());
        fake.entity = new EntityTag(entity.getBukkitEntity());
        fake.entity.isFake = true;
        fake.entity.isFakeValid = true;
        List<TrackerData> trackers = new ArrayList<>();
        fake.triggerSpawnPacket = (player) -> {
            EntityPlayer nmsPlayer = ((CraftPlayer) player.getPlayerEntity()).getHandle();
            PlayerConnection conn = nmsPlayer.playerConnection;
            final EntityTrackerEntry tracker = new EntityTrackerEntry(world.getHandle(), nmsEntity, 1, true, conn::sendPacket, Collections.singleton(nmsPlayer));
            tracker.b(nmsPlayer);
            final TrackerData data = new TrackerData();
            data.player = player;
            data.tracker = tracker;
            trackers.add(data);
            if (autoTrack) {
                new BukkitRunnable() {
                    boolean wasOnline = true;
                    @Override
                    public void run() {
                        if (!fake.entity.isFakeValid) {
                            cancel();
                            return;
                        }
                        if (player.isOnline()) {
                            if (!wasOnline) {
                                tracker.b(((CraftPlayer) player.getPlayerEntity()).getHandle());
                                wasOnline = true;
                            }
                            tracker.a();
                        }
                        else if (wasOnline) {
                            wasOnline = false;
                        }
                    }
                }.runTaskTimer(Denizen.getInstance(), 1, 1);
            }
        };
        for (PlayerTag player : players) {
            fake.triggerSpawnPacket.accept(player);
        }
        fake.triggerUpdatePacket = () -> {
            for (TrackerData tracker : trackers) {
                if (tracker.player.isOnline()) {
                    tracker.tracker.a();
                }
            }
        };
        fake.triggerDestroyPacket = () -> {
            for (TrackerData tracker : trackers) {
                if (tracker.player.isOnline()) {
                    tracker.tracker.a(((CraftPlayer) tracker.player.getPlayerEntity()).getHandle());
                }
            }
            trackers.clear();
        };
        return fake;
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
        ((CraftBossBar) bar).getHandle().title = Handler.componentToNMS(FormattedTextHelper.parse(title, ChatColor.WHITE));
        ((CraftBossBar) bar).getHandle().sendUpdate(PacketPlayOutBoss.Action.UPDATE_NAME);
    }
}

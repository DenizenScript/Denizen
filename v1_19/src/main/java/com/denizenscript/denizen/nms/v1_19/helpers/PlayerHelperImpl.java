package com.denizenscript.denizen.nms.v1_19.helpers;

import com.denizenscript.denizen.Denizen;
import com.denizenscript.denizen.nms.NMSHandler;
import com.denizenscript.denizen.nms.abstracts.ImprovedOfflinePlayer;
import com.denizenscript.denizen.nms.enums.CustomEntityType;
import com.denizenscript.denizen.nms.interfaces.PlayerHelper;
import com.denizenscript.denizen.nms.v1_19.Handler;
import com.denizenscript.denizen.nms.v1_19.ReflectionMappingsInfo;
import com.denizenscript.denizen.nms.v1_19.impl.ImprovedOfflinePlayerImpl;
import com.denizenscript.denizen.nms.v1_19.impl.ProfileEditorImpl;
import com.denizenscript.denizen.nms.v1_19.impl.entities.CraftFakePlayerImpl;
import com.denizenscript.denizen.nms.v1_19.impl.entities.EntityItemProjectileImpl;
import com.denizenscript.denizen.nms.v1_19.impl.network.handlers.AbstractListenerPlayInImpl;
import com.denizenscript.denizen.nms.v1_19.impl.network.handlers.DenizenNetworkManagerImpl;
import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizen.objects.ItemTag;
import com.denizenscript.denizen.objects.LocationTag;
import com.denizenscript.denizen.objects.PlayerTag;
import com.denizenscript.denizen.utilities.FormattedTextHelper;
import com.denizenscript.denizen.utilities.entity.DenizenEntityType;
import com.denizenscript.denizen.utilities.entity.FakeEntity;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.utilities.CoreUtilities;
import com.denizenscript.denizencore.utilities.ReflectionHelper;
import com.denizenscript.denizencore.utilities.debugging.Debug;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import it.unimi.dsi.fastutil.ints.IntList;
import net.md_5.bungee.api.ChatColor;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.protocol.game.*;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.server.level.ServerEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.server.players.ServerOpList;
import net.minecraft.server.players.ServerOpListEntry;
import net.minecraft.stats.ServerRecipeBook;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagNetworkSerialization;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemCooldowns;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.phys.AABB;
import org.bukkit.*;
import org.bukkit.boss.BossBar;
import org.bukkit.craftbukkit.v1_19_R2.CraftServer;
import org.bukkit.craftbukkit.v1_19_R2.CraftWorld;
import org.bukkit.craftbukkit.v1_19_R2.boss.CraftBossBar;
import org.bukkit.craftbukkit.v1_19_R2.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_19_R2.inventory.CraftItemStack;
import org.bukkit.craftbukkit.v1_19_R2.util.CraftMagicNumbers;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.lang.invoke.MethodHandle;
import java.lang.reflect.Field;
import java.util.*;

public class PlayerHelperImpl extends PlayerHelper {

    public static final Field ATTACK_COOLDOWN_TICKS = ReflectionHelper.getFields(LivingEntity.class).get(ReflectionMappingsInfo.LivingEntity_attackStrengthTicker, int.class);

    public static final Field FLY_TICKS = ReflectionHelper.getFields(ServerGamePacketListenerImpl.class).get(ReflectionMappingsInfo.ServerGamePacketListenerImpl_aboveGroundTickCount, int.class);
    public static final Field VEHICLE_FLY_TICKS = ReflectionHelper.getFields(ServerGamePacketListenerImpl.class).get(ReflectionMappingsInfo.ServerGamePacketListenerImpl_aboveGroundVehicleTickCount, int.class);
    public static final MethodHandle PLAYER_RESPAWNFORCED_SETTER = ReflectionHelper.getFinalSetter(ServerPlayer.class, ReflectionMappingsInfo.ServerPlayer_respawnForced, boolean.class);
    public static final MethodHandle SERVER_GAME_PACKET_LISTENER_IMPL_INTERNAL_TELEPORT = ReflectionHelper.getMethodHandle(ServerGamePacketListenerImpl.class, "internalTeleport", double.class, double.class, double.class, float.class, float.class, Set.class, boolean.class);

    public static final EntityDataAccessor<Byte> ENTITY_HUMAN_SKINLAYERS_DATAWATCHER;

    static {
        EntityDataAccessor<Byte> skinlayers = null;
        try {
            skinlayers = (EntityDataAccessor<Byte>) ReflectionHelper.getFields(net.minecraft.world.entity.player.Player.class).get(ReflectionMappingsInfo.Player_DATA_PLAYER_MODE_CUSTOMISATION).get(null);
        }
        catch (Throwable ex) {
            ex.printStackTrace();
        }
        ENTITY_HUMAN_SKINLAYERS_DATAWATCHER = skinlayers;
    }

    @Override
    public void stopSound(Player player, String sound, SoundCategory category) {
        ((CraftPlayer) player).getHandle().connection.send(new ClientboundStopSoundPacket(sound == null ? null : new ResourceLocation(sound), null));
    }

    @Override
    public void deTrackEntity(Player player, Entity entity) {
        ServerPlayer nmsPlayer = ((CraftPlayer) player).getHandle();
        ServerLevel world = (ServerLevel) nmsPlayer.level;
        ChunkMap.TrackedEntity tracker = world.getChunkSource().chunkMap.entityMap.get(entity.getEntityId());
        if (tracker == null) {
            if (NMSHandler.debugPackets) {
                DenizenNetworkManagerImpl.doPacketOutput("Failed to de-track entity " + entity.getEntityId() + " for " + player.getName() + ": tracker null");
            }
            return;
        }
        sendEntityDestroy(player, entity);
        tracker.removePlayer(nmsPlayer);
    }

    public static class TrackerData {
        public PlayerTag player;
        public ServerEntity tracker;
    }

    @Override
    public FakeEntity sendEntitySpawn(List<PlayerTag> players, DenizenEntityType entityType, LocationTag location, ArrayList<Mechanism> mechanisms, int customId, UUID customUUID, boolean autoTrack) {
        CraftWorld world = ((CraftWorld) location.getWorld());
        net.minecraft.world.entity.Entity nmsEntity;
        if (entityType.isCustom()) {
            if (entityType.customEntityType == CustomEntityType.ITEM_PROJECTILE) {
                ItemStack itemStack = new ItemStack(Material.STONE);
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
                String blob = null;
                for (Mechanism mechanism : new ArrayList<>(mechanisms)) {
                    if (mechanism.matches("name")) {
                        name = mechanism.getValue().asString();
                        mechanisms.remove(mechanism);
                    }
                    else if (mechanism.matches("skin")) {
                        skin = mechanism.getValue().asString();
                        mechanisms.remove(mechanism);
                    }
                    else if (mechanism.matches("skin_blob")) {
                        blob = mechanism.getValue().asString();
                        mechanisms.remove(mechanism);
                    }
                    if (name != null && (skin != null || blob != null)) {
                        break;
                    }
                }
                nmsEntity = ((CraftFakePlayerImpl) NMSHandler.customEntityHelper.spawnFakePlayer(location, name, skin, blob, false)).getHandle();
            }
            else {
                throw new IllegalArgumentException("entityType");
            }
        }
        else {
            nmsEntity = world.createEntity(location, entityType.getBukkitEntityType().getEntityClass());
        }
        if (customUUID != null) {
            nmsEntity.setId(customId);
            nmsEntity.setUUID(customUUID);
        }
        EntityTag entity = new EntityTag(nmsEntity.getBukkitEntity());
        entity.isFake = true;
        entity.isFakeValid = true;
        for (Mechanism mechanism : mechanisms) {
            entity.safeAdjustDuplicate(mechanism);
        }
        nmsEntity.unsetRemoved();
        FakeEntity fake = new FakeEntity(players, location, entity.getBukkitEntity().getEntityId());
        fake.entity = new EntityTag(entity.getBukkitEntity());
        fake.entity.isFake = true;
        fake.entity.isFakeValid = true;
        List<TrackerData> trackers = new ArrayList<>();
        fake.triggerSpawnPacket = (player) -> {
            ServerPlayer nmsPlayer = ((CraftPlayer) player.getPlayerEntity()).getHandle();
            ServerGamePacketListenerImpl conn = nmsPlayer.connection;
            final ServerEntity tracker = new ServerEntity(world.getHandle(), nmsEntity, 1, true, conn::send, Collections.singleton(nmsPlayer.connection));
            tracker.addPairing(nmsPlayer);
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
                                tracker.addPairing(((CraftPlayer) player.getPlayerEntity()).getHandle());
                                wasOnline = true;
                            }
                            tracker.sendChanges();
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
                    tracker.tracker.sendChanges();
                }
            }
        };
        fake.triggerDestroyPacket = () -> {
            for (TrackerData tracker : trackers) {
                if (tracker.player.isOnline()) {
                    tracker.tracker.removePairing(((CraftPlayer) tracker.player.getPlayerEntity()).getHandle());
                }
            }
            trackers.clear();
        };
        return fake;
    }

    @Override
    public void sendEntityDestroy(Player player, Entity entity) {
        ((CraftPlayer) player).getHandle().connection.send(new ClientboundRemoveEntitiesPacket(entity.getEntityId()));
    }

    @Override
    public int getFlyKickCooldown(Player player) {
        ServerGamePacketListenerImpl conn = ((CraftPlayer) player).getHandle().connection;
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
        ServerGamePacketListenerImpl conn = ((CraftPlayer) player).getHandle().connection;
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
        return ((CraftPlayer) player).getHandle().getCurrentItemAttackStrengthDelay() + 3;
    }

    @Override
    public void setAttackCooldown(Player player, int ticks) {
        try {
            ATTACK_COOLDOWN_TICKS.setInt(((CraftPlayer) player).getHandle(), ticks);
        }
        catch (IllegalAccessException e) {
            Debug.echoError(e);
        }

    }

    @Override
    public boolean hasChunkLoaded(Player player, Chunk chunk) {
        return ((CraftWorld) chunk.getWorld()).getHandle().getChunkSource().chunkMap
                .getPlayers(new ChunkPos(chunk.getX(), chunk.getZ()), false).stream()
                .anyMatch(entityPlayer -> entityPlayer.getUUID().equals(player.getUniqueId()));
    }

    @Override
    public void setTemporaryOp(Player player, boolean op) {
        MinecraftServer server = ((CraftServer) Bukkit.getServer()).getServer();
        GameProfile profile = ((CraftPlayer) player).getProfile();
        ServerOpList opList = server.getPlayerList().getOps();
        if (op) {
            int permLevel = server.getOperatorUserPermissionLevel();
            opList.add(new ServerOpListEntry(profile, permLevel, opList.canBypassPlayerLimit(profile)));
        }
        else {
            opList.remove(profile);
        }
        player.recalculatePermissions();
    }

    @Override
    public void showEndCredits(Player player) {
        ((CraftPlayer) player).getHandle().wonGame = true;
        ((CraftPlayer) player).getHandle().connection.send(new ClientboundGameEventPacket(ClientboundGameEventPacket.WIN_GAME, 1f));
    }

    @Override
    public ImprovedOfflinePlayer getOfflineData(UUID uuid) {
        return new ImprovedOfflinePlayerImpl(uuid);
    }

    @Override
    public void resendRecipeDetails(Player player) {
        Collection<Recipe<?>> recipes = ((CraftServer) Bukkit.getServer()).getServer().getRecipeManager().getRecipes();
        ClientboundUpdateRecipesPacket updatePacket = new ClientboundUpdateRecipesPacket(recipes);
        ((CraftPlayer) player).getHandle().connection.send(updatePacket);
    }

    @Override
    public void resendDiscoveredRecipes(Player player) {
        ServerRecipeBook recipeBook = ((CraftPlayer) player).getHandle().getRecipeBook();
        recipeBook.sendInitialRecipeBook(((CraftPlayer) player).getHandle());
    }

    @Override
    public void quietlyAddRecipe(Player player, NamespacedKey key) {
        ServerRecipeBook recipeBook = ((CraftPlayer) player).getHandle().getRecipeBook();
        Recipe<?> recipe = ItemHelperImpl.getNMSRecipe(key);
        if (recipe == null) {
            Debug.echoError("Cannot add recipe '" + key + "': it does not exist.");
            return;
        }
        recipeBook.add(recipe);
        recipeBook.addHighlight(recipe);
    }

    @Override
    public String getPlayerBrand(Player player) {
        return ((DenizenNetworkManagerImpl) ((CraftPlayer) player).getHandle().connection.connection).packetListener.brand;
    }

    @Override
    public byte getSkinLayers(Player player) {
        return ((CraftPlayer) player).getHandle().getEntityData().get(ENTITY_HUMAN_SKINLAYERS_DATAWATCHER);
    }

    @Override
    public void setSkinLayers(Player player, byte flags) {
        ((CraftPlayer) player).getHandle().getEntityData().set(ENTITY_HUMAN_SKINLAYERS_DATAWATCHER, flags);
    }

    @Override
    public void setBossBarTitle(BossBar bar, String title) {
        ((CraftBossBar) bar).getHandle().name = Handler.componentToNMS(FormattedTextHelper.parse(title, ChatColor.WHITE));
        ((CraftBossBar) bar).getHandle().broadcast(ClientboundBossEventPacket::createUpdateNamePacket);
    }

    @Override
    public boolean getSpawnForced(Player player) {
        return ((CraftPlayer) player).getHandle().isRespawnForced();
    }

    @Override
    public void setSpawnForced(Player player, boolean forced) {
        ServerPlayer nmsPlayer = ((CraftPlayer) player).getHandle();
        try {
            PLAYER_RESPAWNFORCED_SETTER.invoke(nmsPlayer, forced);
        }
        catch (Throwable ex) {
            Debug.echoError(ex);
        }
    }

    @Override
    public long getLastActionTime(Player player) {
        return ((CraftPlayer) player).getHandle().getLastActionTime();
    }

    @Override
    public void sendPlayerInfoAddPacket(Player player, ProfileEditMode mode, String name, String display, UUID id, String texture, String signature, int latency, GameMode gameMode) {
        boolean listed = true; // TODO: 1.19.3: Add 'listed' input
        ClientboundPlayerInfoUpdatePacket.Action action = mode == ProfileEditMode.ADD ? ClientboundPlayerInfoUpdatePacket.Action.ADD_PLAYER : (mode == ProfileEditMode.UPDATE_DISPLAY ? ClientboundPlayerInfoUpdatePacket.Action.UPDATE_DISPLAY_NAME : ClientboundPlayerInfoUpdatePacket.Action.UPDATE_LATENCY);
        GameProfile profile = new GameProfile(id, name);
        if (texture != null) {
            profile.getProperties().put("textures", new Property("textures", texture, signature));
        }
        ClientboundPlayerInfoUpdatePacket.Entry entry = new ClientboundPlayerInfoUpdatePacket.Entry(id, profile, listed, latency, GameType.byName(CoreUtilities.toLowerCase(gameMode.name())), display == null ? null : Handler.componentToNMS(FormattedTextHelper.parse(display, ChatColor.WHITE)), null);
        PacketHelperImpl.send(player, ProfileEditorImpl.createInfoPacket(EnumSet.of(action), Collections.singletonList(entry)));
    }

    @Override
    public void sendPlayerRemovePacket(Player player, UUID id) {
        PacketHelperImpl.send(player, new ClientboundPlayerInfoRemovePacket(Collections.singletonList(id)));
    }

    @Override
    public void sendClimbableMaterials(Player player, List<Material> materials) {
        Map<ResourceKey<? extends Registry<?>>, TagNetworkSerialization.NetworkPayload> packetInput = TagNetworkSerialization.serializeTagsToNetwork(((CraftServer) Bukkit.getServer()).getServer().registries());
        Map<ResourceLocation, IntList> tags = ReflectionHelper.getFieldValue(TagNetworkSerialization.NetworkPayload.class, ReflectionMappingsInfo.TagNetworkSerializationNetworkPayload_tags, packetInput.get(BuiltInRegistries.BLOCK.key()));
        IntList intList = tags.get(BlockTags.CLIMBABLE.location());
        intList.clear();
        for (Material material : materials) {
            intList.add(BuiltInRegistries.BLOCK.getId(CraftMagicNumbers.getBlock(material)));
        }
        PacketHelperImpl.send(player, new ClientboundUpdateTagsPacket(packetInput));
    }

    @Override
    public void refreshPlayer(Player player) {
        ServerPlayer nmsPlayer = ((CraftPlayer) player).getHandle();
        ServerLevel nmsWorld = (ServerLevel) nmsPlayer.level;
        nmsPlayer.connection.send(new ClientboundRespawnPacket(
                nmsWorld.dimensionTypeId(),
                nmsWorld.dimension(),
                BiomeManager.obfuscateSeed(nmsWorld.getSeed()),
                nmsPlayer.gameMode.getGameModeForPlayer(),
                nmsPlayer.gameMode.getPreviousGameModeForPlayer(),
                nmsWorld.isDebug(),
                nmsWorld.isFlat(),
                ClientboundRespawnPacket.KEEP_ALL_DATA,
                nmsPlayer.getLastDeathLocation()));
        nmsPlayer.onUpdateAbilities();
        try {
            Location loc = player.getLocation();
            SERVER_GAME_PACKET_LISTENER_IMPL_INTERNAL_TELEPORT.invoke(nmsPlayer.connection, loc.getX(), loc.getY(), loc.getZ(), loc.getYaw(), loc.getPitch(), Set.of(), false);
        }
        catch (Throwable ex) {
            Debug.echoError(ex);
            return;
        }
        if (nmsPlayer.isPassenger()) {
           nmsPlayer.connection.send(new ClientboundSetPassengersPacket(nmsPlayer.getVehicle()));
        }
        if (nmsPlayer.isVehicle()) {
            nmsPlayer.connection.send(new ClientboundSetPassengersPacket(nmsPlayer));
        }
        AABB boundingBox = new AABB(nmsPlayer.position(), nmsPlayer.position()).inflate(10);
        for (Mob nmsMob : nmsWorld.getEntitiesOfClass(Mob.class, boundingBox, nmsMob -> nmsPlayer.equals(nmsMob.getLeashHolder()))) {
            nmsPlayer.connection.send(new ClientboundSetEntityLinkPacket(nmsMob, nmsPlayer));
        }
        if (!nmsPlayer.getCooldowns().cooldowns.isEmpty()) {
            int tickCount = nmsPlayer.getCooldowns().tickCount;
            for (Map.Entry<Item, ItemCooldowns.CooldownInstance> entry : nmsPlayer.getCooldowns().cooldowns.entrySet()) {
                nmsPlayer.connection.send(new ClientboundCooldownPacket(entry.getKey(), entry.getValue().endTime - tickCount));
            }
        }
        nmsPlayer.server.getPlayerList().sendAllPlayerInfo(nmsPlayer);
        nmsPlayer.server.getPlayerList().sendPlayerPermissionLevel(nmsPlayer);
    }
}

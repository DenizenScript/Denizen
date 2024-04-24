package com.denizenscript.denizen.nms.v1_20.helpers;

import com.denizenscript.denizen.nms.NMSHandler;
import com.denizenscript.denizen.nms.interfaces.PacketHelper;
import com.denizenscript.denizen.nms.v1_20.Handler;
import com.denizenscript.denizen.nms.v1_20.ReflectionMappingsInfo;
import com.denizenscript.denizen.nms.v1_20.impl.SidebarImpl;
import com.denizenscript.denizen.nms.v1_20.impl.network.handlers.DenizenNetworkManagerImpl;
import com.denizenscript.denizen.scripts.commands.entity.TeleportCommand;
import com.denizenscript.denizen.utilities.FormattedTextHelper;
import com.denizenscript.denizen.utilities.Utilities;
import com.denizenscript.denizen.utilities.maps.MapImage;
import com.denizenscript.denizen.utilities.packets.NetworkInterceptHelper;
import com.denizenscript.denizencore.objects.core.ColorTag;
import com.denizenscript.denizencore.utilities.ReflectionHelper;
import com.denizenscript.denizencore.utilities.debugging.Debug;
import net.md_5.bungee.api.ChatColor;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket;
import net.minecraft.network.protocol.common.custom.BrandPayload;
import net.minecraft.network.protocol.common.custom.GameTestAddMarkerDebugPayload;
import net.minecraft.network.protocol.common.custom.GameTestClearMarkersDebugPayload;
import net.minecraft.network.protocol.game.*;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.server.level.ServerEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.RelativeMovement;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.CaveSpider;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.entity.monster.Spider;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.Team;
import org.bukkit.Bukkit;
import org.bukkit.EntityEffect;
import org.bukkit.Location;
import org.bukkit.block.Sign;
import org.bukkit.block.sign.Side;
import org.bukkit.block.sign.SignSide;
import org.bukkit.craftbukkit.v1_20_R4.CraftServer;
import org.bukkit.craftbukkit.v1_20_R4.CraftWorld;
import org.bukkit.craftbukkit.v1_20_R4.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_20_R4.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_20_R4.inventory.CraftItemStack;
import org.bukkit.craftbukkit.v1_20_R4.map.CraftMapCanvas;
import org.bukkit.craftbukkit.v1_20_R4.map.CraftMapView;
import org.bukkit.craftbukkit.v1_20_R4.util.CraftLocation;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.map.MapCanvas;
import org.bukkit.map.MapPalette;

import java.lang.invoke.MethodHandle;
import java.lang.reflect.Field;
import java.util.*;

public class PacketHelperImpl implements PacketHelper {

    public static final EntityDataAccessor<Float> PLAYER_DATA_ACCESSOR_ABSORPTION = ReflectionHelper.getFieldValue(net.minecraft.world.entity.player.Player.class, ReflectionMappingsInfo.Player_DATA_PLAYER_ABSORPTION_ID, null);

    public static final EntityDataAccessor<Byte> ENTITY_DATA_ACCESSOR_FLAGS = ReflectionHelper.getFieldValue(net.minecraft.world.entity.Entity.class, ReflectionMappingsInfo.Entity_DATA_SHARED_FLAGS_ID, null);

    public static final MethodHandle ABILITIES_PACKET_FOV_SETTER = ReflectionHelper.getFinalSetter(ClientboundPlayerAbilitiesPacket.class, ReflectionMappingsInfo.ClientboundPlayerAbilitiesPacket_walkingSpeed);

    public static final Field ENTITY_TRACKER_ENTRY_GETTER = ReflectionHelper.getFields(ChunkMap.TrackedEntity.class).getFirstOfType(ServerEntity.class);

    public static final MethodHandle CANVAS_GET_BUFFER = ReflectionHelper.getMethodHandle(CraftMapCanvas.class, "getBuffer");
    public static final Field MAPVIEW_WORLDMAP = ReflectionHelper.getFields(CraftMapView.class).get("worldMap");

    public static final EntityDataAccessor<Optional<Component>> ENTITY_DATA_ACCESSOR_CUSTOM_NAME = ReflectionHelper.getFieldValue(net.minecraft.world.entity.Entity.class, ReflectionMappingsInfo.Entity_DATA_CUSTOM_NAME, null);
    public static final EntityDataAccessor<Boolean> ENTITY_DATA_ACCESSOR_CUSTOM_NAME_VISIBLE = ReflectionHelper.getFieldValue(net.minecraft.world.entity.Entity.class, ReflectionMappingsInfo.Entity_DATA_CUSTOM_NAME_VISIBLE, null);

    @Override
    public void setFakeAbsorption(Player player, float value) {
        send(player, new ClientboundSetEntityDataPacket(player.getEntityId(), List.of(createEntityData(PLAYER_DATA_ACCESSOR_ABSORPTION, value))));
    }

    @Override
    public void setSlot(Player player, int slot, ItemStack itemStack, boolean playerOnly) {
        AbstractContainerMenu menu = ((CraftPlayer) player).getHandle().containerMenu;
        int windowId = playerOnly ? 0 : menu.containerId;
        send(player, new ClientboundContainerSetSlotPacket(windowId, menu.incrementStateId(), slot, CraftItemStack.asNMSCopy(itemStack)));
    }

    @Override
    public void setFieldOfView(Player player, float fov) {
        ClientboundPlayerAbilitiesPacket packet = new ClientboundPlayerAbilitiesPacket(((CraftPlayer) player).getHandle().getAbilities());
        if (!Float.isNaN(fov)) {
            try {
                ABILITIES_PACKET_FOV_SETTER.invoke(packet, fov);
            }
            catch (Throwable ex) {
                Debug.echoError(ex);
            }
        }
        send(player, packet);
    }

    @Override
    public void respawn(Player player) {
        ((CraftPlayer) player).getHandle().connection.handleClientCommand(new ServerboundClientCommandPacket(ServerboundClientCommandPacket.Action.PERFORM_RESPAWN));
    }

    @Override
    public void setVision(Player player, EntityType entityType) {
        final net.minecraft.world.entity.LivingEntity entity;
        if (entityType == EntityType.CREEPER) {
            entity = new Creeper(net.minecraft.world.entity.EntityType.CREEPER, ((CraftWorld) player.getWorld()).getHandle());
        }
        else if (entityType == EntityType.SPIDER) {
            entity = new Spider(net.minecraft.world.entity.EntityType.SPIDER, ((CraftWorld) player.getWorld()).getHandle());
        }
        else if (entityType == EntityType.CAVE_SPIDER) {
            entity = new CaveSpider(net.minecraft.world.entity.EntityType.CAVE_SPIDER, ((CraftWorld) player.getWorld()).getHandle());
        }
        else if (entityType == EntityType.ENDERMAN) {
            entity = new EnderMan(net.minecraft.world.entity.EntityType.ENDERMAN, ((CraftWorld) player.getWorld()).getHandle());
        }
        else {
            return;
        }

        // Spectating an entity then immediately respawning the player prevents a client shader update,
        // allowing the player to retain whatever vision the mob they spectated had.
        send(player, new ClientboundAddEntityPacket(entity));
        send(player, new ClientboundSetCameraPacket(entity));
        NMSHandler.playerHelper.refreshPlayer(player);
    }

    @Override
    public void showBlockAction(Player player, Location location, int action, int state) {
        BlockPos position = CraftLocation.toBlockPosition(location);
        Block block = ((CraftWorld) location.getWorld()).getHandle().getBlockState(position).getBlock();
        send(player, new ClientboundBlockEventPacket(position, block, action, state));
    }

    @Override
    public void showTabListHeaderFooter(Player player, String header, String footer) {
        Component cHeader = Handler.componentToNMS(FormattedTextHelper.parse(header, ChatColor.WHITE));
        Component cFooter = Handler.componentToNMS(FormattedTextHelper.parse(footer, ChatColor.WHITE));
        send(player, new ClientboundTabListPacket(cHeader, cFooter));
    }

    @Override
    public void showTitle(Player player, String title, String subtitle, int fadeInTicks, int stayTicks, int fadeOutTicks) {
        send(player, new ClientboundSetTitlesAnimationPacket(fadeInTicks, stayTicks, fadeOutTicks));
        if (title != null) {
            send(player, new ClientboundSetTitleTextPacket(Handler.componentToNMS(FormattedTextHelper.parse(title, ChatColor.WHITE))));
        }
        if (subtitle != null) {
            send(player, new ClientboundSetSubtitleTextPacket(Handler.componentToNMS(FormattedTextHelper.parse(subtitle, ChatColor.WHITE))));
        }
    }

    @Override
    public void showMobHealth(Player player, LivingEntity mob, double health, double maxHealth) {
        AttributeInstance attr = new AttributeInstance(Attributes.MAX_HEALTH, (a) -> {});
        attr.setBaseValue(maxHealth);
        send(player, new ClientboundUpdateAttributesPacket(mob.getEntityId(), List.of(attr)));
        send(player, new ClientboundSetEntityDataPacket(mob.getEntityId(), List.of(createEntityData(net.minecraft.world.entity.LivingEntity.DATA_HEALTH_ID, (float) health))));
    }

    @Override
    public void showSignEditor(Player player, Location location) {
        NetworkInterceptHelper.enable();
        Sign sign = null;
        BlockPos toOpen = null;
        // It actually allows 8 blocks of distance, but we limit to 7 because the client doesn't properly round down
        for (int i = 0; i < 8; i++) {
            Location toCheck = player.getLocation();
            toCheck.setY(toCheck.getY() - i);
            if (toCheck.getBlock().getState() instanceof Sign foundSign) {
                sign = foundSign;
            }
            else {
                sign = null;
                toOpen = CraftLocation.toBlockPosition(toCheck);
                break;
            }
        }
        if (sign != null) {
            toOpen = CraftLocation.toBlockPosition(sign.getLocation());
            SignSide front = sign.getSide(Side.FRONT);
            for (int line = 0; line < 4; line++) {
                front.setLine(line, "");
            }
            player.sendBlockUpdate(sign.getLocation(), sign);
        }
        DenizenNetworkManagerImpl.getNetworkManager(player).packetListener.fakeSignExpected = toOpen;
        send(player, new ClientboundOpenSignEditorPacket(toOpen, true));
    }

    @Override
    public void forceSpectate(Player player, Entity entity) {
        send(player, new ClientboundSetCameraPacket(((CraftEntity) entity).getHandle()));
    }

    public static void forceRespawnPlayerEntity(Entity entity, Player viewer) {
        ChunkMap tracker = ((ServerLevel) ((CraftEntity) entity).getHandle().level()).getChunkSource().chunkMap;
        ChunkMap.TrackedEntity entityTracker = tracker.entityMap.get(entity.getEntityId());
        if (entityTracker != null) {
            try {
                ServerEntity entry = (ServerEntity) ENTITY_TRACKER_ENTRY_GETTER.get(entityTracker);
                if (entry != null) {
                    entry.removePairing(((CraftPlayer) viewer).getHandle());
                    entry.addPairing(((CraftPlayer) viewer).getHandle());
                }
            }
            catch (Throwable ex) {
                Debug.echoError(ex);
            }
        }
    }

    @Override
    public void sendRename(Player player, Entity entity, String name, boolean listMode) {
        try {
            if (entity.getType() == EntityType.PLAYER) {
                if (listMode) {
                    send(player, new ClientboundPlayerInfoUpdatePacket(ClientboundPlayerInfoUpdatePacket.Action.UPDATE_DISPLAY_NAME, ((CraftPlayer) player).getHandle()));
                }
                else {
                    // For player entities, force a respawn packet and let the dynamic intercept correct the details
                    forceRespawnPlayerEntity(entity, player);
                }
                return;
            }
            List<SynchedEntityData.DataValue<?>> list = List.of(
                    createEntityData(ENTITY_DATA_ACCESSOR_CUSTOM_NAME, Optional.of(Handler.componentToNMS(FormattedTextHelper.parse(name, ChatColor.WHITE)))),
                    createEntityData(ENTITY_DATA_ACCESSOR_CUSTOM_NAME_VISIBLE, true)
            );
            send(player, new ClientboundSetEntityDataPacket(entity.getEntityId(), list));
        }
        catch (Throwable ex) {
            Debug.echoError(ex);
        }
    }

    public static HashMap<UUID, HashMap<UUID, PlayerTeam>> noCollideTeamMap = new HashMap<>();

    @Override
    public void generateNoCollideTeam(Player player, UUID noCollide) {
        removeNoCollideTeam(player, noCollide);
        PlayerTeam team = new PlayerTeam(SidebarImpl.dummyScoreboard, Utilities.generateRandomColors(8));
        team.getPlayers().add(noCollide.toString());
        team.setCollisionRule(Team.CollisionRule.NEVER);
        HashMap<UUID, PlayerTeam> map = noCollideTeamMap.computeIfAbsent(player.getUniqueId(), k -> new HashMap<>());
        map.put(noCollide, team);
        send(player, ClientboundSetPlayerTeamPacket.createAddOrModifyPacket(team, true));
    }

    @Override
    public void removeNoCollideTeam(Player player, UUID noCollide) {
        if (noCollide == null || !player.isOnline()) {
            noCollideTeamMap.remove(player.getUniqueId());
            return;
        }
        HashMap<UUID, PlayerTeam> map = noCollideTeamMap.get(player.getUniqueId());
        if (map == null) {
            return;
        }
        PlayerTeam team = map.remove(noCollide);
        if (team != null) {
            send(player, ClientboundSetPlayerTeamPacket.createRemovePacket(team));
        }
        if (map.isEmpty()) {
            noCollideTeamMap.remove(player.getUniqueId());
        }
    }

    @Override
    public void sendEntityMetadataFlagsUpdate(Player player, Entity entity) {
        byte flags = ((CraftEntity) entity).getHandle().getEntityData().get(ENTITY_DATA_ACCESSOR_FLAGS);
        send(player, new ClientboundSetEntityDataPacket(entity.getEntityId(), List.of(createEntityData(ENTITY_DATA_ACCESSOR_FLAGS, flags))));
    }

    @Override
    public void sendEntityEffect(Player player, Entity entity, EntityEffect effect) {
        send(player, new ClientboundEntityEventPacket(((CraftEntity) entity).getHandle(), effect.getData()));
    }

    @Override
    public int getPacketStats(Player player, boolean sent) {
        DenizenNetworkManagerImpl netMan = DenizenNetworkManagerImpl.getNetworkManager(player);
        return sent ? netMan.packetsSent : netMan.packetsReceived;
    }

    @Override
    public void setMapData(MapCanvas canvas, byte[] bytes, int x, int y, MapImage image) {
        if (x > 127 || y > 127) {
            return;
        }
        int width = Math.min(image.width, 128 - x),
                height = Math.min(image.height, 128 - y);
        if (x + width <= 0 || y + height <= 0) {
            return;
        }
        try {
            boolean anyChanged = false;
            byte[] buffer = (byte[]) CANVAS_GET_BUFFER.invoke(canvas);
            for (int x2 = x < 0 ? -x : 0; x2 < width; ++x2) {
                for (int y2 = y < 0 ? -y : 0; y2 < height; ++y2) {
                    byte p = bytes[y2 * image.width + x2];
                    if (p != MapPalette.TRANSPARENT) {
                        int index = (y2 + y) * 128 + (x2 + x);
                        if (buffer[index] != p) {
                            buffer[index] = p;
                            anyChanged = true;
                        }
                    }
                }
            }
            if (anyChanged) {
                // Flag the whole image as dirty
                MapItemSavedData map = (MapItemSavedData) MAPVIEW_WORLDMAP.get(canvas.getMapView());
                map.setColorsDirty(Math.max(x, 0), Math.max(y, 0));
                map.setColorsDirty(width + x - 1, height + y - 1);
            }
        }
        catch (Throwable ex) {
            Debug.echoError(ex);
        }
    }

    @Override
    public void setNetworkManagerFor(Player player) {
        DenizenNetworkManagerImpl.setNetworkManager(player);
    }

    @Override
    public void enableNetworkManager() {
        DenizenNetworkManagerImpl.enableNetworkManager();
    }

    @Override
    public void showDebugTestMarker(Player player, Location location, ColorTag color, String name, int time) {
        int colorInt = color.blue | (color.green << 8) | (color.red << 16) | (color.alpha << 24);
        GameTestAddMarkerDebugPayload payload = new GameTestAddMarkerDebugPayload(CraftLocation.toBlockPosition(location), colorInt, name, time);
        send(player, new ClientboundCustomPayloadPacket(payload));
    }

    @Override
    public void clearDebugTestMarker(Player player) {
        GameTestClearMarkersDebugPayload payload = new GameTestClearMarkersDebugPayload();
        send(player, new ClientboundCustomPayloadPacket(payload));
    }

    @Override
    public void sendBrand(Player player, String brand) {
        BrandPayload payload = new BrandPayload(brand);
        send(player, new ClientboundCustomPayloadPacket(payload));
    }

    @Override
    public void sendCollectItemEntity(Player player, Entity taker, Entity item, int amount) {
        send(player, new ClientboundTakeItemEntityPacket(item.getEntityId(), taker.getEntityId(), amount));
    }

    public RelativeMovement toNmsRelativeMovement(TeleportCommand.Relative relative) {
        return switch (relative) {
            case X -> RelativeMovement.X;
            case Y -> RelativeMovement.Y;
            case Z -> RelativeMovement.Z;
            case YAW -> RelativeMovement.Y_ROT;
            case PITCH -> RelativeMovement.X_ROT;
        };
    }

    @Override
    public void sendRelativePositionPacket(Player player, double x, double y, double z, float yaw, float pitch, List<TeleportCommand.Relative> relativeAxis) {
        Set<RelativeMovement> relativeMovements;
        if (relativeAxis == null) {
            relativeMovements = RelativeMovement.ALL;
        }
        else {
            relativeMovements = EnumSet.noneOf(RelativeMovement.class);
            for (TeleportCommand.Relative relative : relativeAxis) {
                relativeMovements.add(toNmsRelativeMovement(relative));
            }
        }
        ClientboundPlayerPositionPacket packet = new ClientboundPlayerPositionPacket(x, y, z, yaw, pitch, relativeMovements, 0);
        sendAsyncSafe(player, packet);
    }

    @Override
    public void sendRelativeLookPacket(Player player, float yaw, float pitch) {
        sendRelativePositionPacket(player, 0, 0, 0, yaw, pitch, null);
    }

    @Override
    public void sendEntityDataPacket(List<Player> players, Entity entity, List<Object> data) {
        ClientboundSetEntityDataPacket setEntityDataPacket = new ClientboundSetEntityDataPacket(entity.getEntityId(), (List<SynchedEntityData.DataValue<?>>) (Object) data);
        Iterator<Player> playerIterator = players.iterator();
        while (playerIterator.hasNext()) {
            Player player = playerIterator.next();
            if (!DenizenNetworkManagerImpl.getConnection(player).isConnected()) {
                playerIterator.remove();
                continue;
            }
            sendAsyncSafe(player, setEntityDataPacket);
        }
    }

    public static void send(Player player, Packet<?> packet) {
        ((CraftPlayer) player).getHandle().connection.send(packet);
    }

    public static void broadcast(Packet<?> packet) {
        ((CraftServer) Bukkit.getServer()).getHandle().broadcastAll(packet);
    }

    public static void sendAsyncSafe(Player player, Packet<?> packet) {
        DenizenNetworkManagerImpl.getConnection(player).channel.writeAndFlush(packet);
    }

    public static <T> SynchedEntityData.DataValue<T> createEntityData(EntityDataAccessor<T> accessor, T value) {
        return new SynchedEntityData.DataValue<>(accessor.id(), accessor.serializer(), value);
    }
}

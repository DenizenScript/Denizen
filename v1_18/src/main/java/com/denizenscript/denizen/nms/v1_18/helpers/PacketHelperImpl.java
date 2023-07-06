package com.denizenscript.denizen.nms.v1_18.helpers;

import com.denizenscript.denizen.nms.NMSHandler;
import com.denizenscript.denizen.nms.interfaces.PacketHelper;
import com.denizenscript.denizen.nms.util.jnbt.CompoundTag;
import com.denizenscript.denizen.nms.util.jnbt.JNBTListTag;
import com.denizenscript.denizen.nms.v1_18.Handler;
import com.denizenscript.denizen.nms.v1_18.ReflectionMappingsInfo;
import com.denizenscript.denizen.nms.v1_18.impl.SidebarImpl;
import com.denizenscript.denizen.nms.v1_18.impl.jnbt.CompoundTagImpl;
import com.denizenscript.denizen.nms.v1_18.impl.network.handlers.DenizenNetworkManagerImpl;
import com.denizenscript.denizen.objects.LocationTag;
import com.denizenscript.denizen.objects.MaterialTag;
import com.denizenscript.denizen.objects.PlayerTag;
import com.denizenscript.denizen.utilities.FormattedTextHelper;
import com.denizenscript.denizen.utilities.Utilities;
import com.denizenscript.denizen.utilities.blocks.FakeBlock;
import com.denizenscript.denizen.utilities.maps.MapImage;
import com.denizenscript.denizencore.objects.core.ColorTag;
import com.denizenscript.denizencore.objects.core.DurationTag;
import com.denizenscript.denizencore.utilities.ReflectionHelper;
import com.denizenscript.denizencore.utilities.debugging.Debug;
import com.mojang.datafixers.util.Pair;
import io.netty.buffer.Unpooled;
import net.md_5.bungee.api.ChatColor;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.*;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.server.level.ServerEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.CaveSpider;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.entity.monster.Spider;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.Team;
import org.bukkit.Bukkit;
import org.bukkit.EntityEffect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.banner.Pattern;
import org.bukkit.craftbukkit.v1_18_R2.CraftServer;
import org.bukkit.craftbukkit.v1_18_R2.CraftWorld;
import org.bukkit.craftbukkit.v1_18_R2.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_18_R2.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_18_R2.inventory.CraftItemStack;
import org.bukkit.craftbukkit.v1_18_R2.map.CraftMapCanvas;
import org.bukkit.craftbukkit.v1_18_R2.map.CraftMapView;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.map.MapCanvas;
import org.bukkit.map.MapPalette;

import java.lang.invoke.MethodHandle;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class PacketHelperImpl implements PacketHelper {

    public static final EntityDataAccessor<Float> ENTITY_HUMAN_DATA_WATCHER_ABSORPTION = ReflectionHelper.getFieldValue(net.minecraft.world.entity.player.Player.class, ReflectionMappingsInfo.Player_DATA_PLAYER_ABSORPTION_ID, null);

    public static final EntityDataAccessor<Byte> ENTITY_DATA_WATCHER_FLAGS = ReflectionHelper.getFieldValue(net.minecraft.world.entity.Entity.class, ReflectionMappingsInfo.Entity_DATA_SHARED_FLAGS_ID, null);

    public static final MethodHandle ABILITIES_PACKET_FOV_SETTER = ReflectionHelper.getFinalSetter(ClientboundPlayerAbilitiesPacket.class, ReflectionMappingsInfo.ClientboundPlayerAbilitiesPacket_walkingSpeed);

    public static MethodHandle ENTITY_METADATA_LIST_SETTER = ReflectionHelper.getFinalSetterForFirstOfType(ClientboundSetEntityDataPacket.class, List.class); // packedItems

    public static Field ENTITY_TRACKER_ENTRY_GETTER = ReflectionHelper.getFields(ChunkMap.TrackedEntity.class).getFirstOfType(ServerEntity.class);

    public static MethodHandle CANVAS_GET_BUFFER = ReflectionHelper.getMethodHandle(CraftMapCanvas.class, "getBuffer");
    public static Field MAPVIEW_WORLDMAP = ReflectionHelper.getFields(CraftMapView.class).get("worldMap");

    public static MethodHandle BLOCK_ENTITY_DATA_PACKET_CONSTRUCTOR = ReflectionHelper.getConstructor(ClientboundBlockEntityDataPacket.class, BlockPos.class, BlockEntityType.class, net.minecraft.nbt.CompoundTag.class);

    public static EntityDataAccessor<Optional<Component>> ENTITY_CUSTOM_NAME_METADATA;
    public static EntityDataAccessor<Boolean> ENTITY_CUSTOM_NAME_VISIBLE_METADATA;

    static {
        try {
            ENTITY_CUSTOM_NAME_METADATA = ReflectionHelper.getFieldValue(net.minecraft.world.entity.Entity.class, ReflectionMappingsInfo.Entity_DATA_CUSTOM_NAME, null);
            ENTITY_CUSTOM_NAME_VISIBLE_METADATA = ReflectionHelper.getFieldValue(net.minecraft.world.entity.Entity.class, ReflectionMappingsInfo.Entity_DATA_CUSTOM_NAME_VISIBLE, null);
        }
        catch (Throwable ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void setFakeAbsorption(Player player, float value) {
        SynchedEntityData dw = new SynchedEntityData(null);
        dw.define(ENTITY_HUMAN_DATA_WATCHER_ABSORPTION, value);
        send(player, new ClientboundSetEntityDataPacket(player.getEntityId(), dw, true));
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
        send(player, new ClientboundAddMobPacket(entity));
        send(player, new ClientboundSetCameraPacket(entity));
        ((CraftServer) Bukkit.getServer()).getHandle().respawn(((CraftPlayer) player).getHandle(),
                ((CraftWorld) player.getWorld()).getHandle(), true, player.getLocation(), false);
    }

    @Override
    public void showBlockAction(Player player, Location location, int action, int state) {
        BlockPos position = new BlockPos(location.getX(), location.getY(), location.getZ());
        Block block = ((CraftWorld) location.getWorld()).getHandle().getBlockState(position).getBlock();
        send(player, new ClientboundBlockEventPacket(position, block, action, state));
    }

    @Override
    public void showBlockCrack(Player player, int id, Location location, int progress) {
        BlockPos position = new BlockPos(location.getX(), location.getY(), location.getZ());
        send(player, new ClientboundBlockDestructionPacket(id, position, progress));
    }

    @Override
    public void showTileEntityData(Player player, Location location, int action, CompoundTag compoundTag) {
        BlockPos position = new BlockPos(location.getX(), location.getY(), location.getZ());
        try {
            ClientboundBlockEntityDataPacket packet = (ClientboundBlockEntityDataPacket) BLOCK_ENTITY_DATA_PACKET_CONSTRUCTOR.invoke(position, action, ((CompoundTagImpl) compoundTag).toNMSTag());
            send(player, packet);
        }
        catch (Throwable ex) {
            Debug.echoError(ex);
        }
    }

    @Override
    public void showBannerUpdate(Player player, Location location, List<Pattern> patterns) {
        List<CompoundTag> nbtPatterns = new ArrayList<>();
        for (Pattern pattern : patterns) {
            nbtPatterns.add(NMSHandler.instance
                    .createCompoundTag(new HashMap<>())
                    .createBuilder()
                    .putInt("Color", pattern.getColor().getDyeData())
                    .putString("Pattern", pattern.getPattern().getIdentifier())
                    .build());
        }
        CompoundTag compoundTag = NMSHandler.blockHelper.getNbtData(location.getBlock())
                .createBuilder()
                .put("Patterns", new JNBTListTag(CompoundTag.class, nbtPatterns))
                .build();
        showTileEntityData(player, location, 3, compoundTag);
    }

    @Override
    public void showTabListHeaderFooter(Player player, String header, String footer) {
        Component cHeader = Handler.componentToNMS(FormattedTextHelper.parse(header, ChatColor.WHITE));
        Component cFooter = Handler.componentToNMS(FormattedTextHelper.parse(footer, ChatColor.WHITE));
        ClientboundTabListPacket packet = new ClientboundTabListPacket(cHeader, cFooter);
        send(player, packet);
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
    public void resetEquipment(Player player, LivingEntity entity) {
        EntityEquipment equipment = entity.getEquipment();
        List<Pair<EquipmentSlot, net.minecraft.world.item.ItemStack>> pairList = new ArrayList<>();
        pairList.add(new Pair<>(EquipmentSlot.MAINHAND, CraftItemStack.asNMSCopy(equipment.getItemInMainHand())));
        pairList.add(new Pair<>(EquipmentSlot.OFFHAND, CraftItemStack.asNMSCopy(equipment.getItemInOffHand())));
        pairList.add(new Pair<>(EquipmentSlot.HEAD, CraftItemStack.asNMSCopy(equipment.getHelmet())));
        pairList.add(new Pair<>(EquipmentSlot.CHEST, CraftItemStack.asNMSCopy(equipment.getChestplate())));
        pairList.add(new Pair<>(EquipmentSlot.LEGS, CraftItemStack.asNMSCopy(equipment.getLeggings())));
        pairList.add(new Pair<>(EquipmentSlot.FEET, CraftItemStack.asNMSCopy(equipment.getBoots())));
        send(player, new ClientboundSetEquipmentPacket(entity.getEntityId(), pairList));
    }

    @Override
    public void showHealth(Player player, float health, int food, float saturation) {
        send(player, new ClientboundSetHealthPacket(health, food, saturation));
    }

    @Override
    public void showMobHealth(Player player, LivingEntity mob, double health, double maxHealth) {
        AttributeInstance attr = new AttributeInstance(Attributes.MAX_HEALTH, (a) -> { });
        attr.setBaseValue(maxHealth);
        send(player, new ClientboundUpdateAttributesPacket(mob.getEntityId(), Collections.singletonList(attr)));
        FriendlyByteBuf healthData = new FriendlyByteBuf(Unpooled.buffer());
        healthData.writeVarInt(mob.getEntityId());
        healthData.writeByte(9); // health id
        healthData.writeVarInt(2); // type = float
        healthData.writeFloat((float) health);
        healthData.writeByte(255); // Mark end of packet
        send(player, new ClientboundSetEntityDataPacket(healthData));
    }

    @Override
    public void resetHealth(Player player) {
        showHealth(player, (float) player.getHealth(), player.getFoodLevel(), player.getSaturation());
    }

    @Override
    public void showSignEditor(Player player, Location location) {
        LocationTag fakeSign = new LocationTag(player.getLocation());
        fakeSign.setY(0);
        FakeBlock.showFakeBlockTo(Collections.singletonList(new PlayerTag(player)), fakeSign, new MaterialTag(Material.OAK_WALL_SIGN), new DurationTag(1), true);
        BlockPos pos = new BlockPos(fakeSign.getX(), 0, fakeSign.getZ());
        ((DenizenNetworkManagerImpl) ((CraftPlayer) player).getHandle().connection.connection).packetListener.fakeSignExpected = pos;
        send(player, new ClientboundOpenSignEditorPacket(pos));
    }

    @Override
    public void forceSpectate(Player player, Entity entity) {
        send(player, new ClientboundSetCameraPacket(((CraftEntity) entity).getHandle()));
    }

    public static void forceRespawnPlayerEntity(Entity entity, Player viewer) {
        ChunkMap tracker = ((ServerLevel) ((CraftEntity) entity).getHandle().level).getChunkSource().chunkMap;
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
                    send(player, new ClientboundPlayerInfoPacket(ClientboundPlayerInfoPacket.Action.UPDATE_DISPLAY_NAME, ((CraftPlayer) player).getHandle()));
                }
                else {
                    // For player entities, force a respawn packet and let the dynamic intercept correct the details
                    forceRespawnPlayerEntity(entity, player);
                }
                return;
            }
            SynchedEntityData fakeData = new SynchedEntityData(((CraftEntity) entity).getHandle());
            ClientboundSetEntityDataPacket packet = new ClientboundSetEntityDataPacket(entity.getEntityId(), fakeData, false);
            List<SynchedEntityData.DataItem<?>> list = new ArrayList<>();
            list.add(new SynchedEntityData.DataItem<>(ENTITY_CUSTOM_NAME_METADATA, Optional.of(Handler.componentToNMS(FormattedTextHelper.parse(name, ChatColor.WHITE)))));
            list.add(new SynchedEntityData.DataItem<>(ENTITY_CUSTOM_NAME_VISIBLE_METADATA, true));
            ENTITY_METADATA_LIST_SETTER.invoke(packet, list);
            send(player, packet);
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
        SynchedEntityData dw = new SynchedEntityData(null);
        dw.define(ENTITY_DATA_WATCHER_FLAGS, ((CraftEntity) entity).getHandle().getEntityData().get(ENTITY_DATA_WATCHER_FLAGS));
        send(player, new ClientboundSetEntityDataPacket(entity.getEntityId(), dw, true));
    }

    @Override
    public void sendEntityEffect(Player player, Entity entity, EntityEffect effect) {
        send(player, new ClientboundEntityEventPacket(((CraftEntity) entity).getHandle(), effect.getData()));
    }

    @Override
    public int getPacketStats(Player player, boolean sent) {
        DenizenNetworkManagerImpl netMan = (DenizenNetworkManagerImpl) ((CraftPlayer) player).getHandle().connection.connection;
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
        ResourceLocation packetKey = new ResourceLocation("minecraft", "debug/game_test_add_marker");
        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
        buf.writeBlockPos(new BlockPos(location.getBlockX(), location.getBlockY(), location.getBlockZ()));
        int colorInt = color.blue | (color.green << 8) | (color.red << 16) | (color.alpha << 24);
        buf.writeInt(colorInt);
        buf.writeByteArray(name.getBytes(StandardCharsets.UTF_8));
        buf.writeInt(time);
        ClientboundCustomPayloadPacket packet = new ClientboundCustomPayloadPacket(packetKey, buf);
        send(player, packet);
    }

    @Override
    public void clearDebugTestMarker(Player player) {
        ResourceLocation packetKey = new ResourceLocation("minecraft", "debug/game_test_clear");
        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
        ClientboundCustomPayloadPacket packet = new ClientboundCustomPayloadPacket(packetKey, buf);
        send(player, packet);
    }

    @Override
    public void sendBrand(Player player, String brand) {
        ResourceLocation packetKey = new ResourceLocation("minecraft", "brand");
        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
        buf.writeUtf(brand);
        ClientboundCustomPayloadPacket packet = new ClientboundCustomPayloadPacket(packetKey, buf);
        send(player, packet);
    }

    @Override
    public void sendCollectItemEntity(Player player, Entity taker, Entity item, int amount) {
        ClientboundTakeItemEntityPacket packet = new ClientboundTakeItemEntityPacket(item.getEntityId(), taker.getEntityId(), amount);
        send(player, packet);
    }

    public static void send(Player player, Packet packet) {
        ((CraftPlayer) player).getHandle().connection.send(packet);
    }
}

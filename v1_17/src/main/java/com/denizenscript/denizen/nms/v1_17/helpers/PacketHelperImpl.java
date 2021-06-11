package com.denizenscript.denizen.nms.v1_17.helpers;

import com.denizenscript.denizen.nms.NMSHandler;
import com.denizenscript.denizen.nms.v1_17.impl.SidebarImpl;
import com.denizenscript.denizen.nms.v1_17.impl.network.handlers.DenizenNetworkManagerImpl;
import com.denizenscript.denizen.objects.LocationTag;
import com.denizenscript.denizen.objects.MaterialTag;
import com.denizenscript.denizen.objects.PlayerTag;
import com.denizenscript.denizen.utilities.Utilities;
import com.denizenscript.denizen.utilities.blocks.FakeBlock;
import com.denizenscript.denizen.utilities.maps.MapImage;
import com.denizenscript.denizencore.objects.core.DurationTag;
import com.denizenscript.denizencore.utilities.ReflectionHelper;
import com.denizenscript.denizen.nms.v1_17.Handler;
import com.denizenscript.denizen.nms.v1_17.impl.jnbt.CompoundTagImpl;
import com.denizenscript.denizen.nms.interfaces.PacketHelper;
import com.denizenscript.denizen.nms.util.jnbt.CompoundTag;
import com.denizenscript.denizen.nms.util.jnbt.JNBTListTag;
import com.denizenscript.denizen.utilities.FormattedTextHelper;
import com.denizenscript.denizen.utilities.debugging.Debug;
import com.mojang.datafixers.util.Pair;
import net.md_5.bungee.api.ChatColor;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.IChatBaseComponent;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.*;
import net.minecraft.network.syncher.DataWatcher;
import net.minecraft.network.syncher.DataWatcherObject;
import net.minecraft.server.level.EntityTrackerEntry;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.EnumHand;
import net.minecraft.world.entity.EntityLiving;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.EnumItemSlot;
import net.minecraft.world.entity.monster.EntityCaveSpider;
import net.minecraft.world.entity.monster.EntityCreeper;
import net.minecraft.world.entity.monster.EntityEnderman;
import net.minecraft.world.entity.monster.EntitySpider;
import net.minecraft.world.entity.player.EntityHuman;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.TileEntitySign;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.level.saveddata.maps.WorldMap;
import net.minecraft.world.scores.ScoreboardTeam;
import net.minecraft.world.scores.ScoreboardTeamBase;
import org.bukkit.Bukkit;
import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.block.banner.Pattern;
import org.bukkit.craftbukkit.v1_17_R1.CraftEquipmentSlot;
import org.bukkit.craftbukkit.v1_17_R1.CraftServer;
import org.bukkit.craftbukkit.v1_17_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_17_R1.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_17_R1.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_17_R1.inventory.CraftItemStack;
import org.bukkit.craftbukkit.v1_17_R1.map.CraftMapCanvas;
import org.bukkit.craftbukkit.v1_17_R1.map.CraftMapView;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.map.MapCanvas;
import org.bukkit.map.MapPalette;

import java.lang.invoke.MethodHandle;
import java.lang.reflect.Field;
import java.util.*;

public class PacketHelperImpl implements PacketHelper {

    public static final DataWatcherObject<Float> ENTITY_HUMAN_DATA_WATCHER_ABSORPTION = ReflectionHelper.getFieldValue(EntityHuman.class, "c", null);

    public static final DataWatcherObject<Byte> ENTITY_DATA_WATCHER_FLAGS = ReflectionHelper.getFieldValue(net.minecraft.world.entity.Entity.class, "S", null);

    public static final MethodHandle ABILITIES_PACKET_FOV_SETTER = ReflectionHelper.getFinalSetter(PacketPlayOutAbilities.class, "f");

    @Override
    public void setFakeAbsorption(Player player, float value) {
        DataWatcher dw = new DataWatcher(null);
        dw.register(ENTITY_HUMAN_DATA_WATCHER_ABSORPTION, value);
        send(player, new PacketPlayOutEntityMetadata(player.getEntityId(), dw, true));
    }

    @Override
    public void resetWorldBorder(Player player) {
        WorldBorder wb = ((CraftWorld) player.getWorld()).getHandle().getWorldBorder();
        send(player, new PacketPlayOutWorldBorder(wb, PacketPlayOutWorldBorder.EnumWorldBorderAction.INITIALIZE));
    }

    @Override
    public void setWorldBorder(Player player, Location center, double size, double currSize, long time, int warningDistance, int warningTime) {
        WorldBorder wb = new WorldBorder();

        wb.world = ((CraftWorld) player.getWorld()).getHandle();
        wb.setCenter(center.getX(), center.getZ());
        wb.setWarningDistance(warningDistance);
        wb.setWarningTime(warningTime);

        if (time > 0) {
            wb.transitionSizeBetween(currSize, size, time);
        }
        else {
            wb.setSize(size);
        }

        send(player, new PacketPlayOutWorldBorder(wb, PacketPlayOutWorldBorder.EnumWorldBorderAction.INITIALIZE));
    }

    @Override
    public void setSlot(Player player, int slot, ItemStack itemStack, boolean playerOnly) {
        int windowId = playerOnly ? 0 : ((CraftPlayer) player).getHandle().activeContainer.windowId;
        send(player, new PacketPlayOutSetSlot(windowId, slot, CraftItemStack.asNMSCopy(itemStack)));
    }

    @Override
    public void setFieldOfView(Player player, float fov) {
        PacketPlayOutAbilities packet = new PacketPlayOutAbilities(((CraftPlayer) player).getHandle().abilities);
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
        ((CraftPlayer) player).getHandle().connection.a(
                new PacketPlayInClientCommand(PacketPlayInClientCommand.EnumClientCommand.PERFORM_RESPAWN));
    }

    @Override
    public void setVision(Player player, EntityType entityType) {
        final EntityLiving entity;
        if (entityType == EntityType.CREEPER) {
            entity = new EntityCreeper(EntityTypes.CREEPER, ((CraftWorld) player.getWorld()).getHandle());
        }
        else if (entityType == EntityType.SPIDER) {
            entity = new EntitySpider(EntityTypes.SPIDER, ((CraftWorld) player.getWorld()).getHandle());
        }
        else if (entityType == EntityType.CAVE_SPIDER) {
            entity = new EntityCaveSpider(EntityTypes.CAVE_SPIDER, ((CraftWorld) player.getWorld()).getHandle());
        }
        else if (entityType == EntityType.ENDERMAN) {
            entity = new EntityEnderman(EntityTypes.ENDERMAN, ((CraftWorld) player.getWorld()).getHandle());
        }
        else {
            return;
        }

        // Spectating an entity then immediately respawning the player prevents a client shader update,
        // allowing the player to retain whatever vision the mob they spectated had.
        send(player, new PacketPlayOutSpawnEntityLiving(entity));
        send(player, new PacketPlayOutCamera(entity));
        ((CraftServer) Bukkit.getServer()).getHandle().moveToWorld(((CraftPlayer) player).getHandle(),
                ((CraftWorld) player.getWorld()).getHandle(), true, player.getLocation(), false);
    }

    @Override
    public void showDemoScreen(Player player) {
        send(player, new PacketPlayOutGameStateChange(PacketPlayOutGameStateChange.f, 0f));
    }

    @Override
    public void showBlockAction(Player player, Location location, int action, int state) {
        BlockPos position = new BlockPos(location.getX(), location.getY(), location.getZ());
        Block block = ((CraftWorld) location.getWorld()).getHandle().getType(position).getBlock();
        send(player, new PacketPlayOutBlockAction(position, block, action, state));
    }

    @Override
    public void showBlockCrack(Player player, int id, Location location, int progress) {
        BlockPos position = new BlockPos(location.getX(), location.getY(), location.getZ());
        send(player, new PacketPlayOutBlockBreakAnimation(id, position, progress));
    }

    @Override
    public void showTileEntityData(Player player, Location location, int action, CompoundTag compoundTag) {
        BlockPos position = new BlockPos(location.getX(), location.getY(), location.getZ());
        send(player, new PacketPlayOutTileEntityData(position, action, ((CompoundTagImpl) compoundTag).toNMSTag()));
    }

    @Override
    public void showBannerUpdate(Player player, Location location, DyeColor base, List<Pattern> patterns) {
        List<CompoundTag> nbtPatterns = new ArrayList<>();
        for (Pattern pattern : patterns) {
            nbtPatterns.add(NMSHandler.getInstance()
                    .createCompoundTag(new HashMap<>())
                    .createBuilder()
                    .putInt("Color", pattern.getColor().getDyeData())
                    .putString("Pattern", pattern.getPattern().getIdentifier())
                    .build());
        }
        CompoundTag compoundTag = NMSHandler.getBlockHelper().getNbtData(location.getBlock())
                .createBuilder()
                .put("Patterns", new JNBTListTag(CompoundTag.class, nbtPatterns))
                .build();
        showTileEntityData(player, location, 3, compoundTag);
    }

    @Override
    public void showTabListHeaderFooter(Player player, String header, String footer) {
        PacketPlayOutPlayerListHeaderFooter packet = new PacketPlayOutPlayerListHeaderFooter();
        packet.header = Handler.componentToNMS(FormattedTextHelper.parse(header, ChatColor.WHITE));
        packet.footer = Handler.componentToNMS(FormattedTextHelper.parse(footer, ChatColor.WHITE));
        send(player, packet);
    }

    @Override
    public void resetTabListHeaderFooter(Player player) {
        showTabListHeaderFooter(player, "", "");
    }

    @Override
    public void showTitle(Player player, String title, String subtitle, int fadeInTicks, int stayTicks, int fadeOutTicks) {
        send(player, new PacketPlayOutTitle(fadeInTicks, stayTicks, fadeOutTicks));
        if (title != null) {
            send(player, new PacketPlayOutTitle(PacketPlayOutTitle.EnumTitleAction.TITLE, Handler.componentToNMS(FormattedTextHelper.parse(title, ChatColor.WHITE))));
        }
        if (subtitle != null) {
            send(player, new PacketPlayOutTitle(PacketPlayOutTitle.EnumTitleAction.SUBTITLE, Handler.componentToNMS(FormattedTextHelper.parse(subtitle, ChatColor.WHITE))));
        }
    }

    @Override
    public void showEquipment(Player player, LivingEntity entity, EquipmentSlot equipmentSlot, ItemStack itemStack) {
        Pair<EnumItemSlot, net.minecraft.world.item.ItemStack> pair = new Pair<>(CraftEquipmentSlot.getNMS(equipmentSlot), CraftItemStack.asNMSCopy(itemStack));
        ArrayList<Pair<EnumItemSlot, net.minecraft.world.item.ItemStack>> pairList = new ArrayList<>();
        pairList.add(pair);
        send(player, new PacketPlayOutEntityEquipment(entity.getEntityId(), pairList));
    }

    @Override
    public void resetEquipment(Player player, LivingEntity entity) {
        EntityEquipment equipment = entity.getEquipment();
        ArrayList<Pair<EnumItemSlot, net.minecraft.world.item.ItemStack>> pairList = new ArrayList<>();
        pairList.add(new Pair<>(EnumItemSlot.MAINHAND, CraftItemStack.asNMSCopy(equipment.getItemInMainHand())));
        pairList.add(new Pair<>(EnumItemSlot.OFFHAND, CraftItemStack.asNMSCopy(equipment.getItemInOffHand())));
        pairList.add(new Pair<>(EnumItemSlot.HEAD, CraftItemStack.asNMSCopy(equipment.getHelmet())));
        pairList.add(new Pair<>(EnumItemSlot.CHEST, CraftItemStack.asNMSCopy(equipment.getChestplate())));
        pairList.add(new Pair<>(EnumItemSlot.LEGS, CraftItemStack.asNMSCopy(equipment.getLeggings())));
        pairList.add(new Pair<>(EnumItemSlot.FEET, CraftItemStack.asNMSCopy(equipment.getBoots())));
        send(player, new PacketPlayOutEntityEquipment(entity.getEntityId(), pairList));
    }

    @Override
    public void openBook(Player player, EquipmentSlot hand) {
        send(player, new PacketPlayOutOpenBook(hand == EquipmentSlot.OFF_HAND ? EnumHand.OFF_HAND : EnumHand.MAIN_HAND));
    }

    @Override
    public void showHealth(Player player, float health, int food, float saturation) {
        send(player, new PacketPlayOutUpdateHealth(health, food, saturation));
    }

    @Override
    public void resetHealth(Player player) {
        showHealth(player, (float) player.getHealth(), player.getFoodLevel(), player.getSaturation());
    }

    @Override
    public void showExperience(Player player, float experience, int level) {
        send(player, new PacketPlayOutExperience(experience, 0, level));
    }

    @Override
    public void resetExperience(Player player) {
        showExperience(player, player.getExp(), player.getLevel());
    }

    @Override
    public boolean showSignEditor(Player player, Location location) {
        if (location == null) {
            LocationTag fakeSign = new LocationTag(player.getLocation());
            fakeSign.setY(0);
            FakeBlock.showFakeBlockTo(Collections.singletonList(new PlayerTag(player)), fakeSign, new MaterialTag(org.bukkit.Material.OAK_WALL_SIGN), new DurationTag(1));
            BlockPos pos = new BlockPos(fakeSign.getX(), 0, fakeSign.getZ());
            ((DenizenNetworkManagerImpl) ((CraftPlayer) player).getHandle().connection.networkManager).packetListener.fakeSignExpected = pos;
            send(player, new PacketPlayOutOpenSignEditor(pos));
            return true;
        }
        BlockEntity tileEntity = ((CraftWorld) location.getWorld()).getHandle().getTileEntity(new BlockPos(location.getBlockX(),
                location.getBlockY(), location.getBlockZ()));
        if (tileEntity instanceof TileEntitySign) {
            TileEntitySign sign = (TileEntitySign) tileEntity;
            // Prevent client crashing by sending current state of the sign
            send(player, sign.getUpdatePacket());
            sign.isEditable = true;
            sign.a((EntityHuman) ((CraftPlayer) player).getHandle());
            send(player, new PacketPlayOutOpenSignEditor(sign.getPosition()));
            return true;
        }
        else {
            return false;
        }
    }

    @Override
    public void forceSpectate(Player player, Entity entity) {
        send(player, new PacketPlayOutCamera(((CraftEntity) entity).getHandle()));
    }

    public static MethodHandle ENTITY_METADATA_EID_SETTER = ReflectionHelper.getFinalSetter(PacketPlayOutEntityMetadata.class, "a");
    public static MethodHandle ENTITY_METADATA_LIST_SETTER = ReflectionHelper.getFinalSetter(PacketPlayOutEntityMetadata.class, "b");

    public static DataWatcherObject<Optional<IChatBaseComponent>> ENTITY_CUSTOM_NAME_METADATA;
    public static DataWatcherObject<Boolean> ENTITY_CUSTOM_NAME_VISIBLE_METADATA;

    static {
        try {
            ENTITY_CUSTOM_NAME_METADATA = ReflectionHelper.getFieldValue(net.minecraft.world.entity.Entity.class, "aq", null);
            ENTITY_CUSTOM_NAME_VISIBLE_METADATA = ReflectionHelper.getFieldValue(net.minecraft.world.entity.Entity.class, "ar", null);
        }
        catch (Throwable ex) {
            ex.printStackTrace();
        }
    }

    public static Field ENTITY_TRACKER_ENTRY_GETTER = ReflectionHelper.getFields(PlayerChunkMap.EntityTracker.class).get("trackerEntry");

    @Override
    public void sendRename(Player player, Entity entity, String name, boolean listMode) {
        try {
            if (entity.getType() == EntityType.PLAYER) {
                if (listMode) {
                    send(player, new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.UPDATE_DISPLAY_NAME, ((CraftPlayer) player).getHandle()));
                }
                else {
                    // For player entities, force a respawn packet and let the dynamic intercept correct the details
                    ChunkMap tracker = ((ServerLevel) ((CraftEntity) entity).getHandle().world).getChunkProvider().chunkMap;
                    ChunkMap.EntityTracker entityTracker = tracker.trackedEntities.get(entity.getEntityId());
                    if (entityTracker != null) {
                        try {
                            EntityTrackerEntry entry = (EntityTrackerEntry) ENTITY_TRACKER_ENTRY_GETTER.get(entityTracker);
                            if (entry != null) {
                                entry.a(((CraftPlayer) player).getHandle());
                                entry.b(((CraftPlayer) player).getHandle());
                            }
                        }
                        catch (Throwable ex) {
                            Debug.echoError(ex);
                        }
                    }
                }
                return;
            }
            PacketPlayOutEntityMetadata packet = new PacketPlayOutEntityMetadata();
            ENTITY_METADATA_EID_SETTER.invoke(packet, entity.getEntityId());
            List<DataWatcher.Item<?>> list = new ArrayList<>();
            list.add(new DataWatcher.Item<>(ENTITY_CUSTOM_NAME_METADATA, Optional.of(Handler.componentToNMS(FormattedTextHelper.parse(name, ChatColor.WHITE)))));
            list.add(new DataWatcher.Item<>(ENTITY_CUSTOM_NAME_VISIBLE_METADATA, true));
            ENTITY_METADATA_LIST_SETTER.invoke(packet, list);
            send(player, packet);
        }
        catch (Throwable ex) {
            Debug.echoError(ex);
        }
    }

    public static HashMap<UUID, HashMap<UUID, ScoreboardTeam>> noCollideTeamMap = new HashMap<>();

    @Override
    public void generateNoCollideTeam(Player player, UUID noCollide) {
        removeNoCollideTeam(player, noCollide);
        ScoreboardTeam team = new ScoreboardTeam(SidebarImpl.dummyScoreboard, Utilities.generateRandomColors(8));
        team.getPlayerNameSet().add(noCollide.toString());
        team.setCollisionRule(ScoreboardTeamBase.EnumTeamPush.NEVER);
        HashMap<UUID, ScoreboardTeam> map = noCollideTeamMap.computeIfAbsent(player.getUniqueId(), k -> new HashMap<>());
        map.put(noCollide, team);
        send(player, new PacketPlayOutScoreboardTeam(team, 0));
    }

    @Override
    public void removeNoCollideTeam(Player player, UUID noCollide) {
        if (noCollide == null || !player.isOnline()) {
            noCollideTeamMap.remove(player.getUniqueId());
            return;
        }
        HashMap<UUID, ScoreboardTeam> map = noCollideTeamMap.get(player.getUniqueId());
        if (map == null) {
            return;
        }
        ScoreboardTeam team = map.remove(noCollide);
        if (team != null) {
            send(player, new PacketPlayOutScoreboardTeam(team, 1));
        }
        if (map.isEmpty()) {
            noCollideTeamMap.remove(player.getUniqueId());
        }
    }

    @Override
    public void sendEntityMetadataFlagsUpdate(Player player, Entity entity) {
        DataWatcher dw = new DataWatcher(null);
        dw.register(ENTITY_DATA_WATCHER_FLAGS, ((CraftEntity) entity).getHandle().getDataWatcher().get(ENTITY_DATA_WATCHER_FLAGS));
        send(player, new PacketPlayOutEntityMetadata(entity.getEntityId(), dw, true));
    }

    @Override
    public void sendEntityEffect(Player player, Entity entity, byte effectId) {
        send(player, new PacketPlayOutEntityStatus(((CraftEntity) entity).getHandle(), effectId));
    }

    @Override
    public int getPacketStats(Player player, boolean sent) {
        DenizenNetworkManagerImpl netMan = (DenizenNetworkManagerImpl) ((CraftPlayer) player).getHandle().connection.networkManager;
        return sent ? netMan.packetsSent : netMan.packetsReceived;
    }

    public static MethodHandle CANVAS_GET_BUFFER = ReflectionHelper.getMethodHandle(CraftMapCanvas.class, "getBuffer");
    public static Field MAPVIEW_WORLDMAP = ReflectionHelper.getFields(CraftMapView.class).get("worldMap");

    @Override
    public void setMapData(MapCanvas canvas, byte[] bytes, int x, int y, MapImage image) {
        if (x > 127 || y > 127) {
            return;
        }
        try {
            int width = Math.min(image.width, 128 - x), height = Math.min(image.height, 128 - y);
            byte[] buffer = (byte[]) CANVAS_GET_BUFFER.invoke(canvas);
            for (int x2 = x < 0 ? -x : 0; x2 < width; ++x2) {
                for (int y2 = y < 0 ? -y : 0; y2 < height; ++y2) {
                    byte p = bytes[y2 * image.width + x2];
                    if (p != MapPalette.TRANSPARENT) {
                        buffer[(y2 + y) * 128 + (x2 + x)] = p;
                    }
                }
            }
            // Flag the whole image as dirty
            WorldMap map = (WorldMap) MAPVIEW_WORLDMAP.get(canvas.getMapView());
            map.flagDirty(Math.max(x, 0), Math.max(y, 0));
            map.flagDirty(width + x - 1, height + y - 1);
        }
        catch (Throwable ex) {
            Debug.echoError(ex);
        }
    }

    public static void send(Player player, Packet packet) {
        ((CraftPlayer) player).getHandle().connection.send(packet);
    }
}

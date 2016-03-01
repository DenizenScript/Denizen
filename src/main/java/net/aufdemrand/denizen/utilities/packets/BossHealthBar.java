package net.aufdemrand.denizen.utilities.packets;


import net.aufdemrand.denizen.utilities.DenizenAPI;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.minecraft.server.v1_9_R1.*;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.lang.reflect.Field;
import java.util.*;

// Retrieved from https://forums.bukkit.org/threads/tutorial-utilizing-the-boss-health-bar.158018/
// Modified for usage by Denizen

// Original code by chasechocolate
// Modified by ftbastler for Minecraft 1.7

public class BossHealthBar {
    public static final int ENTITY_ID = 1234567;
    private static HashMap<String, Boolean> hasHealthBar = new HashMap<String, Boolean>();
    private static ArrayList<Player> playersWithHealthBar = new ArrayList<Player>();

    // Keep track of fields to avoid unnecessarily getting them repeatedly
    private static final Field spawn_entityId, spawn_entityType, spawn_locationX, spawn_locationY, spawn_locationZ,
            spawn_velocityX, spawn_velocityY, spawn_velocityZ, spawn_yaw, spawn_pitch, spawn_headPitch, spawn_data;
    private static final Field destroy_entityList;
    private static final Field metadata_entityId, metadata_data;
    private static final Field ccommand_command;
    private static final Field teleport_entityId, teleport_x, teleport_y, teleport_z,
            teleport_yaw, teleport_pitch, teleport_onGround;

    private static final HashSet<Material> ignoreAllBlocks = new HashSet<Material>(EnumSet.allOf(Material.class));
    private static final BukkitTask task = new BukkitRunnable() {
        @Override
        public void run() {
            for (int i = 0; i < playersWithHealthBar.size(); i++) {
                Player player = playersWithHealthBar.get(i);
                if (!player.isDead() && player.isValid()) {
                    PacketHelper.sendPacket(player, getTeleportPacket(player.getLocation().clone()
                            .add(player.getLocation().getDirection().multiply(30))));
                }
                else {
                    hasHealthBar.put(player.getName(), false);
                    playersWithHealthBar.remove(player);
                }
            }
        }
    }.runTaskTimer(DenizenAPI.getCurrentInstance(), 20, 20);

    static {
        Map<String, Field> fields = PacketHelper.registerFields(PacketPlayOutSpawnEntityLiving.class);
        spawn_entityId = fields.get("a");// TODO: 1.9
        spawn_entityType = fields.get("b");// TODO: 1.9
        spawn_locationX = fields.get("c");// TODO: 1.9
        spawn_locationY = fields.get("d");// TODO: 1.9
        spawn_locationZ = fields.get("e");// TODO: 1.9
        spawn_velocityX = fields.get("f");// TODO: 1.9
        spawn_velocityY = fields.get("g");// TODO: 1.9
        spawn_velocityZ = fields.get("h");// TODO: 1.9
        spawn_yaw = fields.get("i");// TODO: 1.9
        spawn_pitch = fields.get("j");// TODO: 1.9
        spawn_headPitch = fields.get("k");// TODO: 1.9
        spawn_data = fields.get("l");// TODO: 1.9

        fields = PacketHelper.registerFields(PacketPlayOutEntityDestroy.class);
        destroy_entityList = fields.get("a");// TODO: 1.9

        fields = PacketHelper.registerFields(PacketPlayOutEntityMetadata.class);
        metadata_entityId = fields.get("a");// TODO: 1.9
        metadata_data = fields.get("b");// TODO: 1.9

        fields = PacketHelper.registerFields(PacketPlayInClientCommand.class);
        ccommand_command = fields.get("a");// TODO: 1.9

        fields = PacketHelper.registerFields(PacketPlayOutEntityTeleport.class);
        teleport_entityId = fields.get("a");// TODO: 1.9
        teleport_x = fields.get("b");// TODO: 1.9
        teleport_y = fields.get("c");// TODO: 1.9
        teleport_z = fields.get("d");// TODO: 1.9
        teleport_yaw = fields.get("e");// TODO: 1.9
        teleport_pitch = fields.get("f");// TODO: 1.9
        teleport_onGround = fields.get("g");// TODO: 1.9
    }

    //Accessing packets
    @SuppressWarnings("deprecation")
    public static PacketPlayOutSpawnEntityLiving getMobPacket(String text, Location loc, int health) {
        PacketPlayOutSpawnEntityLiving mobPacket = new PacketPlayOutSpawnEntityLiving();
        try {
            spawn_entityId.set(mobPacket, ENTITY_ID);
            spawn_entityType.set(mobPacket, (byte) EntityType.WITHER.getTypeId());
            spawn_locationX.set(mobPacket, (int) Math.floor(loc.getBlockX() * 32.0D));
            spawn_locationY.set(mobPacket, -256 * 32);
            spawn_locationZ.set(mobPacket, (int) Math.floor(loc.getBlockZ() * 32.0D));
            spawn_velocityX.set(mobPacket, (byte) 0);
            spawn_velocityY.set(mobPacket, (byte) 0);
            spawn_velocityZ.set(mobPacket, (byte) 0);
            spawn_yaw.set(mobPacket, (byte) 0);
            spawn_pitch.set(mobPacket, (byte) 0);
            spawn_headPitch.set(mobPacket, (byte) 0);
            spawn_data.set(mobPacket, getWatcher(text, health));
        }
        catch (Exception e) {
            dB.echoError(e);
        }
        return mobPacket;
    }

    public static PacketPlayOutEntityDestroy getDestroyEntityPacket() {
        PacketPlayOutEntityDestroy destroyPacket = new PacketPlayOutEntityDestroy();
        try {
            destroy_entityList.set(destroyPacket, new int[]{ENTITY_ID});
        }
        catch (Exception e) {
            dB.echoError(e);
        }
        return destroyPacket;
    }

    public static PacketPlayOutEntityMetadata getMetadataPacket(DataWatcher watcher) {
        PacketPlayOutEntityMetadata metaPacket = new PacketPlayOutEntityMetadata();
        try {
            metadata_entityId.set(metaPacket, ENTITY_ID);
            metadata_data.set(metaPacket, watcher.c());
        }
        catch (Exception e) {
            dB.echoError(e);
        }
        return metaPacket;
    }

    public static PacketPlayOutEntityTeleport getTeleportPacket(Location location) {
        PacketPlayOutEntityTeleport teleportPacket = new PacketPlayOutEntityTeleport();
        try {
            teleport_entityId.set(teleportPacket, ENTITY_ID);
            teleport_x.set(teleportPacket, (int) Math.floor(location.getBlockX() * 32.0D));
            teleport_y.set(teleportPacket, (int) Math.floor(location.getBlockY() * 32.0D));
            teleport_z.set(teleportPacket, (int) Math.floor(location.getBlockZ() * 32.0D));
            teleport_yaw.set(teleportPacket, (byte) 0);
            teleport_pitch.set(teleportPacket, (byte) 0);
            teleport_onGround.set(teleportPacket, false);
        }
        catch (Exception e) {
            dB.echoError(e);
        }
        return teleportPacket;
    }

    public static PacketPlayInClientCommand getRespawnPacket() {
        PacketPlayInClientCommand ccommandPacket = new PacketPlayInClientCommand();
        try {
            ccommand_command.set(ccommandPacket, PacketPlayInClientCommand.EnumClientCommand.PERFORM_RESPAWN);
        }
        catch (Exception e) {
            dB.echoError(e);
        }
        return ccommandPacket;
    }

    public static DataWatcher getWatcher(String text, int health) {
        DataWatcher watcher = new DataWatcher(null);
// TODO: 1.9        watcher.a(0, (byte) 0x20); //Flags, 0x20 = invisible
// TODO: 1.9        watcher.a(2, text); //Entity name
// TODO: 1.9        watcher.a(3, (byte) 1); //Show name, 1 = show, 0 = don't show
// TODO: 1.9        watcher.a(6, (float) health * 1.5F); // Account for 1.8 switch from Ender Dragon to Wither by multiplying by 1.5
        return watcher;
    }

    //Other methods
    public static void displayTextBar(String text, final Player player, int health) {
        PacketPlayOutSpawnEntityLiving mobPacket = getMobPacket(text, player.getLocation(), health);
        PacketHelper.sendPacket(player, mobPacket);
        hasHealthBar.put(player.getName(), true);
        playersWithHealthBar.add(player);
    }

    public static void removeTextBar(Player player) {
        if (hasHealthBar.containsKey(player.getName())) {
            PacketPlayOutEntityDestroy destroyEntityPacket = getDestroyEntityPacket();
            PacketHelper.sendPacket(player, destroyEntityPacket);
            hasHealthBar.put(player.getName(), false);
            playersWithHealthBar.remove(player);
        }
    }

    public static void displayLoadingBar(final String text, final String completeText, final Player player, final int healthAdd, final long delay, final boolean loadUp) {
        PacketPlayOutSpawnEntityLiving mobPacket = getMobPacket(text, player.getLocation(), 200);
        PacketHelper.sendPacket(player, mobPacket);
        hasHealthBar.put(player.getName(), true);
        playersWithHealthBar.add(player);
        new BukkitRunnable() {
            int health = (loadUp ? 0 : 300);

            @Override
            public void run() {
                if ((loadUp ? health < 300 : health > 0)) {
                    DataWatcher watcher = getWatcher(text, health);
                    PacketPlayOutEntityMetadata metaPacket = getMetadataPacket(watcher);
                    PacketHelper.sendPacket(player, metaPacket);
                    if (loadUp) {
                        health += healthAdd;
                    }
                    else {
                        health -= healthAdd;
                    }
                }
                else {
                    DataWatcher watcher = getWatcher(text, (loadUp ? 300 : 0));
                    PacketPlayOutEntityMetadata metaPacket = getMetadataPacket(watcher);
                    PacketPlayOutEntityDestroy destroyEntityPacket = getDestroyEntityPacket();
                    PacketHelper.sendPacket(player, metaPacket);
                    PacketHelper.sendPacket(player, destroyEntityPacket);
                    hasHealthBar.put(player.getName(), false);
                    playersWithHealthBar.remove(player);
//Complete text
                    PacketPlayOutSpawnEntityLiving mobPacket = getMobPacket(completeText, player.getLocation(), 200);
                    PacketHelper.sendPacket(player, mobPacket);
                    hasHealthBar.put(player.getName(), true);
                    playersWithHealthBar.add(player);
                    DataWatcher watcher2 = getWatcher(completeText, 300);
                    PacketPlayOutEntityMetadata metaPacket2 = getMetadataPacket(watcher2);
                    PacketHelper.sendPacket(player, metaPacket2);
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            PacketPlayOutEntityDestroy destroyEntityPacket = getDestroyEntityPacket();
                            PacketHelper.sendPacket(player, destroyEntityPacket);
                            hasHealthBar.put(player.getName(), false);
                            playersWithHealthBar.remove(player);
                        }
                    }.runTaskLater(DenizenAPI.getCurrentInstance(), 40L);
                    this.cancel();
                }
            }
        }.runTaskTimer(DenizenAPI.getCurrentInstance(), delay, delay);
    }

    public static void displayLoadingBar(final String text, final String completeText, final Player player, final int secondsDelay, final boolean loadUp) {
        final int healthChangePerSecond = 300 / secondsDelay;
        displayLoadingBar(text, completeText, player, healthChangePerSecond, 20L, loadUp);
    }
}

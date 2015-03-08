package net.aufdemrand.denizen.utilities.packets;

import net.minecraft.server.v1_8_R2.PacketPlayInClientCommand;
import net.minecraft.server.v1_8_R2.PacketPlayOutEntityDestroy;
import net.minecraft.server.v1_8_R2.PacketPlayOutEntityMetadata;
import net.minecraft.server.v1_8_R2.PacketPlayOutSpawnEntityLiving;
import net.minecraft.server.v1_8_R2.DataWatcher;

import net.aufdemrand.denizen.utilities.DenizenAPI;
import net.aufdemrand.denizen.utilities.debugging.dB;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

// Retrieved from https://forums.bukkit.org/threads/tutorial-utilizing-the-boss-health-bar.158018/
// Modified for usage by Denizen

// Original code by chasechocolate
// Modified by ftbastler for Minecraft 1.7

public class BossHealthBar {
    public static final int ENTITY_ID = 1234567;
    private static HashMap<String, Boolean> hasHealthBar = new HashMap<String, Boolean>();

    // Keep track of fields to avoid unnecessarily getting them repeatedly
    private static final Field spawn_entityId, spawn_entityType, spawn_locationX, spawn_locationY, spawn_locationZ,
            spawn_velocityX, spawn_velocityY, spawn_velocityZ, spawn_yaw, spawn_pitch, spawn_headPitch, spawn_data;
    private static final Field destroy_entityList;
    private static final Field metadata_entityId, metadata_data;
    private static final Field ccommand_command;

    static {
        Map<String, Field> fields = PacketHelper.registerFields(PacketPlayOutSpawnEntityLiving.class);
        spawn_entityId = fields.get("a"); // TODO: Are these accurate (1.8.3)?
        spawn_entityType = fields.get("b");
        spawn_locationX = fields.get("c");
        spawn_locationY = fields.get("d");
        spawn_locationZ = fields.get("e");
        spawn_velocityX = fields.get("f");
        spawn_velocityY = fields.get("g");
        spawn_velocityZ = fields.get("h");
        spawn_yaw = fields.get("i");
        spawn_pitch = fields.get("j");
        spawn_headPitch = fields.get("k");
        spawn_data = fields.get("l");

        fields = PacketHelper.registerFields(PacketPlayOutEntityDestroy.class);
        destroy_entityList = fields.get("a");

        fields = PacketHelper.registerFields(PacketPlayOutEntityMetadata.class);
        metadata_entityId = fields.get("a");
        metadata_data = fields.get("b");

        fields = PacketHelper.registerFields(PacketPlayInClientCommand.class);
        ccommand_command = fields.get("a");
    }

    //Accessing packets
    @SuppressWarnings("deprecation")
    public static PacketPlayOutSpawnEntityLiving getMobPacket(String text, Location loc, int health) {
        PacketPlayOutSpawnEntityLiving mobPacket = new PacketPlayOutSpawnEntityLiving();
        try {
            spawn_entityId.set(mobPacket, ENTITY_ID);
            spawn_entityType.set(mobPacket, (byte) EntityType.ENDER_DRAGON.getTypeId());
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
        } catch (Exception e) {
            dB.echoError(e);
        }
        return mobPacket;
    }

    public static PacketPlayOutEntityDestroy getDestroyEntityPacket() {
        PacketPlayOutEntityDestroy destroyPacket = new PacketPlayOutEntityDestroy();
        try {
            destroy_entityList.set(destroyPacket, new int[]{ENTITY_ID});
        } catch (Exception e) {
            dB.echoError(e);
        }
        return destroyPacket;
    }

    public static PacketPlayOutEntityMetadata getMetadataPacket(DataWatcher watcher) {
        PacketPlayOutEntityMetadata metaPacket = new PacketPlayOutEntityMetadata();
        try {
            metadata_entityId.set(metaPacket, ENTITY_ID);
            metadata_data.set(metaPacket, watcher.c());
        } catch (Exception e) {
            dB.echoError(e);
        }
        return metaPacket;
    }

    public static PacketPlayInClientCommand getRespawnPacket() {
        PacketPlayInClientCommand ccommandPacket = new PacketPlayInClientCommand();
        try {
            ccommand_command.set(ccommandPacket, PacketPlayInClientCommand.EnumClientCommand.PERFORM_RESPAWN);
        } catch (Exception e) {
            dB.echoError(e);
        }
        return ccommandPacket;
    }

    public static DataWatcher getWatcher(String text, int health) {
        DataWatcher watcher = new DataWatcher(null);
        watcher.a(0, (byte) 0x20); //Flags, 0x20 = invisible
        watcher.a(2, text); //Entity name
        watcher.a(3, (byte) 1); //Show name, 1 = show, 0 = don't show
        watcher.a(6, (float) health);
        return watcher;
    }

    //Other methods
    public static void displayTextBar(String text, final Player player, int health) {
        PacketPlayOutSpawnEntityLiving mobPacket = getMobPacket(text, player.getLocation(), health);
        PacketHelper.sendPacket(player, mobPacket);
        hasHealthBar.put(player.getName(), true);
    }

    public static void removeTextBar(Player player) {
        if (hasHealthBar.containsKey(player.getName())) {
            PacketPlayOutEntityDestroy destroyEntityPacket = getDestroyEntityPacket();
            PacketHelper.sendPacket(player, destroyEntityPacket);
            hasHealthBar.put(player.getName(), false);
        }
    }

    public static void displayLoadingBar(final String text, final String completeText, final Player player, final int healthAdd, final long delay, final boolean loadUp) {
        PacketPlayOutSpawnEntityLiving mobPacket = getMobPacket(text, player.getLocation(), 200);
        PacketHelper.sendPacket(player, mobPacket);
        hasHealthBar.put(player.getName(), true);
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
                    } else {
                        health -= healthAdd;
                    }
                } else {
                    DataWatcher watcher = getWatcher(text, (loadUp ? 300 : 0));
                    PacketPlayOutEntityMetadata metaPacket = getMetadataPacket(watcher);
                    PacketPlayOutEntityDestroy destroyEntityPacket = getDestroyEntityPacket();
                    PacketHelper.sendPacket(player, metaPacket);
                    PacketHelper.sendPacket(player, destroyEntityPacket);
                    hasHealthBar.put(player.getName(), false);
//Complete text
                    PacketPlayOutSpawnEntityLiving mobPacket = getMobPacket(completeText, player.getLocation(), 200);
                    PacketHelper.sendPacket(player, mobPacket);
                    hasHealthBar.put(player.getName(), true);
                    DataWatcher watcher2 = getWatcher(completeText, 300);
                    PacketPlayOutEntityMetadata metaPacket2 = getMetadataPacket(watcher2);
                    PacketHelper.sendPacket(player, metaPacket2);
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            PacketPlayOutEntityDestroy destroyEntityPacket = getDestroyEntityPacket();
                            PacketHelper.sendPacket(player, destroyEntityPacket);
                            hasHealthBar.put(player.getName(), false);
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

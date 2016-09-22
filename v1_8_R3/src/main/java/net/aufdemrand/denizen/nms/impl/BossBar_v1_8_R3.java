package net.aufdemrand.denizen.nms.impl;

import net.aufdemrand.denizen.nms.NMSHandler;
import net.aufdemrand.denizen.nms.helpers.PacketHelper_v1_8_R3;
import net.minecraft.server.v1_8_R3.EntityWither;
import net.minecraft.server.v1_8_R3.PacketPlayOutEntityDestroy;
import net.minecraft.server.v1_8_R3.PacketPlayOutEntityTeleport;
import net.minecraft.server.v1_8_R3.PacketPlayOutSpawnEntityLiving;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

public class BossBar_v1_8_R3 {

    private static final Map<String, Boolean> hasHealthBar = new HashMap<String, Boolean>();
    private static final List<Player> playersWithHealthBar = new ArrayList<Player>();
    private static final HashSet<Material> ignoreAllBlocks = new HashSet<Material>(EnumSet.allOf(Material.class));
    private static final BukkitTask task = new BukkitRunnable() {
        @Override
        public void run() {
            for (int i = 0; i < playersWithHealthBar.size(); i++) {
                Player player = playersWithHealthBar.get(i);
                if (!player.isDead() && player.isValid()) {
                    Location location = player.getLocation().clone();
                    location.add(player.getLocation().getDirection().multiply(30));
                    wither.setPosition(location.getX(), location.getY(), location.getZ());
                    PacketHelper_v1_8_R3.sendPacket(player, new PacketPlayOutEntityTeleport(wither));
                }
                else {
                    hasHealthBar.put(player.getName(), false);
                    playersWithHealthBar.remove(player);
                }
            }
        }
    }.runTaskTimer(NMSHandler.getJavaPlugin(), 20, 20);

    private static final EntityWither wither = new EntityWither(((CraftWorld) Bukkit.getWorlds().get(0)).getHandle());
    static {
        wither.b(true);
        wither.setInvisible(true);
        wither.setCustomNameVisible(true);
    }

    public static void showBossBar(Player player, String text, int health) {
        wither.setCustomName(text);
        wither.setHealth(health);
        Location location = player.getLocation().clone();
        location.add(player.getLocation().getDirection().multiply(30));
        wither.setPosition(location.getX(), location.getY(), location.getZ());
        PacketHelper_v1_8_R3.sendPacket(player, new PacketPlayOutSpawnEntityLiving(wither));
        hasHealthBar.put(player.getName(), true);
        playersWithHealthBar.add(player);
    }

    public static void removeBossBar(Player player) {
        if (hasHealthBar.containsKey(player.getName())) {
            PacketHelper_v1_8_R3.sendPacket(player, new PacketPlayOutEntityDestroy(wither.getId()));
            hasHealthBar.put(player.getName(), false);
            playersWithHealthBar.remove(player);
        }
    }
}

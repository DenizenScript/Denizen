package net.aufdemrand.denizen.utilities;

import org.bukkit.Bukkit;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarFlag;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class BossBarManager {

    private static final Map<UUID, List<BossBar>> displaying = new HashMap<UUID, List<BossBar>>();

    public static void showBossBar(Player player, boolean removeOld, String title, double progress, BarColor color, BarStyle style, BarFlag... flags) {
        UUID uuid = player.getUniqueId();
        if (!displaying.containsKey(uuid)) {
            displaying.put(uuid, new ArrayList<BossBar>());
        }
        List<BossBar> playerBars = displaying.get(uuid);
        if (removeOld && !playerBars.isEmpty()) {
            Iterator<BossBar> iterator = playerBars.iterator();
            while (iterator.hasNext()) {
                BossBar bossBar = iterator.next();
                bossBar.removePlayer(player);
                iterator.remove();
            }
        }
        BossBar bossBar = Bukkit.createBossBar(title, color, style, flags);
        bossBar.setProgress(progress);
        bossBar.addPlayer(player);
        bossBar.show();
        playerBars.add(bossBar);
    }

    public static void removeBossBars(Player player) {
        UUID uuid = player.getUniqueId();
        if (displaying.containsKey(uuid) && !displaying.get(uuid).isEmpty()) {
            Iterator<BossBar> iterator = displaying.get(uuid).iterator();
            while (iterator.hasNext()) {
                BossBar bossBar = iterator.next();
                bossBar.removePlayer(player);
                iterator.remove();
            }
        }
    }
}

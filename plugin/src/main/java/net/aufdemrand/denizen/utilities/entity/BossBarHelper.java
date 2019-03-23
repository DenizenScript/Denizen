package net.aufdemrand.denizen.utilities.entity;

import org.bukkit.Bukkit;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;

import java.util.*;

public class BossBarHelper {

    private static final Map<UUID, List<BossBar>> bossBars = new HashMap<>();

    public static void showSimpleBossBar(Player player, String title, double progress) {
        UUID uuid = player.getUniqueId();
        if (!bossBars.containsKey(uuid)) {
            bossBars.put(uuid, new ArrayList<>());
        }
        List<BossBar> playerBars = bossBars.get(uuid);
        if (!playerBars.isEmpty()) {
            Iterator<BossBar> iterator = playerBars.iterator();
            while (iterator.hasNext()) {
                BossBar bossBar = iterator.next();
                bossBar.removePlayer(player);
                iterator.remove();
            }
        }
        BossBar bossBar = Bukkit.createBossBar(title, BarColor.PURPLE, BarStyle.SOLID);
        bossBar.setProgress(progress);
        bossBar.addPlayer(player);
        bossBar.setVisible(true);
        playerBars.add(bossBar);
    }

    public static void removeSimpleBossBar(Player player) {
        UUID uuid = player.getUniqueId();
        if (bossBars.containsKey(uuid) && !bossBars.get(uuid).isEmpty()) {
            Iterator<BossBar> iterator = bossBars.get(uuid).iterator();
            while (iterator.hasNext()) {
                BossBar bossBar = iterator.next();
                bossBar.removePlayer(player);
                iterator.remove();
            }
        }
    }
}

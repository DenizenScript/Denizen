package net.aufdemrand.denizen.nms.interfaces;

import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarFlag;
import org.bukkit.boss.BarStyle;
import org.bukkit.entity.Player;

public interface BossBarHelper {

    void showBossBar(Player player, boolean removeOld, String title, double progress, BarColor color, BarStyle style, BarFlag... flags);

    void removeBossBars(Player player);
}

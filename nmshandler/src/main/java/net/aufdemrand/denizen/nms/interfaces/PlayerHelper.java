package net.aufdemrand.denizen.nms.interfaces;

import net.aufdemrand.denizen.nms.abstracts.ImprovedOfflinePlayer;
import org.bukkit.Chunk;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.UUID;

public interface PlayerHelper {

    boolean hasChunkLoaded(Player player, Chunk chunk);

    int getPing(Player player);

    void setTemporaryOp(Player player, boolean op);

    void showEndCredits(Player player);

    ImprovedOfflinePlayer getOfflineData(UUID uuid);

    ImprovedOfflinePlayer getOfflineData(OfflinePlayer offlinePlayer);

    void showSimpleBossBar(Player player, String title, double progress);

    void removeSimpleBossBar(Player player);
}

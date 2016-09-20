package net.aufdemrand.denizen.nms.interfaces;

import org.bukkit.Chunk;
import org.bukkit.entity.Player;

public interface PlayerHelper {

    boolean hasChunkLoaded(Player player, Chunk chunk);

    int getPing(Player player);

    void setTemporaryOp(Player player, boolean op);

    void showEndCredits(Player player);
}

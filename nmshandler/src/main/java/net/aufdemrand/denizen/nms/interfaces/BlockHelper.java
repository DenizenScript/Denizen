package net.aufdemrand.denizen.nms.interfaces;

import net.aufdemrand.denizen.nms.util.PlayerProfile;
import org.bukkit.block.Skull;

public interface BlockHelper {

    PlayerProfile getPlayerProfile(Skull skull);

    void setPlayerProfile(Skull skull, PlayerProfile playerProfile);
}

package net.aufdemrand.denizen.nms.interfaces;

import net.aufdemrand.denizen.nms.util.Advancement;
import org.bukkit.entity.Player;

public interface AdvancementHelper {

    void register(Advancement advancement);

    void unregister(Advancement advancement);

    void grant(Advancement advancement, Player player);

    void revoke(Advancement advancement, Player player);
}

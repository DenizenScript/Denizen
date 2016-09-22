package net.aufdemrand.denizen.nms.interfaces;

import org.bukkit.entity.Player;

public interface FakePlayer extends CustomEntity, Player {

    String getFullName();
}

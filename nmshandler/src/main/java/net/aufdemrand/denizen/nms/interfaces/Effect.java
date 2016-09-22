package net.aufdemrand.denizen.nms.interfaces;

import org.bukkit.Location;
import org.bukkit.entity.Player;

public interface Effect {

    void play(Location location, int data, int radius);

    void playFor(Player player, Location location, int data);

    boolean isVisual();

    String getName();
}

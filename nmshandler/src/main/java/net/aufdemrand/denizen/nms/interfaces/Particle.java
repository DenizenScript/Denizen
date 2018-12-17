package net.aufdemrand.denizen.nms.interfaces;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

public interface Particle {

    void playFor(Player player, Location location, int count, Vector offset, double extra);

    <T> void playFor(Player player, Location location, int count, Vector offset, double extra, T data);

    boolean isVisible();

    String getName();

    default Class neededData() {
        return null;
    }
}

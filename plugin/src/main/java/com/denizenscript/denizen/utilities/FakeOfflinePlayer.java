package com.denizenscript.denizen.utilities;

import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.UUID;

public class FakeOfflinePlayer implements OfflinePlayer {
    String name;
    UUID uuid;

    public FakeOfflinePlayer(String _name) {
        name = _name;
        uuid = new UUID(name.length() > 0 ? (long) name.charAt(0) : 52L,
                name.length() > 1 ? (long) name.charAt(1) : 27L);
    }

    @Override
    public boolean isOnline() {
        return false;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public UUID getUniqueId() {
        return uuid;
    }

    @Override
    public boolean isBanned() {
        return false;
    }

    // TODO: 1.12 update removed this
   /* @Override
    public void setBanned(boolean b) {
    }*/

    @Override
    public boolean isWhitelisted() {
        return true;
    }

    @Override
    public void setWhitelisted(boolean b) {
    }

    @Override
    public Player getPlayer() {
        return null;
    }

    @Override
    public long getFirstPlayed() {
        return System.currentTimeMillis();
    }

    @Override
    public long getLastPlayed() {
        return System.currentTimeMillis();
    }

    @Override
    public boolean hasPlayedBefore() {
        return false;
    }

    @Override
    public Location getBedSpawnLocation() {
        return null;
    }

    @Override
    public Map<String, Object> serialize() {
        return null;
    }

    @Override
    public boolean isOp() {
        return false;
    }

    @Override
    public void setOp(boolean b) {
    }
}

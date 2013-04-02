package net.aufdemrand.denizen.utilities.scoreboard;

import net.minecraft.server.v1_5_R2.Packet206SetScoreboardObjective;
import net.minecraft.server.v1_5_R2.Packet207SetScoreboardScore;
import net.minecraft.server.v1_5_R2.Packet208SetScoreboardDisplayObjective;
import net.minecraft.server.v1_5_R2.PlayerConnection;
import org.bukkit.craftbukkit.v1_5_R2.entity.CraftPlayer;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

public class Scoreboard {
    Scoreboard(String name, int priority, ScoreboardAPI plugin) {
        this.name = name;
        this.priority = priority;
        this.plugin = plugin;
    }

    public enum Type {
        PLAYER_LIST, SIDEBAR
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
        for (Player p : players) {
            updatePosition(p);
        }
    }

    public String getName() {
        return name;
    }

    public int getPriority() {
        return priority;
    }

    public String getScoreboardName() {
        return displayName;
    }

    public void setScoreboardName(String displayName) {
        this.displayName = displayName;
        Packet206SetScoreboardObjective pack = new Packet206SetScoreboardObjective();
        pack.a = name;
        pack.b = displayName;
        pack.c = 2;
        for (Player p : players) {
            if (!isUnique(p)) {
                continue;
            }
            ((CraftPlayer) p).getHandle().playerConnection.sendPacket(pack);
        }
    }

    public void setItem(String name2, int value) {
        items.put(name2, value);
        Packet207SetScoreboardScore pack = new Packet207SetScoreboardScore();
        pack.a = name2;
        pack.c = value;
        pack.d = 0;
        pack.b = name;
        for (Player p : players) {
            if (!isUnique(p)) {
                continue;
            }
            ((CraftPlayer) p).getHandle().playerConnection.sendPacket(pack);
        }
    }

    public void removeItem(String name2) {
        if (items.remove(name2) != null) {
            Packet207SetScoreboardScore pack = new Packet207SetScoreboardScore();
            pack.a = name2;
            pack.c = 0;
            pack.d = 1;
            pack.b = name;
            for (Player p : players) {
                if (!isUnique(p)) {
                    continue;
                }
                ((CraftPlayer) p).getHandle().playerConnection.sendPacket(pack);
            }
        }
    }

    public boolean hasPlayerAdded(Player p) {
        return players.contains(p);
    }

    public List<Player> getAddedPlayers() {
        return players;
    }

    public void showToPlayer(Player p, boolean show) {
        if (show) {
            if (!players.contains(p)) {
                players.add(p);
                plugin.updateForPlayer(p);
            }
        } else {
            if (players.remove(p)) {
                Packet206SetScoreboardObjective pack = new Packet206SetScoreboardObjective();
                pack.a = name;
                pack.b = "";
                pack.c = 1;
                ((CraftPlayer) p).getHandle().playerConnection.sendPacket(pack);
                plugin.updateForPlayer(p);
            }
        }
    }

    public void showToPlayer(Player p) {
        showToPlayer(p, true);
    }

    public void stopShowingAllPlayers() {
        for (Player p : players) {
            showToPlayer(p, false);
        }
    }

    private void updatePosition(Player p) {
        if (!isUnique(p)) {
            return;
        }
        Packet208SetScoreboardDisplayObjective pack2 = new Packet208SetScoreboardDisplayObjective();
        pack2.a = type.ordinal();
        pack2.b = name;
        ((CraftPlayer) p).getHandle().playerConnection.sendPacket(pack2);
    }

    public void checkIfNeedsToBeDisabledForPlayer(Player p) {
        if (!players.contains(p)) {
            return;
        }
        PlayerConnection conn = ((CraftPlayer) p).getHandle().playerConnection;
        if (!isUnique(p)) {
            Packet206SetScoreboardObjective pack = new Packet206SetScoreboardObjective();
            pack.a = name;
            pack.b = displayName;
            pack.c = 1;
            conn.sendPacket(pack);
        }
    }

    public void checkIfNeedsToBeEnabledForPlayer(Player p) {
        if (!players.contains(p)) {
            return;
        }
        PlayerConnection conn = ((CraftPlayer) p).getHandle().playerConnection;
        if (isUnique(p)) {
            Packet206SetScoreboardObjective pack = new Packet206SetScoreboardObjective();
            pack.a = name;
            pack.b = displayName;
            pack.c = 0;
            conn.sendPacket(pack);
            for (String name2 : items.keySet()) {
                Integer valObj = items.get(name2);
                if (valObj == null) {
                    continue;
                }
                int val = valObj.intValue();
                Packet207SetScoreboardScore pack2 = new Packet207SetScoreboardScore();
                pack2.a = name2;
                pack2.c = val;
                pack2.d = 0;
                pack2.b = name;
                conn.sendPacket(pack2);
            }
            updatePosition(p);
        }

    }

    private boolean isUnique(Player p) {
        int myPos = 0;
        for (int i = 0; i < plugin.getScoreboards().size(); i++) {
            if (plugin.getScoreboards().get(i) == this) {
                myPos = i;
                break;
            }
            Scoreboard s = plugin.getScoreboards().get(i);
            if (s != this && s.hasPlayerAdded(p) && s.getType() == type && (s.getPriority() > priority || (i > myPos && s.getPriority() == priority))) {
                return false;
            }
        }
        return true;
    }

    private List<Player> players = new LinkedList<Player>();

    private HashMap<String, Integer> items = new HashMap<String, Integer>();

    private Type type = Type.SIDEBAR;

    private String name;

    private String displayName = "§4Not initialized";

    private int priority = 10;

    private ScoreboardAPI plugin;
}

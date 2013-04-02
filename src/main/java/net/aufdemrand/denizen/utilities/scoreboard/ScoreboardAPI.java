package net.aufdemrand.denizen.utilities.scoreboard;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

public class ScoreboardAPI {
	
    public static ScoreboardAPI api_instance = null;

    public static List<Scoreboard> scoreboards = new ArrayList<Scoreboard>();

    public List<Scoreboard> getScoreboards() {
        return scoreboards;
    }

	Format format = new Format(this);

    public static ScoreboardAPI getInstance() {
        return api_instance;
    }

    public Scoreboard createScoreboard(String name, int priority) {
        for (Scoreboard s : scoreboards) {
            if (s.getName() == name) {
                return null;
            }
        }
        Scoreboard s = new Scoreboard(name, priority);
        scoreboards.add(s);
        return s;
    }

    public ScoreboardAPI() {
        api_instance = this;
    }

    public Scoreboard getScoreboard(String name) {
        for (Scoreboard s : scoreboards) {
            if (s.getName() == name) {
                return s;
            }
        }
        return null;
    }

    public void updateForPlayer(Player p) {
        for (Scoreboard s : scoreboards) {
            s.checkIfNeedsToBeDisabledForPlayer(p);
            s.checkIfNeedsToBeEnabledForPlayer(p);
        }
    }

    public boolean isPlayerReceivingScoreboard(Player p) {
        for (Scoreboard s: scoreboards) {
            if (s.hasPlayerAdded(p)) {
                return true;
            }
        }
        return false;
    }

    public void updateForAllPlayers() {
        for (Player p : Bukkit.getOnlinePlayers()) {
            updateForPlayer(p);
        }
    }

}

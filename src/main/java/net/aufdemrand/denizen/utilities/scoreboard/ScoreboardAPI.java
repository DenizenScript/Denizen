package net.aufdemrand.denizen.utilities.scoreboard;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import net.aufdemrand.denizen.utilities.debugging.dB;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

public class ScoreboardAPI {
	
    public static ScoreboardAPI api_instance = null;

    public ScoreboardAPI() {
        api_instance = this;
    }

    public static List<Scoreboard> scoreboards = new ArrayList<Scoreboard>();

    public static ScoreboardAPI getInstance() {
        return api_instance;
    }

    public List<Scoreboard> getScoreboards() {
        return scoreboards;
    }

    public Scoreboard createScoreboard(String name, int priority) {
        for (Scoreboard s : scoreboards) {
            if (s.getName().equalsIgnoreCase(name)) {
                return s;
            }
        }
        Scoreboard s = new Scoreboard(name, priority);
        scoreboards.add(s);
        return s;
    }

    public Scoreboard getScoreboard(String name) {
        dB.echoDebug(scoreboards.size() + " SBSIZE");
        for (Scoreboard s : scoreboards) {
            if (s.getName().equalsIgnoreCase(name)) {
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

    public void removeScoreboard(String name) {
        Scoreboard to_remove = null;
        for (Scoreboard s : scoreboards) {
            if (s.getName().equalsIgnoreCase(name)) {
                to_remove = s;
            }
        }

        to_remove.stopShowingAllPlayers();
        scoreboards.remove(to_remove);
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

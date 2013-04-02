package net.aufdemrand.denizen.utilities.scoreboard;

import java.util.LinkedList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

public class ScoreboardAPI {
	
	//Global Definitions:
	public static String PREFIX_COLOR = ChatColor.AQUA + "";
	public static String CONFIG_NAME = "config.yml";
	public static boolean ENABLE_METRICS = false;
	//End

    public static ScoreboardAPI api_instance;

    public static List<Scoreboard> scoreboards = new LinkedList<Scoreboard>();

    public static List<Scoreboard> getScoreboards() {
        return scoreboards;
    }

	Format format = new Format(this);

    public static ScoreboardAPI getInstance() {
        return ScoreboardAPI.api_instance;
    }

    public Scoreboard createScoreboard(String name, int priority) {
        for (Scoreboard s : scoreboards) {
            if (s.getName() == name) {
                return null;
            }
        }
        Scoreboard s = new Scoreboard(name, priority, this);
        scoreboards.add(s);
        return s;
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

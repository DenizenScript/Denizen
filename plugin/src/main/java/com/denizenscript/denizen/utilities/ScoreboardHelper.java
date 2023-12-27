package com.denizenscript.denizen.utilities;

import com.denizenscript.denizen.Denizen;
import com.denizenscript.denizencore.utilities.debugging.Debug;
import com.denizenscript.denizencore.objects.Argument;
import com.denizenscript.denizencore.utilities.CoreUtilities;
import com.denizenscript.denizen.objects.PlayerTag;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.scoreboard.*;

import java.util.*;

/** Helper/manager for scoreboards that creates, saves, and loads scoreboards. */
public class ScoreboardHelper {

    public static ScoreboardManager manager = Bukkit.getScoreboardManager();

    /** A map with scoreboard IDs as keys and scoreboards as values. */
    public static Map<String, Scoreboard> scoreboardMap = new HashMap<>();

    /** A map with viewer names as keys and scoreboard IDs as values. */
    public static Map<UUID, String> viewerMap = new HashMap<>();

    /** Called on server startup or /denizen reload saves. */
    public static void _recallScoreboards() {
        for (Map.Entry<String, Scoreboard> entry : scoreboardMap.entrySet()) {
            clearScoreboard(entry.getValue());
        }
        Scoreboard emptyBoard = createScoreboard();
        for (Map.Entry<UUID, String> entry : viewerMap.entrySet()) {
            OfflinePlayer player = Bukkit.getPlayer(entry.getKey());
            if (player != null && player.isOnline()) {
                player.getPlayer().setScoreboard(emptyBoard);
            }
        }
        scoreboardMap.clear();
        viewerMap.clear();
        ConfigurationSection rootSection = Denizen.getInstance().getScoreboards().getConfigurationSection("Scoreboards");
        if (rootSection == null) {
            return;
        }
        Scoreboard board;
        for (String id : rootSection.getKeys(false)) {
            board = createScoreboard(id);
            List<String> viewerList = rootSection.getStringList(id + ".Viewers");
            for (String viewer : viewerList) {
                if (PlayerTag.matches(viewer)) {
                    PlayerTag player = PlayerTag.valueOf(viewer, CoreUtilities.basicContext);
                    viewerMap.put(player.getUUID(), id);
                    if (player.isOnline()) {
                        player.getPlayerEntity().setScoreboard(board);
                    }
                }
            }
            ConfigurationSection objSection = rootSection.getConfigurationSection(id + ".Objectives");
            if (objSection == null) {
                return;
            }
            for (String obj : objSection.getKeys(false)) {
                String displaySlot = objSection.getString(obj + ".Display slot");
                String criteria = objSection.getString(obj + ".Criteria");
                if (criteria == null) {
                    criteria = "dummy";
                }
                if (displaySlot == null) {
                    displaySlot = "NONE";
                }
                // TODO: When 1.19 is minimum version: Objective o = board.registerNewObjective(obj, Bukkit.getScoreboardCriteria(criteria), obj);
                Objective o = board.registerNewObjective(obj, criteria);
                if (Argument.valueOf(displaySlot).matchesEnum(DisplaySlot.class)) {
                    o.setDisplaySlot(DisplaySlot.valueOf(displaySlot.toUpperCase()));
                }
                ConfigurationSection scoreSection = objSection.getConfigurationSection(obj + ".Scores");
                if (scoreSection != null) {
                    for (String scoreName : scoreSection.getKeys(false)) {
                        int scoreInt = scoreSection.getInt(scoreName);
                        addScore(o, scoreName, scoreInt);
                    }
                }
            }
        }
    }

    /** Called on server shutdown or /denizen save. */
    public static void _saveScoreboards() {
        try {
            Denizen.getInstance().getScoreboards().set("Scoreboards", null);
            for (Map.Entry<String, Scoreboard> scoreboardEntry : scoreboardMap.entrySet()) {
                String id = scoreboardEntry.getKey();
                List<String> viewerList = new ArrayList<>();
                for (Map.Entry<UUID, String> viewerEntry : viewerMap.entrySet()) {
                    if (id.equalsIgnoreCase(viewerEntry.getValue())) {
                        viewerList.add(viewerEntry.getKey().toString());
                    }
                }
                Denizen.getInstance().getScoreboards().set("Scoreboards." + id + ".Viewers", viewerList);
                for (Objective obj : scoreboardEntry.getValue().getObjectives()) {
                    String objPath = "Scoreboards." + id + ".Objectives." + obj.getName();
                    Denizen.getInstance().getScoreboards().set(objPath + ".Criteria", obj.getCriteria());
                    String displaySlot;
                    if (obj.getDisplaySlot() != null) {
                        displaySlot = obj.getDisplaySlot().name();
                    }
                    else {
                        displaySlot = "NONE";
                    }
                    Denizen.getInstance().getScoreboards().set(objPath + ".Display slot", displaySlot);
                    if (obj.isModifiable()) {
                        // There is no method for getting an objective's scores, so iterate through all of the scoreboard's players, check if they have a score for this objective, and save it if they do
                        for (String player : scoreboardEntry.getValue().getEntries()) {
                            int score = obj.getScore(player).getScore();
                            Team team = scoreboardEntry.getValue().getTeam(player);
                            // TODO: Properly save and load teams
                            player = (team != null && team.getPrefix() != null ? team.getPrefix() : "") + player + (team != null && team.getSuffix() != null ? team.getSuffix() : "");
                            // If a player has no score for this objective, getScore() will return 0, so ignore scores of 0
                            if (score != 0) {
                                Denizen.getInstance().getScoreboards().set(objPath + ".Scores." + player, score);
                            }
                        }
                    }
                }
            }
        }
        catch (Exception e) {
            Debug.echoError(e);
        }
    }

    /** Add a score to an Objective for a name. */
    public static void addScore(Objective o, String playerName, int score) {
        Score sc = o.getScore(playerName);
        // If the score is 0, it won't normally be displayed at first, so force it to be displayed by using setScore() like below on it
        if (score == 0) {
            sc.setScore(1);
            sc.setScore(0);
        }
        else {
            sc.setScore(score);
        }
    }

    /** Remove a score from an Objective for a name. */
    public static void removeScore(Objective o, String playerName) {
        // There is no method to remove a single score from an objective, as confirmed here: https://bukkit.atlassian.net/browse/BUKKIT-4014
        // So use crazy workaround below (save all scores, clear, and re-add)
        Scoreboard board = o.getScoreboard();
        Map<String, Integer> scoreMap = new HashMap<>();
        for (Score sc : board.getScores(playerName)) {
            if (!sc.getObjective().equals(o)) {
                scoreMap.put(sc.getObjective().getName(), sc.getScore());
            }
        }
        board.resetScores(playerName);
        for (Map.Entry<String, Integer> entry : scoreMap.entrySet()) {
            board.getObjective(entry.getKey()).getScore(playerName).setScore(entry.getValue());
        }
    }

    /** Clears all the objectives from a Scoreboard, making it empty. */
    public static void clearScoreboard(Scoreboard board) {
        for (Objective o : board.getObjectives()) {
            o.unregister();
        }
    }

    /** Creates an anonymous new Scoreboard that isn't saved anywhere. */
    public static Scoreboard createScoreboard() {
        return manager.getNewScoreboard();
    }

    /** Creates a new Scoreboard with a certain id and stories it in the scoreboards map. */
    public static Scoreboard createScoreboard(String id) {
        Scoreboard board = manager.getNewScoreboard();
        scoreboardMap.put(id.toUpperCase(), board);
        return board;
    }

    /** Deletes a Scoreboard, clearing it and removing it from the scoreboards map, unless it is the server's main scoreboard, in which case it is just cleared. */
    public static void deleteScoreboard(String id) {
        if (id.equalsIgnoreCase("main")) {
            clearScoreboard(getMain());
        }
        else {
            clearScoreboard(getScoreboard(id));
            scoreboardMap.remove(id.toUpperCase());
        }
    }

    /** Returns the server's main scoreboard, that isn't stored in the scoreboards map because Bukkit already saves it by itself. */
    public static Scoreboard getMain() {
        return manager.getMainScoreboard();
    }

    /** Returns a Scoreboard from the scoreboards map. */
    public static Scoreboard getScoreboard(String id) {
        return scoreboardMap.get(id.toUpperCase());
    }

    /** Returns true if the scoreboards map contains a certain scoreboard id. */
    public static boolean hasScoreboard(String id) {
        return scoreboardMap.containsKey(id.toUpperCase());
    }

    public static void removePlayer(String id, String name) {
        scoreboardMap.get(id.toUpperCase()).resetScores(name);
    }
}

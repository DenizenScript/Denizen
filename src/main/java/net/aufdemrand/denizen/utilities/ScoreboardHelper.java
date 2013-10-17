package net.aufdemrand.denizen.utilities;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.aufdemrand.denizen.objects.aH.Argument;
import net.aufdemrand.denizen.objects.dPlayer;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;

/**
 * Creates, saves and loads scoreboards
 *
 * @author David Cernat
 */

public class ScoreboardHelper {

    public static ScoreboardManager manager = Bukkit.getScoreboardManager();
    public static Map<String, Scoreboard> scoreboards = new HashMap<String, Scoreboard>();

    /*
     * Called on server startup or /denizen reload saves
     */
    public static void _recallScoreboards() {

        // Clear every existing ingame scoreboard
        for (Map.Entry<String, Scoreboard> entry : scoreboards.entrySet()) {
            clearScoreboard(entry.getValue());
        }

        // Delete the contents of the scoreboards map
        scoreboards.clear();

        ConfigurationSection rootSection = DenizenAPI.getCurrentInstance()
                .getScoreboards().getConfigurationSection("Scoreboards");

        // Go no further if we have no scoreboards saved
        if (rootSection == null) return;

        Scoreboard board = null;

        // Iterate through scoreboards
        for (String id : rootSection.getKeys(false)) {
            // Create a scoreboard with this id
            board = createScoreboard(id);

            // Get the list of viewers
            List<String> viewers = rootSection.getStringList(id + ".Viewers");

            // Iterate through viewers and make them see this scoreboard
            // if they are online
            for (String viewer : viewers) {
                if (dPlayer.matches(viewer)) {
                    dPlayer player = dPlayer.valueOf(viewer);

                    if (player.isOnline())
                        player.getPlayerEntity().setScoreboard(board);
                }
            }

            ConfigurationSection objSection = rootSection
                    .getConfigurationSection(id + ".Objectives");

            // Go no further if we have no objectives saved
            if (objSection == null) return;

            // Iterate through objectives
            for (String obj : objSection.getKeys(false)) {
                // Get display slot and criteria for this objective
                String displaySlot = objSection.getString(obj + ".Display slot");
                String criteria = objSection.getString(obj + ".Criteria");

                // Use default criteria if necessary
                if (criteria == null)
                    criteria = "dummy";
                // Use default display slot if necessary
                if (displaySlot == null)
                    displaySlot = "NONE";

                // Register the objective and set it up
                Objective o = board.registerNewObjective(obj, criteria);
                o.setDisplayName(obj);

                // Only set display slot if it's valid
                if (Argument.valueOf(displaySlot).matchesEnum(DisplaySlot.values())) {
                    o.setDisplaySlot(DisplaySlot.valueOf(displaySlot.toUpperCase()));
                }

                ConfigurationSection scoreSection = objSection
                        .getConfigurationSection(obj + ".Scores");

                if (scoreSection != null) {
                    // Iterate through scores and add them to this objective
                    for (String scoreName : scoreSection.getKeys(false)) {
                        int scoreInt = scoreSection.getInt(scoreName);
                        addScore(o, Bukkit.getOfflinePlayer(scoreName), scoreInt);
                    }
                }
            }
        }
    }

    /*
     * Called on server shutdown or /denizen save
     */
    public static void _saveScoreboards() {

        // Clear scoreboards.yml
        DenizenAPI.getCurrentInstance().getScoreboards()
            .set("Scoreboards", null);

        // Iterate through scoreboards map
        for (Map.Entry<String, Scoreboard> entry : scoreboards.entrySet()) {

            String id = entry.getKey();
            List<String> viewers = new ArrayList<String>();

            // There is no method to get the viewers of a scoreboard,
            // so iterate through all of the players and check if
            // they can see this scoreboard
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (player.getScoreboard() == entry.getValue()) {
                    viewers.add(player.getName());
                }
            }

            // Save viewer list
            DenizenAPI.getCurrentInstance().getScoreboards()
                .set("Scoreboards." + id + ".Viewers", viewers);

            // Iterate through objectives
            for (Objective obj : entry.getValue().getObjectives()) {
                String objPath = "Scoreboards." + id + ".Objectives."
                                 + obj.getName();

                // Save criteria for this objective
                DenizenAPI.getCurrentInstance().getScoreboards()
                    .set(objPath + ".Criteria", obj.getCriteria());

                String displaySlot;
                // If the display slot is null, save it as "NONE";
                // otherwise, save it with its regular name
                if (obj.getDisplaySlot() != null)
                    displaySlot = obj.getDisplaySlot().name();
                else
                    displaySlot = "NONE";

                DenizenAPI.getCurrentInstance().getScoreboards()
                    .set(objPath + ".Display slot", displaySlot);

                // There is no method for getting an objective's
                // scores, so iterate through all of the scoreboard's
                // players, check if they have a score for this
                // objective, and save it if they do
                for (OfflinePlayer player : entry.getValue().getPlayers()) {

                    int score = obj.getScore(player).getScore();

                    // If a player has no score for this objective,
                    // getScore() will return 0, so ignore scores
                    // of 0
                    if (score != 0) {
                        DenizenAPI.getCurrentInstance().getScoreboards()
                            .set(objPath + ".Scores." + player.getName(), score);
                    }
                }
            }
        }
    }


    /////////////////////
    //   OBJECTIVE METHODS
    /////////////////

    /**
     * Add a score to an Objective for an OfflinePlayer.
     *
     * @param Scoreboard  the Objective to add the score to
     * @param OfflinePlayer  the OfflinePlayer to set the score for
     * @param int  the score
     *
     */
    public static void addScore(Objective o, OfflinePlayer player, int score) {
        Score sc = o.getScore(player);

        // If the score is 0, it won't normally be displayed at first,
        // so force it to be displayed by using setScore() like below on it
        if (score == 0) {
            sc.setScore(1); sc.setScore(0);
        }
        else sc.setScore(score);
    }

    /**
     * Remove a score from an Objective for an OfflinePlayer.
     *
     * @param Scoreboard  the Objective to remove the score from
     * @param String  the OfflinePlayer to remove the score for
     *
     */
    public static void removeScore(Objective o, OfflinePlayer player) {

        // There is no method to remove a single score from an
        // objective, as confirmed here:
        // https://bukkit.atlassian.net/browse/BUKKIT-4014
        //
        // So use crazy workaround below
        Scoreboard board = o.getScoreboard();
        Map<String, Integer> scoreMap = new HashMap<String, Integer>();

        // Go through every score for this (real or fake) player
        // and put it in scoreMap if it doesn't belong to the
        // objective we want to remove the score from
        for (Score sc : board.getScores(player)) {
            if (!sc.getObjective().equals(o)) {
                scoreMap.put(sc.getObjective().getName(), sc.getScore());
            }
        }

        // Remove all the scores for this (real or fake) player
        board.resetScores(player);

        // Go through scoreMap and add back all the scores we saved
        // for this (real or fake) player
        for (Map.Entry<String, Integer> entry : scoreMap.entrySet()) {
            board.getObjective(entry.getKey())
                 .getScore(player).setScore(entry.getValue());
        }
    }


    /////////////////////
    //   SCOREBOARD METHODS
    /////////////////

    /**
     * Clears all the objectives from a Scoreboard, making
     * it empty.
     *
     * @param Scoreboard  the Scoreboard to clear
     *
     */
    public static void clearScoreboard(Scoreboard board) {
        for (Objective o : board.getObjectives()) {
            o.unregister();
        }
    }

    /**
     * Creates an anonymous new Scoreboard that isn't
     * saved anywhere.
     *
     * @return  the new Scoreboard
     *
     */
    public static Scoreboard createScoreboard() {
        Scoreboard board = manager.getNewScoreboard();
        return board;
    }

    /**
     * Creates a new Scoreboard with a certain id and
     * stories it in the scoreboards map.
     *
     * @param String  the id of the new Scoreboard
     * @return  the new Scoreboard
     *
     */
    public static Scoreboard createScoreboard(String id) {
        Scoreboard board = manager.getNewScoreboard();
        scoreboards.put(id.toUpperCase(), board);
        return board;
    }

    /**
     * Deletes a Scoreboard, clearing it and removing it from
     * the scoreboards map, unless it is the server's main
     * scoreboard, in which case it is just cleared.
     *
     * @param String  the id of the Scoreboard
     *
     */
    public static void deleteScoreboard(String id) {
        if (id.equalsIgnoreCase("main"))
            clearScoreboard(getMain());
        else {
            clearScoreboard(getScoreboard(id));
            scoreboards.remove(id.toUpperCase());
        }
    }

    /**
     * Returns the server's main scoreboard, that isn't stored
     * in the scoreboards map because Bukkit already saves it
     * by itself.
     *
     * @return  the main Scoreboard
     *
     */
    public static Scoreboard getMain() {
        return manager.getMainScoreboard();
    }

    /**
     * Returns a Scoreboard from the scoreboards map.
     *
     * @param String  the id of the Scoreboard
     * @return  the Scoreboard returned
     *
     */
    public static Scoreboard getScoreboard(String id) {
        return scoreboards.get(id.toUpperCase());
    }

    /**
     * Returns true if the scoreboards map contains a certain
     * scoreboard id.
     *
     * @param String  the id of the Scoreboard
     * @return  true or false
     *
     */
    public static boolean hasScoreboard(String id) {
        if (scoreboards.containsKey(id.toUpperCase())) return true;
        else return false;
    }

    /**
     * Removes all the scores of an OfflinePlayer from a
     * Scoreboard.
     *
     * @param String  the id of the Scoreboard
     * @param OfflinePlayer  the OfflinePlayer
     *
     */
    public static void removePlayer(String id, OfflinePlayer player) {
        scoreboards.get(id.toUpperCase()).resetScores(player);
    }
}

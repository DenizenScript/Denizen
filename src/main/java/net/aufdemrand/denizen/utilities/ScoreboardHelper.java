package net.aufdemrand.denizen.utilities;

import com.google.common.base.Splitter;
import net.aufdemrand.denizen.objects.dPlayer;
import net.aufdemrand.denizen.scripts.commands.server.ScoreboardCommand;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.aufdemrand.denizencore.objects.aH;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.scoreboard.*;

import java.util.*;

/**
 * Creates, saves and loads scoreboards
 *
 * @author David Cernat
 */

public class ScoreboardHelper {

    public static ScoreboardManager manager = Bukkit.getScoreboardManager();

    // A map with scoreboard IDs as keys and scoreboards as values
    public static Map<String, Scoreboard> scoreboardMap = new HashMap<String, Scoreboard>();
    // A map with viewer names as keys and scoreboard IDs as values
    public static Map<String, String> viewerMap = new HashMap<String, String>();

    /*
     * Called on server startup or /denizen reload saves
     */
    public static void _recallScoreboards() {

        // Clear every existing ingame scoreboard
        for (Map.Entry<String, Scoreboard> entry : scoreboardMap.entrySet()) {
            clearScoreboard(entry.getValue());
        }

        Scoreboard emptyBoard = createScoreboard();

        // Clear every viewer's set scoreboard
        for (Map.Entry<String, String> entry : viewerMap.entrySet()) {
            OfflinePlayer player = ScoreboardCommand.getOfflinePlayer(entry.getKey());
            if (player.isOnline()) {
                player.getPlayer().setScoreboard(emptyBoard);
            }
        }

        // Delete the contents of the scoreboardMap and viewerMap
        scoreboardMap.clear();
        viewerMap.clear();

        ConfigurationSection rootSection = DenizenAPI.getCurrentInstance()
                .getScoreboards().getConfigurationSection("Scoreboards");

        // Go no further if we have no scoreboards saved
        if (rootSection == null) {
            return;
        }

        Scoreboard board = null;

        // Iterate through scoreboards
        for (String id : rootSection.getKeys(false)) {
            // Create a scoreboard with this id
            board = createScoreboard(id);

            // Get the list of viewers
            List<String> viewerList = rootSection.getStringList(id + ".Viewers");

            // Iterate through viewers, store them in the viewerMap,
            // and make them see this scoreboard if they are online
            for (String viewer : viewerList) {
                if (dPlayer.matches(viewer)) {
                    dPlayer player = dPlayer.valueOf(viewer);
                    viewerMap.put(player.getName(), id);

                    if (player.isOnline()) {
                        player.getPlayerEntity().setScoreboard(board);
                    }
                }
            }

            ConfigurationSection objSection = rootSection
                    .getConfigurationSection(id + ".Objectives");

            // Go no further if we have no objectives saved
            if (objSection == null) {
                return;
            }

            // Iterate through objectives
            for (String obj : objSection.getKeys(false)) {
                // Get display slot and criteria for this objective
                String displaySlot = objSection.getString(obj + ".Display slot");
                String criteria = objSection.getString(obj + ".Criteria");

                // Use default criteria if necessary
                if (criteria == null) {
                    criteria = "dummy";
                }
                // Use default display slot if necessary
                if (displaySlot == null) {
                    displaySlot = "NONE";
                }

                // Register the objective and set it up
                Objective o = board.registerNewObjective(obj, criteria);
                o.setDisplayName(obj);

                // Only set display slot if it's valid
                if (aH.Argument.valueOf(displaySlot).matchesEnum(DisplaySlot.values())) {
                    o.setDisplaySlot(DisplaySlot.valueOf(displaySlot.toUpperCase()));
                }

                ConfigurationSection scoreSection = objSection
                        .getConfigurationSection(obj + ".Scores");

                if (scoreSection != null) {
                    // Iterate through scores and add them to this objective
                    for (String scoreName : scoreSection.getKeys(false)) {
                        int scoreInt = scoreSection.getInt(scoreName);
                        addScore(o, ScoreboardCommand.getOfflinePlayer(scoreName), scoreInt);
                    }
                }
            }
        }
    }

    /*
     * Called on server shutdown or /denizen save
     */
    public static void _saveScoreboards() {

        try {
            // Clear scoreboards.yml
            DenizenAPI.getCurrentInstance().getScoreboards()
                    .set("Scoreboards", null);

            // Iterate through scoreboards map
            for (Map.Entry<String, Scoreboard> scoreboardEntry : scoreboardMap.entrySet()) {

                String id = scoreboardEntry.getKey();
                List<String> viewerList = new ArrayList<String>();

                // Find all of the viewers that are viewing this scoreboard
                // and put them on a list
                for (Map.Entry<String, String> viewerEntry : viewerMap.entrySet()) {
                    if (id.equalsIgnoreCase(viewerEntry.getValue())) {
                        viewerList.add(viewerEntry.getKey());
                    }
                }

                // Save viewer list
                DenizenAPI.getCurrentInstance().getScoreboards()
                        .set("Scoreboards." + id + ".Viewers", viewerList);

                // Iterate through objectives
                for (Objective obj : scoreboardEntry.getValue().getObjectives()) {
                    String objPath = "Scoreboards." + id + ".Objectives."
                            + obj.getName();

                    // Save criteria for this objective
                    DenizenAPI.getCurrentInstance().getScoreboards()
                            .set(objPath + ".Criteria", obj.getCriteria());

                    String displaySlot;
                    // If the display slot is null, save it as "NONE";
                    // otherwise, save it with its regular name
                    if (obj.getDisplaySlot() != null) {
                        displaySlot = obj.getDisplaySlot().name();
                    }
                    else {
                        displaySlot = "NONE";
                    }

                    DenizenAPI.getCurrentInstance().getScoreboards()
                            .set(objPath + ".Display slot", displaySlot);

                    // There is no method for getting an objective's
                    // scores, so iterate through all of the scoreboard's
                    // players, check if they have a score for this
                    // objective, and save it if they do
                    for (String player : scoreboardEntry.getValue().getEntries()) {

                        int score = obj.getScore(player).getScore();
                        Team team = scoreboardEntry.getValue().getTeam(player);
                        // TODO: Properly save and load teams
                        player = (team != null && team.getPrefix() != null ? team.getPrefix() : "") + player + (team != null && team.getSuffix() != null ? team.getSuffix() : "");

                        // If a player has no score for this objective,
                        // getScore() will return 0, so ignore scores
                        // of 0
                        if (score != 0) {
                            DenizenAPI.getCurrentInstance().getScoreboards()
                                    .set(objPath + ".Scores." + player, score);
                        }
                    }
                }
            }
        }
        catch (Exception e) {
            dB.echoError(e);
        }
    }


    /////////////////////
    //   OBJECTIVE METHODS
    /////////////////

    /**
     * Add a score to an Objective for an OfflinePlayer.
     *
     * @param o      the Objective to add the score to
     * @param player the OfflinePlayer to set the score for
     * @param score  the score
     */
    public static void addScore(Objective o, OfflinePlayer player, int score) {
        Score sc;
        if (player.getName().length() <= 16) {
            sc = o.getScore(player.getName());
        }
        else {
            Map.Entry<Team, String> teamData = createTeam(o.getScoreboard(), player.getName());
            sc = o.getScore(teamData.getValue());
            teamData.getKey().addPlayer(ScoreboardCommand.getOfflinePlayer(teamData.getValue()));
        }

        // If the score is 0, it won't normally be displayed at first,
        // so force it to be displayed by using setScore() like below on it
        if (score == 0) {
            sc.setScore(1);
            sc.setScore(0);
        }
        else {
            sc.setScore(score);
        }
    }

    /**
     * Remove a score from an Objective for an OfflinePlayer.
     *
     * @param o      the Objective to remove the score from
     * @param player the OfflinePlayer to remove the score for
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
        String name = createTeam(board, player.getName()).getValue();
        // TODO: Properly remove when teams are involved
        for (Score sc : board.getScores(name)) {
            if (!sc.getObjective().equals(o)) {
                scoreMap.put(sc.getObjective().getName(), sc.getScore());
            }
        }

        // Remove all the scores for this (real or fake) player
        board.resetScores(player.getName());

        // Go through scoreMap and add back all the scores we saved
        // for this (real or fake) player
        for (Map.Entry<String, Integer> entry : scoreMap.entrySet()) {
            board.getObjective(entry.getKey())
                    .getScore(player.getName()).setScore(entry.getValue());
        }
    }

    private static Map.Entry<Team, String> createTeam(Scoreboard scoreboard, String text) {
        if (text.length() <= 16) {
            return new HashMap.SimpleEntry<Team, String>(null, text);
        }
        if (text.length() <= 32) {
            Team team = scoreboard.registerNewTeam("text-" + scoreboard.getTeams().size());
            team.setPrefix(text.substring(0, text.length() - 16));
            String result = text.substring(text.length() - 16);
            return new HashMap.SimpleEntry<Team, String>(team, result);
        }
        Team team = scoreboard.registerNewTeam("text-" + scoreboard.getTeams().size());
        Iterator<String> iterator = Splitter.fixedLength(16).split(text).iterator();
        team.setPrefix(iterator.next());
        String result = iterator.next();
        if (text.length() > 32) {
            team.setSuffix(iterator.next());
        }
        return new HashMap.SimpleEntry<Team, String>(team, result);
    }

    /////////////////////
    //   SCOREBOARD METHODS
    /////////////////

    /**
     * Clears all the objectives from a Scoreboard, making
     * it empty.
     *
     * @param board the Scoreboard to clear
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
     * @return the new Scoreboard
     */
    public static Scoreboard createScoreboard() {
        return manager.getNewScoreboard();
    }

    /**
     * Creates a new Scoreboard with a certain id and
     * stories it in the scoreboards map.
     *
     * @param id the id of the new Scoreboard
     * @return the new Scoreboard
     */
    public static Scoreboard createScoreboard(String id) {
        Scoreboard board = manager.getNewScoreboard();
        scoreboardMap.put(id.toUpperCase(), board);
        return board;
    }

    /**
     * Deletes a Scoreboard, clearing it and removing it from
     * the scoreboards map, unless it is the server's main
     * scoreboard, in which case it is just cleared.
     *
     * @param id the id of the Scoreboard
     */
    public static void deleteScoreboard(String id) {
        if (id.equalsIgnoreCase("main")) {
            clearScoreboard(getMain());
        }
        else {
            clearScoreboard(getScoreboard(id));
            scoreboardMap.remove(id.toUpperCase());
        }
    }

    /**
     * Returns the server's main scoreboard, that isn't stored
     * in the scoreboards map because Bukkit already saves it
     * by itself.
     *
     * @return the main Scoreboard
     */
    public static Scoreboard getMain() {
        return manager.getMainScoreboard();
    }

    /**
     * Returns a Scoreboard from the scoreboards map.
     *
     * @param id the id of the Scoreboard
     * @return the Scoreboard returned
     */
    public static Scoreboard getScoreboard(String id) {
        return scoreboardMap.get(id.toUpperCase());
    }

    /**
     * Returns true if the scoreboards map contains a certain
     * scoreboard id.
     *
     * @param id the id of the Scoreboard
     * @return true or false
     */
    public static boolean hasScoreboard(String id) {
        return scoreboardMap.containsKey(id.toUpperCase());
    }

    /**
     * Removes all the scores of an OfflinePlayer from a
     * Scoreboard.
     *
     * @param id     the id of the Scoreboard
     * @param player the OfflinePlayer
     */
    public static void removePlayer(String id, OfflinePlayer player) {
        scoreboardMap.get(id.toUpperCase()).resetScores(player.getName());
    }
}

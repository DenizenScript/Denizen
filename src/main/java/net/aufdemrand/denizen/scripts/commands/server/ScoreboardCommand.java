package net.aufdemrand.denizen.scripts.commands.server;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;

import net.aufdemrand.denizen.exceptions.CommandExecutionException;
import net.aufdemrand.denizen.exceptions.InvalidArgumentsException;
import net.aufdemrand.denizen.objects.Element;
import net.aufdemrand.denizen.objects.aH;
import net.aufdemrand.denizen.objects.dList;
import net.aufdemrand.denizen.objects.dPlayer;
import net.aufdemrand.denizen.scripts.ScriptEntry;
import net.aufdemrand.denizen.scripts.commands.AbstractCommand;
import net.aufdemrand.denizen.utilities.debugging.dB;


/**
 * Add or removes viewers, objectives and scores from scoreboards.
 *
 * @author David Cernat
 */

public class ScoreboardCommand extends AbstractCommand {

    public static Map<String, Scoreboard> scoreboards = new HashMap<String, Scoreboard>();
    private enum Action { ADD, REMOVE }

    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {

        // Initialize necessary fields
        for (aH.Argument arg : aH.interpret(scriptEntry.getArguments())) {

            if (!scriptEntry.hasObject("action")
                && arg.matchesEnum(Action.values())) {
               scriptEntry.addObject("action", arg.asElement());
            }

            else if (!scriptEntry.hasObject("lines")
                     && arg.matchesPrefix("lines, l")) {
                scriptEntry.addObject("lines", arg.asElement());
            }

            else if (!scriptEntry.hasObject("id")
                     && arg.matchesPrefix("id")) {
                scriptEntry.addObject("id", arg.asElement());
            }

            else if (!scriptEntry.hasObject("objective")
                     && arg.matchesPrefix("objective, obj, o")) {
                scriptEntry.addObject("objective", arg.asElement());
            }

            else if (!scriptEntry.hasObject("criteria")
                     && arg.matchesPrefix("criteria, c")) {
                scriptEntry.addObject("criteria", arg.asElement());
            }

            else if (!scriptEntry.hasObject("score")
                     && arg.matchesPrimitive(aH.PrimitiveType.Integer)) {
                scriptEntry.addObject("score", arg.asElement());
            }

            else if (!scriptEntry.hasObject("displayslot")
                     && (arg.matchesEnum(DisplaySlot.values()) ||
                         arg.matches("none"))) {
                scriptEntry.addObject("displayslot", arg.asElement());
            }

            else if (!scriptEntry.hasObject("viewers")
                     && arg.matchesArgumentList(dPlayer.class)) {
                scriptEntry.addObject("viewers", ((dList) arg.asType(dList.class)).filter(dPlayer.class));
            }

            else dB.echoError(dB.Messages.ERROR_UNKNOWN_ARGUMENT, arg.raw_value);
        }

        scriptEntry.defaultObject("action", new Element("add"));
        scriptEntry.defaultObject("id", new Element("main"));
        scriptEntry.defaultObject("criteria", new Element("dummy"));
        scriptEntry.defaultObject("displayslot", new Element("sidebar"));
    }

    @SuppressWarnings("unchecked")
    @Override
    public void execute(final ScriptEntry scriptEntry) throws CommandExecutionException {
        // Get objects

        List<dPlayer> viewers = (List<dPlayer>) scriptEntry.getObject("viewers");
        dList lines = scriptEntry.hasObject("lines") ?
                        dList.valueOf(scriptEntry.getElement("lines").asString()) :
                        new dList();

        Element action = scriptEntry.getElement("action");
        Element id = scriptEntry.getElement("id");
        Element objective = scriptEntry.getElement("objective");
        Element criteria = scriptEntry.getElement("criteria");
        Element score = scriptEntry.getElement("score");
        Element displaySlot = scriptEntry.getElement("displayslot");
        Action act = Action.valueOf(action.asString().toUpperCase());

        // Report to dB
        dB.report(getName(), action.debug() +
                             id.debug() +
                             (viewers != null ? aH.debugObj("viewers", viewers.toString()) : "") +
                             (objective != null ? objective.debug() : "") +
                             (act.equals(Action.ADD) ? criteria.debug() : "") +
                             (!lines.isEmpty() ? lines.debug() : "") +
                             (score != null && act.equals(Action.ADD)
                                 ? score.debug()
                                 : "") +
                             (act.equals(Action.ADD) ? displaySlot.debug() : ""));

        ScoreboardManager manager = Bukkit.getScoreboardManager();
        Scoreboard board = null;

        // Get the main scoreboard by default
        if (id.asString().equalsIgnoreCase("main")) {
            board = manager.getMainScoreboard();
        }
        else {
            // If this scoreboard already exists, get it
            if (scoreboards.containsKey(id.asString().toUpperCase())) {
                board = scoreboards.get(id.asString().toUpperCase());
            }
            // Else, create a new one if Action is ADD
            else if (act.equals(Action.ADD)) {
                board = manager.getNewScoreboard();
                scoreboards.put(id.asString().toUpperCase(), board);
            }
        }

        // Don't progress if we ended up with a null board
        if (board == null) {
            dB.echoError("Scoreboard " + id.asString() + " does not exist!");
            return;
        }

        Objective obj = null;

        if (act.equals(Action.ADD)) {

            if (objective != null) {
                // Try getting the objective from the board
                obj = board.getObjective(objective.asString());

                // Create the objective if it does not already exist
                if (obj == null) {
                    obj = board.registerNewObjective(objective.asString(), criteria.asString());
                }
                // If a different criteria has been set for this objective,
                // recreate the objective
                else if (criteria != null && !obj.getCriteria().equals(criteria.asString())) {
                    obj.unregister();
                    obj = board.registerNewObjective(objective.asString(), criteria.asString());
                }

                // Change the objective's display slot
                if (!displaySlot.asString().equalsIgnoreCase("none")) {
                    obj.setDisplaySlot(DisplaySlot.valueOf(displaySlot.asString().toUpperCase()));
                }

                obj.setDisplayName(objective.asString());

                if (!lines.isEmpty()) {
                    // Set all the score lines in the scoreboard, creating fake players
                    // for those lines that are not meant to track players
                    //
                    // Read https://forums.bukkit.org/threads/help-with-multi-line-scoreboards.181149/
                    // for clarifications
                    for (String line : lines) {
                        line = line.replaceAll("[pP]@", "");

                        Score sc = null;
                        sc = obj.getScore(Bukkit.getOfflinePlayer(line));

                        if (score != null) sc.setScore(score.asInt());
                        // If the score is 0, it won't normally be displayed at first,
                        // so force it to be displayed by using setScore() like below on it
                        else if (sc.getScore() == 0) {
                            sc.setScore(1); sc.setScore(0);
                        }
                    }
                }
            }
        }
        else if (act.equals(Action.REMOVE)) {
            if (objective != null) {
                // Try getting the objective from the board
                obj = board.getObjective(objective.asString());

                if (obj != null) {
                    // Remove the entire objective if no lines have been specified
                    if (lines.isEmpty()) obj.unregister();
                    else {
                        for (String line : lines) {
                            // There is no method to remove a single score from an
                            // objective, as confirmed here:
                            // https://bukkit.atlassian.net/browse/BUKKIT-4014
                            //
                            // So use crazy workaround below where we delete all the
                            // scores, then put them back in except for the one we wanted
                            // to delete
                            line = line.replaceAll("[pP]@", "");

                            Map<String, Integer> scoreMap = new HashMap<String, Integer>();
                            for (Score sc : board.getScores(Bukkit.getOfflinePlayer(line))) {
                                if (!sc.getObjective().equals(obj)) {
                                    scoreMap.put(sc.getObjective().getName(), sc.getScore());
                                }
                            }

                            board.resetScores(Bukkit.getOfflinePlayer(line));

                            for (Map.Entry<String, Integer> entry : scoreMap.entrySet()) {
                                board.getObjective(entry.getKey())
                                     .getScore(Bukkit.getOfflinePlayer(line))
                                     .setScore(entry.getValue());
                            }
                        }
                    }
                }
                else {
                    dB.echoError("Objective " + objective.asString() +
                                 " does not exist in scoreboard " + id.asString());
                }

            }
            // Only remove all objectives from scoreboard if viewers
            // argument was not specified (because if it was, a list
            // of viewers should be removed instead)
            else if (viewers == null) {
                dB.echoDebug("Removing all objectives from scoreboard " + id.asString());
                for (Objective o : board.getObjectives()) {
                    o.unregister();
                }
            }
        }

        if (viewers != null) {
            for (dPlayer viewer : viewers) {
                // Add viewers to this scoreboard
                if (act.equals(Action.ADD))
                    viewer.getPlayerEntity().setScoreboard(board);
                // Remove viewers from this scoreboard by giving them
                // a blank scoreboard (in lieu of better methods provided
                // by Bukkit)
                else if (act.equals(Action.REMOVE))
                    viewer.getPlayerEntity().setScoreboard(manager.getNewScoreboard());
            }
        }
    }
}

package net.aufdemrand.denizen.scripts.commands.server;

import net.aufdemrand.denizen.objects.dPlayer;
import net.aufdemrand.denizen.utilities.FakeOfflinePlayer;
import net.aufdemrand.denizen.utilities.ScoreboardHelper;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.aufdemrand.denizencore.exceptions.CommandExecutionException;
import net.aufdemrand.denizencore.exceptions.InvalidArgumentsException;
import net.aufdemrand.denizencore.objects.Element;
import net.aufdemrand.denizencore.objects.aH;
import net.aufdemrand.denizencore.objects.dList;
import net.aufdemrand.denizencore.scripts.ScriptEntry;
import net.aufdemrand.denizencore.scripts.commands.AbstractCommand;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;

import java.util.List;

public class ScoreboardCommand extends AbstractCommand {

    private enum Action {ADD, REMOVE}

    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {

        // Initialize necessary fields
        for (aH.Argument arg : aH.interpret(scriptEntry.getArguments())) {

            if (!scriptEntry.hasObject("action")
                    && arg.matchesEnum(Action.values())) {
                scriptEntry.addObject("action", arg.asElement());
            }
            else if (!scriptEntry.hasObject("lines")
                    && arg.matchesPrefix("lines", "l")) {
                scriptEntry.addObject("lines", arg.asElement());
            }
            else if (!scriptEntry.hasObject("id")
                    && arg.matchesPrefix("id")) {
                scriptEntry.addObject("id", arg.asElement());
            }
            else if (!scriptEntry.hasObject("objective")
                    && arg.matchesPrefix("objective", "obj", "o")) {
                scriptEntry.addObject("objective", arg.asElement());
            }
            else if (!scriptEntry.hasObject("criteria")
                    && arg.matchesPrefix("criteria", "c")) {
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
                scriptEntry.addObject("viewers", arg.asType(dList.class).filter(dPlayer.class));
            }
            else {
                arg.reportUnhandled();
            }
        }

        scriptEntry.defaultObject("action", new Element("add"));
        scriptEntry.defaultObject("id", new Element("main"));
        scriptEntry.defaultObject("criteria", new Element("dummy"));
        scriptEntry.defaultObject("displayslot", new Element("sidebar"));
    }

    public static OfflinePlayer getOfflinePlayer(String name) {
        if (dPlayer.playerNameIsValid(name)) {
            return Bukkit.getOfflinePlayer(name);
        }
        else {
            return new FakeOfflinePlayer(name);
        }
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
        if (scriptEntry.dbCallShouldDebug()) {
            dB.report(scriptEntry, getName(), action.debug() +
                    id.debug() +
                    (viewers != null ? aH.debugObj("viewers", viewers.toString()) : "") +
                    (objective != null ? objective.debug() : "") +
                    (act.equals(Action.ADD) && objective != null
                            ? criteria.debug()
                            : "") +
                    (!lines.isEmpty() ? lines.debug() : "") +
                    (act.equals(Action.ADD) && score != null
                            ? score.debug()
                            : "") +
                    (act.equals(Action.ADD) && objective != null
                            ? displaySlot.debug()
                            : ""));
        }

        Scoreboard board = null;

        // Get the main scoreboard by default
        if (id.asString().equalsIgnoreCase("main")) {
            board = ScoreboardHelper.getMain();
        }
        else {
            // If this scoreboard already exists, get it
            if (ScoreboardHelper.hasScoreboard(id.asString())) {
                board = ScoreboardHelper.getScoreboard(id.asString());
            }
            // Else, create a new one if Action is ADD
            else if (act.equals(Action.ADD)) {
                board = ScoreboardHelper.createScoreboard(id.asString());
            }
        }

        // Don't progress if we ended up with a null board
        if (board == null) {
            dB.echoError(scriptEntry.getResidingQueue(), "Scoreboard " + id.asString() + " does not exist!");
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
                    // If we've gotten this far, but the score is null,
                    // use a score of 0
                    if (score == null) {
                        score = new Element(0);
                    }

                    // Set all the score lines in the scoreboard, creating fake players
                    // for those lines that are not meant to track players
                    //
                    // Read https://forums.bukkit.org/threads/help-with-multi-line-scoreboards.181149/
                    // for clarifications
                    for (String line : lines) {
                        line = line.replaceAll("[pP]@", "");
                        if (line.length() > 48) {
                            line = line.substring(0, 48);
                        }
                        ScoreboardHelper.addScore(obj, getOfflinePlayer(line), score.asInt());
                    }
                }
            }
            // If there is no objective and no viewers, but there are some lines,
            // the command cannot do anything at all, so print a message about that
            else if (viewers == null && !lines.isEmpty()) {
                dB.echoDebug(scriptEntry, "Cannot add lines without specifying an objective!");
            }
        }
        else if (act.equals(Action.REMOVE)) {
            if (objective != null) {
                // Try getting the objective from the board
                obj = board.getObjective(objective.asString());

                if (obj != null) {
                    // Remove the entire objective if no lines have been specified
                    if (lines.isEmpty()) {
                        dB.echoDebug(scriptEntry, "Removing objective " + obj.getName() +
                                " from scoreboard " + id.asString());
                        obj.unregister();
                    }
                    else {
                        for (String line : lines) {
                            line = line.replaceAll("[pP]@", "");
                            ScoreboardHelper.removeScore(obj, getOfflinePlayer(line));
                        }
                    }
                }
                else {
                    dB.echoError(scriptEntry.getResidingQueue(), "Objective " + objective.asString() +
                            " does not exist in scoreboard " + id.asString());
                }
            }
            // If lines were specified, but an objective was not, remove the
            // lines from every objective
            else if (!lines.isEmpty()) {
                dB.echoDebug(scriptEntry, "Removing lines " + lines.identify() +
                        " from all objectives in scoreboard " + id.asString());

                for (String line : lines) {
                    line = line.replaceAll("[pP]@", "");
                    ScoreboardHelper.removePlayer(id.asString(), getOfflinePlayer(line));
                }
            }
            // Only remove all objectives from scoreboard if viewers
            // argument was not specified (because if it was, a list
            // of viewers should be removed instead)
            else if (viewers == null) {
                dB.echoDebug(scriptEntry, "Removing scoreboard " + id.asString());
                ScoreboardHelper.deleteScoreboard(id.asString());
            }
        }

        if (viewers != null) {
            for (dPlayer viewer : viewers) {
                // Add viewers for this scoreboard
                if (act.equals(Action.ADD)) {
                    // If this isn't the main scoreboard, add this viewer
                    // to the map of viewers saved by Denizen
                    if (!id.asString().equalsIgnoreCase("main")) {
                        ScoreboardHelper.viewerMap.put(viewer.getName(), id.asString());
                    }

                    // Make this player view the scoreboard if he/she
                    // is already online
                    if (viewer.isOnline()) {
                        viewer.getPlayerEntity().setScoreboard(board);
                    }
                }
                // Remove viewers for this scoreboard
                else if (act.equals(Action.REMOVE)) {
                    // Take this player out of the map of viewers
                    ScoreboardHelper.viewerMap.remove(viewer.getName());

                    // Make the player view a blank scoreboard if he/she
                    // is online (in lieu of a scoreboard-removing method
                    // provided by Bukkit)
                    if (viewer.isOnline()) {
                        viewer.getPlayerEntity().setScoreboard(ScoreboardHelper.createScoreboard());
                    }
                }
            }
        }
    }
}

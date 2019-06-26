package net.aufdemrand.denizen.scripts.commands.server;

import net.aufdemrand.denizen.objects.dPlayer;
import net.aufdemrand.denizen.utilities.FakeOfflinePlayer;
import net.aufdemrand.denizen.utilities.ScoreboardHelper;
import net.aufdemrand.denizen.utilities.debugging.dB;
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

    // <--[command]
    // @Name Scoreboard
    // @Syntax scoreboard ({add}/remove) (viewers:<player>|...) (lines:<player>/<text>|...) (id:<value>/{main}) (objective:<value>) (criteria:<criteria>/{dummy}) (score:<#>) (displayslot:<value>/{sidebar}/none)
    // @Required 1
    // @Short Add or removes viewers, objectives and scores from scoreboards.
    // @Group server
    //
    // @Description
    // Lets you make players see a certain scoreboard and then a certain objective in that scoreboard.
    //
    // There are currently three slots where objectives can be displayed: in the sidebar on the right of
    // the screen, below player names and in the player list that shows up when you press Tab. The names
    // of these slots can be found here:
    // http://jd.bukkit.org/rb/apidocs/org/bukkit/scoreboard/DisplaySlot.html
    //
    // Every objective has several lines of scores. Technically, the lines track players, but fake player
    // names can be used by Denizen to let you call the lines anything you want.
    //
    // When using the sidebar as the display slot, all the scores set for an objective will be displayed
    // there, but you will need to put actual player names in the lines to be able to use the below_name
    // display slot (which displays each player's score underneath his/her name) and the player_list
    // display slot (which displays each player's score to the right of his/her name in the player list).
    //
    // If you do not specify a display slot, the sidebar will be used. You can also use "none" as the
    // display slot if you want to add a hidden objective without automatically making it get displayed.
    //
    // You can set scores manually, or you can use different Minecraft criteria that set and update the
    // scores automatically. A list of these criteria can be found here:
    // http://minecraft.gamepedia.com/Scoreboard#Objectives
    //
    // You can use the "remove" argument to remove different parts of scoreboards. The more arguments
    // you use with it, the more specific your removal will be. For example, if you only use the "remove"
    // argument and the "id" argument, you will completely remove all the objectives in a scoreboard,
    // but if you specify an objective as well, you will only delete that one objective from that
    // scoreboard, and if you also specify certain lines, you will only delete those specific lines from
    // that objective. Similarly, if you use the "remove" argument along with the "id" and "viewers"
    // arguments, you will only remove those viewers from the scoreboard, not the entire scoreboard.
    //
    // @Tags
    // <server.scoreboard[(<board>)].exists>
    // <server.scoreboard[(<board>)].team_members[<team>]>
    //
    // @Usage
    // Add a score for the player "mythan" to the default scoreboard under the objective "cookies" and let him see it
    // - scoreboard add obj:cookies lines:mythan score:1000 viewers:p@mythan
    //
    // @Usage
    // Add a new current objective called "food" to the "test" scoreboard with 3 lines that each have a score of 50:
    // - scoreboard add id:test obj:food lines:Cookies|Donuts|Cake score:50
    //
    // @Usage
    // Make a list of players see the scoreboard that has the id "test":
    // - scoreboard add viewers:p@Goma|p@mythan|p@Ares513 id:test
    //
    // @Usage
    // Change the value of one of the scores in the "food" objective:
    // - scoreboard add id:test obj:food lines:Cake score:9000
    //
    // @Usage
    // Remove one of the lines from the "food" objective in the "test" scoreboard
    // - scoreboard remove obj:food lines:Donuts
    //
    // @Usage
    // Remove one of the viewers of the "test" scoreboard:
    // - scoreboard remove viewers:p@mythan
    //
    // @Usage
    // Make the player "dimensionZ" see the health of other players below their names
    // - scoreboard add viewers:p@dimensionZ id:test obj:anything criteria:health displayslot:below_name
    //
    // @Usage
    // Make all the players on the world "survival" see each other's number of entity kills in the player list when pressing Tab
    // - scoreboard add "viewers:<w@survival.players>" id:test obj:anything criteria:totalKillCount displayslot:player_list
    // -->

    private enum Action {ADD, REMOVE}

    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {

        // Initialize necessary fields
        for (aH.Argument arg : aH.interpretArguments(scriptEntry.aHArgs)) {

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
                scriptEntry.addObject("viewers", arg.asType(dList.class).filter(dPlayer.class, scriptEntry));
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
    public void execute(final ScriptEntry scriptEntry) {
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

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

// <--[example]
// @Title Smooth Scoreboard Updates
// @Description
// Use this method to create scoreboards that update smoothly (without jitter).
//
// @Code
// # How to make smoothly updating global and player-specific scoreboards using Denizen.
// # Most people seem to experience glitching and jittering in the scoreboard when attempting to make an updating scoreboard,
// # so I'm going to show you my method that is mostly glitch free.
//
// # we're going to start with creating player specific scoreboards (these will run as the player,
// # allowing you to pull specific tags that fill in differently for each player, such as p@player.location, p@player.health, and many more.)
// # First we create a script that loops itself.
// looping_scoreboard_script:
//   type: task
//   script:
//   - foreach <server.list_online_players> {
//     - run player_specific_scoreboard_script player:<def[value]> instantly
//     }
//   - run looping_scoreboard_script instantly delay:19t
//   # You can include an IF statement to ensure that only people who choose to, will be shown their scoreboard.
//   # I'm using 19 ticks as a delay, to make sure it syncs up with other looping scripts as few times as possible
//   # (assuming that those looping scripts are using an even, bigger number as delay, such as 20 ticks, 5 seconds, etc..)
//   # whist still updating faster than seconds pass.
//
// # Now we'll create the script that runs as each player.
// player_specific_scoreboard_script:
//   type: task
//   script:
//   - define ID <player.name><server.current_time_millis>
//   - scoreboard add id:<def[ID]>
//   # We create the new scoreboard, with a unique player specific ID. We're using the server.current_time_millis
//   # tag to ensure that each time it loops it'll have a unique ID that can't be identical to the previous one at all.
//   - scoreboard add id:<def[ID]> "objective:<&6>Stats" "lines:<&c>Health<&co>" score:15
//   - scoreboard add id:<def[ID]> "objective:<&6>Stats" "lines:<&e><player.health.percentage||0><&pc>" score:14
//   - scoreboard add id:<def[ID]> "objective:<&6>Stats" "lines:<&c>Location<&co>" score:13
//   - scoreboard add id:<def[ID]> "objective:<&6>Stats" "lines:<&e><player.location.simple||nowhere>" score:12
//   # Note; we are using scores to ensure the position of the line inside the scoreboard. If 2
//   # lines have an equal score they will be ordered alphabetically.
//   - scoreboard add id:<def[ID]> "objective:<&6>Stats" "lines:<&c>Ping<&co>" score:11
//   - scoreboard add id:<def[ID]> "objective:<&6>Stats" "lines:<&e><player.ping||-1>" score:10
//   - scoreboard add id:<def[ID]> viewers:<player>
//   # Now we add the player to the scoreboard.
//   - wait 2s
//   - scoreboard remove id:<def[ID]>
//   # Lastly, we remove the scoreboard again, after a short delay.
//   # This delay has to be longer than the delay on the looping script to ensure smoothness!
//
// # You can ensure this script runs at all times by creating a world script with the
// # 'on server start' event, that runs the looping script.
// scoreboard_initiator:
//   type: world
//   events:
//     on server start:
//     - run looping_scoreboard_script instantly
//
// # Additional notes: If you're using multiple variables that could return the same result, make
// # the lines unique by using different colour code combinations.
// # For example: "lines:<&a><&e><player.health>" and "lines:<&b><&e><player.saturation>"
//
// # The maximum character length of a scoreboard line is 48. This includes colour codes!
// # (not including the <>'s.) If you're unsure whether a tag will fill in with a string longer
// # than 48 characters, use the substring tag.
// # For example: <player.flag[flagname].substring[1,48]>
//
// # To make a global scoreboard instead, make the script with the scoreboard commands loop on it's own,
// # and add viewers:<server.list_online_players> instead.
// # An example of this script in action: <@link url http://i.imgur.com/2tmQxff.png>
//
// -->

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

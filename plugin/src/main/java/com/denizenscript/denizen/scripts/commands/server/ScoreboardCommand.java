package com.denizenscript.denizen.scripts.commands.server;

import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizen.utilities.Utilities;
import com.denizenscript.denizen.utilities.ScoreboardHelper;
import com.denizenscript.denizencore.utilities.debugging.Debug;
import com.denizenscript.denizen.objects.PlayerTag;
import com.denizenscript.denizencore.exceptions.InvalidArgumentsException;
import com.denizenscript.denizencore.objects.Argument;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.core.ListTag;
import com.denizenscript.denizencore.scripts.ScriptEntry;
import com.denizenscript.denizencore.scripts.commands.AbstractCommand;
import com.denizenscript.denizencore.utilities.CoreUtilities;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.RenderType;
import org.bukkit.scoreboard.Scoreboard;

import java.util.List;

public class ScoreboardCommand extends AbstractCommand {

    public ScoreboardCommand() {
        setName("scoreboard");
        setSyntax("scoreboard ({add}/remove) (viewers:<player>|...) (lines:<player>/<text>|...) (id:<value>/player/{main}) (objective:<value>) (criteria:<criteria>/{dummy}) (score:<#>) (displayslot:<value>/{sidebar}/none) (displayname:<name>) (rendertype:<type>)");
        setRequiredArguments(1, 10);
        isProcedural = false;
    }

    // <--[command]
    // @Name Scoreboard
    // @Syntax scoreboard ({add}/remove) (viewers:<player>|...) (lines:<player>/<text>|...) (id:<value>/player/{main}) (objective:<value>) (criteria:<criteria>/{dummy}) (score:<#>) (displayslot:<value>/{sidebar}/none) (displayname:<name>) (rendertype:<type>)
    // @Required 1
    // @Maximum 10
    // @Short Add or removes viewers, objectives and scores from scoreboards.
    // @Group server
    //
    // @Description
    // Lets you make players see a certain scoreboard and then a certain objective in that scoreboard.
    // Please note that a 'scoreboard' is NOT a 'sidebar' - if you want that thing where text is on the right side of the screen, use <@link command sidebar>.
    // 'Scoreboard' is the underlying internal system that powers sidebars, below_name plates, team prefixing, and a lot of other systems. Generally, you should avoid using this command directly.
    //
    // The ID can be 'main' for the global default scoreboard, 'player' for the attached player's current scoreboard, or any other value for a custom named scoreboard.
    //
    // There are currently three slots where objectives can be displayed:
    // in the sidebar on the right of the screen, below player names and in the player list that shows up when you press Tab.
    // The names of these slots can be found here: <@link url https://hub.spigotmc.org/javadocs/spigot/org/bukkit/scoreboard/DisplaySlot.html>
    //
    // Every objective has several lines of scores.
    // Technically, the lines track players, but fake player names can be used by Denizen to let you call the lines anything you want.
    //
    // When using the sidebar as the display slot, all the scores set for an objective will be displayed there,
    // but you will need to put actual player names in the lines to be able to use
    // the below_name display slot (which displays each player's score underneath his/her name) and
    // the player_list display slot (which displays each player's score to the right of his/her name in the player list).
    //
    // If you do not specify a display slot, the sidebar will be used. You can also use "none" as the
    // display slot if you want to add a hidden objective without automatically making it get displayed.
    // If the object already exists, and you don't specify the display slot, it will use the existing setting.
    //
    // When setting an objective, you can also optionally set the display name by using the "displayname:" argument.
    //
    // "Rendertype" can be "INTEGER" or "HEARTS". Defaults to integer.
    //
    // You can set scores manually, or you can use different Minecraft criteria that set and update the scores automatically.
    // A list of these criteria can be found here: <@link url https://minecraft.wiki/w/Scoreboard#Objectives>
    // If the object already exists, and you don't specify the criteria, it will use the existing setting.
    //
    // You can use the "remove" argument to remove different parts of scoreboards.
    // The more arguments you use with it, the more specific your removal will be.
    // For example, if you only use the "remove" argument and the "id" argument, you will completely remove all the objectives in a scoreboard,
    // but if you specify an objective as well, you will only delete that one objective from that scoreboard,
    // and if you also specify certain lines, you will only delete those specific lines from that objective.
    // Similarly, if you use the "remove" argument along with the "id" and "viewers" arguments, you will only remove those viewers from the scoreboard, not the entire scoreboard.
    //
    // @Tags
    // <server.scoreboard[(<board>)].exists>
    // <server.scoreboard[(<board>)].team[<team>].members>
    // <PlayerTag.scoreboard_id>
    //
    // @Usage
    // Add a score for the defined player to the default scoreboard under the objective "cookies" and let him see it
    // - scoreboard add obj:cookies lines:joe score:1000 viewers:<[aplayer]>
    //
    // @Usage
    // Add a new current objective called "food" to the "test" scoreboard with 3 lines that each have a score of 50:
    // - scoreboard add id:test obj:food lines:Cookies|Donuts|Cake score:50
    //
    // @Usage
    // Make a list of players see the scoreboard that has the id "test":
    // - scoreboard add viewers:<[player]>|<[aplayer]>|<[thatplayer]> id:test
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
    // - scoreboard remove viewers:<[aplayer]>
    //
    // @Usage
    // Make the defined player see the health of other players below their names
    // - scoreboard add viewers:<[player]> id:test obj:anything criteria:health displayslot:below_name
    //
    // @Usage
    // Make all the players on the world "survival" see each other's number of entity kills in the player list when pressing Tab
    // - scoreboard add viewers:<world[survival].players> id:test obj:anything criteria:totalKillCount displayslot:player_list
    // -->

    private enum Action {ADD, REMOVE}

    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {
        for (Argument arg : scriptEntry) {
            if (!scriptEntry.hasObject("action")
                    && arg.matchesEnum(Action.class)) {
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
                    && arg.matchesInteger()) {
                scriptEntry.addObject("score", arg.asElement());
            }
            else if (!scriptEntry.hasObject("displayslot")
                    && (arg.matchesEnum(DisplaySlot.class) ||
                    arg.matches("none"))) {
                scriptEntry.addObject("displayslot", arg.asElement());
            }
            else if (!scriptEntry.hasObject("displayslot")
                    && arg.matchesPrefix("displayname")) {
                scriptEntry.addObject("displayname", arg.asElement());
            }
            else if (!scriptEntry.hasObject("rendertype")
                    && arg.matchesPrefix("rendertype")
                    && arg.matchesEnum(RenderType.class)) {
                scriptEntry.addObject("rendertype", arg.asElement());
            }
            else if (!scriptEntry.hasObject("viewers")
                    && arg.matchesArgumentList(PlayerTag.class)) {
                scriptEntry.addObject("viewers", arg.asType(ListTag.class).filter(PlayerTag.class, scriptEntry));
            }
            else {
                arg.reportUnhandled();
            }
        }
        scriptEntry.defaultObject("action", new ElementTag("add"));
        scriptEntry.defaultObject("id", new ElementTag("main"));
    }

    public static String checkLine(ObjectTag obj) {
        if (obj instanceof PlayerTag) {
            return ((PlayerTag) obj).getName();
        }
        else if (obj instanceof EntityTag && ((EntityTag) obj).isSpawned()) {
            Entity ent = ((EntityTag) obj).getBukkitEntity();
            if (ent instanceof Player) {
                return ent.getName();
            }
            return ent.getUniqueId().toString();
        }
        String raw = obj.toString();
        if (raw.startsWith("p@")) {
            PlayerTag player = PlayerTag.valueOf(raw, CoreUtilities.noDebugContext);
            if (player != null) {
                return player.getName();
            }
        }
        return raw;
    }

    @Override
    public void execute(final ScriptEntry scriptEntry) {
        List<PlayerTag> viewers = (List<PlayerTag>) scriptEntry.getObject("viewers");
        ListTag lines = scriptEntry.hasObject("lines") ? ListTag.valueOf(scriptEntry.getElement("lines").asString(), scriptEntry.getContext()) : new ListTag();
        ElementTag action = scriptEntry.getElement("action");
        ElementTag id = scriptEntry.getElement("id");
        ElementTag objective = scriptEntry.getElement("objective");
        ElementTag criteria = scriptEntry.getElement("criteria");
        ElementTag score = scriptEntry.getElement("score");
        ElementTag displaySlot = scriptEntry.getElement("displayslot");
        ElementTag displayName = scriptEntry.getElement("displayname");
        ElementTag renderType = scriptEntry.getElement("rendertype");
        Action act = Action.valueOf(action.asString().toUpperCase());
        boolean hadCriteria = criteria != null;
        boolean hadDisplaySlot = displaySlot != null;
        if (!hadCriteria) {
            criteria = new ElementTag("dummy");
        }
        if (!hadDisplaySlot) {
            displaySlot = new ElementTag("sidebar");
        }
        if (scriptEntry.dbCallShouldDebug()) {
            Debug.report(scriptEntry, getName(), action, id, db("viewers", viewers), objective, lines, score, objective, displaySlot, criteria, displayName, renderType);
        }
        Scoreboard board = null;
        // Get the main scoreboard by default
        if (id.asString().equalsIgnoreCase("main")) {
            board = ScoreboardHelper.getMain();
        }
        else if (id.asString().equalsIgnoreCase("player")) {
            board = Utilities.getEntryPlayer(scriptEntry).getPlayerEntity().getScoreboard();
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
            Debug.echoError(scriptEntry, "Scoreboard " + id.asString() + " does not exist!");
            return;
        }
        Objective obj;
        if (act.equals(Action.ADD)) {
            if (objective != null) {
                // Try getting the objective from the board
                obj = board.getObjective(objective.asString());
                boolean existedAlready = obj != null;
                // Create the objective if it does not already exist
                if (obj == null) {
                    obj = board.registerNewObjective(objective.asString(), criteria.asString());
                }
                // If a different criteria has been set for this objective,
                // recreate the objective
                else if (hadCriteria && !obj.getCriteria().equals(criteria.asString())) {
                    obj.unregister();
                    obj = board.registerNewObjective(objective.asString(), criteria.asString());
                }
                // Change the objective's display slot
                if ((!existedAlready || hadDisplaySlot) && !displaySlot.asString().equalsIgnoreCase("none")) {
                    obj.setDisplaySlot(DisplaySlot.valueOf(displaySlot.asString().toUpperCase()));
                }
                if (renderType != null) {
                    obj.setRenderType(RenderType.valueOf(renderType.asString().toUpperCase()));
                }
                if (displayName != null) {
                    obj.setDisplayName(displayName.asString());
                }
                else if (!existedAlready) {
                    obj.setDisplayName(objective.asString());
                }
                if (!lines.isEmpty()) {
                    // If we've gotten this far, but the score is null,
                    // use a score of 0
                    if (score == null) {
                        score = new ElementTag(0);
                    }

                    for (ObjectTag line : lines.objectForms) {
                        ScoreboardHelper.addScore(obj, checkLine(line), score.asInt());
                    }
                }
            }
            // If there is no objective and no viewers, but there are some lines,
            // the command cannot do anything at all, so print a message about that
            else if (viewers == null && !lines.isEmpty()) {
                Debug.echoDebug(scriptEntry, "Cannot add lines without specifying an objective!");
            }
        }
        else if (act.equals(Action.REMOVE)) {
            if (objective != null) {
                // Try getting the objective from the board
                obj = board.getObjective(objective.asString());
                if (obj != null) {
                    // Remove the entire objective if no lines have been specified
                    if (lines.isEmpty()) {
                        Debug.echoDebug(scriptEntry, "Removing objective " + obj.getName() +
                                " from scoreboard " + id.asString());
                        obj.unregister();
                    }
                    else {
                        for (ObjectTag line : lines.objectForms) {
                            ScoreboardHelper.removeScore(obj, checkLine(line));
                        }
                    }
                }
                else {
                    Debug.echoError(scriptEntry, "Objective " + objective.asString() +
                            " does not exist in scoreboard " + id.asString());
                }
            }
            // If lines were specified, but an objective was not, remove the
            // lines from every objective
            else if (!lines.isEmpty()) {
                Debug.echoDebug(scriptEntry, "Removing lines " + lines.identify() +
                        " from all objectives in scoreboard " + id.asString());

                for (ObjectTag line : lines.objectForms) {
                    ScoreboardHelper.removePlayer(id.asString(), checkLine(line));
                }
            }
            // Only remove all objectives from scoreboard if viewers
            // argument was not specified (because if it was, a list
            // of viewers should be removed instead)
            else if (viewers == null) {
                Debug.echoDebug(scriptEntry, "Removing scoreboard " + id.asString());
                ScoreboardHelper.deleteScoreboard(id.asString());
            }
        }
        if (viewers != null) {
            for (PlayerTag viewer : viewers) {
                // Add viewers for this scoreboard
                if (act.equals(Action.ADD)) {
                    // If this isn't the main scoreboard, add this viewer
                    // to the map of viewers saved by Denizen
                    if (!id.asString().equalsIgnoreCase("main")) {
                        ScoreboardHelper.viewerMap.put(viewer.getUUID(), id.asString());
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
                    ScoreboardHelper.viewerMap.remove(viewer.getUUID());

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

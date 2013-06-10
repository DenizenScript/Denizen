package net.aufdemrand.denizen.scripts.commands.server;

import net.aufdemrand.denizen.exceptions.CommandExecutionException;
import net.aufdemrand.denizen.exceptions.InvalidArgumentsException;
import net.aufdemrand.denizen.scripts.ScriptEntry;
import net.aufdemrand.denizen.scripts.commands.AbstractCommand;
import net.aufdemrand.denizen.objects.aH;
import net.aufdemrand.denizen.objects.aH.ArgumentType;
import net.aufdemrand.denizen.utilities.debugging.dB;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Scoreboard;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 *
 * TODO: Document usage
 *
 * Controls scoreboards.
 *
 * @author aufdemrand
 *
 */
public class ScoreboardCommand extends AbstractCommand {

    enum Action { SET, REMOVE, SHOW, HIDE }

    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {

        Action action = null;
        String name = null;
        String id = null;
        Integer priority = null;
        String show = null;
        String value = null;

        // Parse Arguments
        for (String arg : scriptEntry.getArguments()) {

            if (aH.matchesValueArg("SET", arg, ArgumentType.Custom)) {
                action = Action.SET;
                name = aH.getStringFrom(arg);

            } else if (aH.matchesValueArg("REMOVE", arg, ArgumentType.Custom)) {
                action = Action.REMOVE;
                name = aH.getStringFrom(arg);

            } else if (aH.matchesValueArg("PRIORITY", arg, ArgumentType.Integer)) {
                priority = aH.getIntegerFrom(arg);

            } else if (aH.matchesArg("SHOW", arg)) {
                action = Action.SHOW;

            } else if (aH.matchesArg("HIDE", arg)) {
                action = Action.HIDE;

            } else if (aH.matchesValueArg("VALUE", arg, ArgumentType.String)) {
                value = aH.getStringFrom(arg);

            } else  {
                id = aH.getStringFrom(arg);
            }
        }

        scriptEntry.addObject("action", action)
                .addObject("value", value)
                .addObject("name", name)
                .addObject("id", id)
                .addObject("priority", priority)
                .addObject("show", show);
    }

    private Map<String, Scoreboard> scoreboards = new ConcurrentHashMap<String, Scoreboard>();

    private Scoreboard getScoreboard(String id) {
        for (String scoreboard_id : scoreboards.keySet())
            if (scoreboard_id.equalsIgnoreCase(id)) return scoreboards.get(scoreboard_id);
        scoreboards.put(id, Bukkit.getScoreboardManager().getNewScoreboard());
        getScoreboard(id).registerNewTeam(id);
        return null;
    }

    private void removeScoreboard(String id) {
        for (String scoreboard_id : scoreboards.keySet())
            if (scoreboard_id.equalsIgnoreCase(id)) scoreboards.remove(scoreboard_id);
        return;
    }

    @Override
    public void execute(ScriptEntry scriptEntry) throws CommandExecutionException {
        // Get objects

        Action action = (Action) scriptEntry.getObject("action");
        String name = (String) scriptEntry.getObject("name");
        String value = (String) scriptEntry.getObject("value");
        Integer priority = (Integer) scriptEntry.getObject("priority");
        String id = (String) scriptEntry.getObject("id");

        // Report to dB
        dB.report(getName(),
                aH.debugObj("Action", action.toString())
                        + aH.debugObj("Id", id));


        switch (action) {

            case SET:
                Scoreboard scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
                scoreboard.registerNewObjective(id, id);
                scoreboard.getObjective(id).setDisplaySlot(DisplaySlot.BELOW_NAME);
                scoreboard.getObjective(id).setDisplayName("display name");
                scoreboard.registerNewTeam(scriptEntry.getNPC().getName());
                scoreboard.getTeam(scriptEntry.getNPC().getName()).addPlayer(((Player) scriptEntry.getNPC().getEntity()));
                break;

            case REMOVE:
                break;

            case SHOW:
                break;

            case HIDE:
                break;
        }

    }
}
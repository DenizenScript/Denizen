package net.aufdemrand.denizen.scripts.commands.core;

import net.aufdemrand.denizen.exceptions.CommandExecutionException;
import net.aufdemrand.denizen.exceptions.InvalidArgumentsException;
import net.aufdemrand.denizen.npc.dNPC;
import net.aufdemrand.denizen.scripts.ScriptEntry;
import net.aufdemrand.denizen.scripts.commands.AbstractCommand;
import net.aufdemrand.denizen.utilities.arguments.aH;
import net.aufdemrand.denizen.utilities.arguments.aH.ArgumentType;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.aufdemrand.denizen.utilities.debugging.dB.Messages;
import net.aufdemrand.denizen.utilities.scoreboard.Scoreboard;
import net.aufdemrand.denizen.utilities.scoreboard.ScoreboardAPI;
import net.citizensnpcs.trait.Poses;
import org.bukkit.Location;
import org.bukkit.entity.Player;

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

    enum Action { CREATE, DESTROY, SET, REMOVE, SHOW, HIDE }

    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {

        Action action = null;
        String value = null;
        String id = null;
        Integer priority = null;
        String show = null;
        Integer num = null;

        // Parse Arguments
        for (String arg : scriptEntry.getArguments()) {

            if (aH.matchesValueArg("SET", arg, ArgumentType.Custom)) {
                action = Action.SET;
                value = aH.getStringFrom(arg);

            } else if (aH.matchesValueArg("REMOVE", arg, ArgumentType.Custom)) {
                action = Action.REMOVE;
                value = aH.getStringFrom(arg);

            } else if (aH.matchesArg("CREATE, DESTROY", arg)) {
                action = Action.valueOf(arg.toUpperCase());

            } else if (aH.matchesValueArg("PRIORITY", arg, ArgumentType.Integer)) {
                priority = aH.getIntegerFrom(arg);

            } else if (aH.matchesArg("SHOW", arg)) {
                action = Action.SHOW;

            } else if (aH.matchesArg("HIDE", arg)) {
                action = Action.HIDE;

            } else if (aH.matchesValueArg("VALUE", arg, ArgumentType.Integer)) {
                num = aH.getIntegerFrom(arg);

            } else  {
                id = aH.getStringFrom(arg);
            }

        }

        scriptEntry.addObject("action", action)
                .addObject("value", value)
                .addObject("num", num)
                .addObject("id", id)
                .addObject("priority", priority)
                .addObject("show", show);
    }


	@Override
    public void execute(ScriptEntry scriptEntry) throws CommandExecutionException {
        // Get objects

        Action action = (Action) scriptEntry.getObject("action");
        String value = (String) scriptEntry.getObject("value");
        Integer num = (Integer) scriptEntry.getObject("num");
        Integer priority = (Integer) scriptEntry.getObject("priority");
        String id = (String) scriptEntry.getObject("id");

        // Report to dB
        dB.report(getName(),
                aH.debugObj("Action", action.toString())
                        + aH.debugObj("Id", id)
                        + aH.debugObj("Exists?", ScoreboardAPI.getInstance().getScoreboard(id) != null ? "Yes" : "No"));


        switch (action) {

            case CREATE:
                ScoreboardAPI.getInstance().createScoreboard(id, priority);
                ScoreboardAPI.getInstance().getScoreboard(id).setScoreboardName(id);
                ScoreboardAPI.getInstance().getScoreboard(id).setType(Scoreboard.Type.SIDEBAR);
                break;

            case DESTROY:
                ScoreboardAPI.getInstance().getScoreboard(id).stopShowingAllPlayers();
                break;

            case SET:
                dB.echoDebug(id);
                dB.echoDebug(value);
                dB.echoDebug(num.toString());
                ScoreboardAPI.getInstance().getScoreboard(id).setItem(value, num);
                break;

            case REMOVE:
                ScoreboardAPI.getInstance().getScoreboard(id).removeItem(value);
                break;

            case SHOW:
                ScoreboardAPI.getInstance().getScoreboard(id).showToPlayer(scriptEntry.getPlayer());
                break;

            case HIDE:
                ScoreboardAPI.getInstance().getScoreboard(id).showToPlayer(scriptEntry.getPlayer(), false);
                break;
        }

    }
}
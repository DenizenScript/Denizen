package net.aufdemrand.denizen.scripts.commands.core;

import net.aufdemrand.denizen.exceptions.CommandExecutionException;
import net.aufdemrand.denizen.exceptions.InvalidArgumentsException;
import net.aufdemrand.denizen.flags.FlagManager.Flag;
import net.aufdemrand.denizen.scripts.ScriptEntry;
import net.aufdemrand.denizen.scripts.commands.AbstractCommand;
import net.aufdemrand.denizen.utilities.arguments.Duration;
import net.aufdemrand.denizen.utilities.arguments.aH;
import net.aufdemrand.denizen.utilities.debugging.dB;
import org.bukkit.event.Listener;

/**
 * Sets a Player 'Flag'. Flags can hold information to check against
 * with the FLAGGED requirement.
 *
 * @author Jeremy Schroeder
 */

public class FlagCommand extends AbstractCommand implements Listener {

    public enum Action { SET_VALUE, SET_BOOLEAN, INCREASE, DECREASE, MULTIPLY, DIVIDE, INSERT, REMOVE, SPLIT, DELETE }
    public enum Type   { GLOBAL, NPC, PLAYER }

    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {

        // Set some defaults with information from the scriptEntry
        String name = null;
        String value = null;
        Duration duration = new Duration(-1d);
        Action action = Action.SET_VALUE;
        Type type = Type.PLAYER;

        for (String arg : scriptEntry.getArguments()) {

            if (aH.matchesDuration(arg))
                duration = aH.getDurationFrom(arg);

            else if (aH.matchesArg("GLOBAL, NPC, DENIZEN, GLOBAL", arg))
                type = Type.valueOf(arg.toUpperCase().replace("DENIZEN", "NPC"));

                // Determine flagAction and set the flagName/flagValue
            else if (arg.split(":", 3).length > 1) {
                String[] flagArgs = arg.split(":");
                name = flagArgs[0].toUpperCase();

                if (flagArgs.length == 2) {
                    if (flagArgs[1].equals("+")) {
                        action = Action.INCREASE;
                        value = "1";
                    }   else if (flagArgs[1].equals("-")) {
                    	// Using equals instead of startsWith because
                    	// people need to be able to set values like "-2"
                        action = Action.DECREASE;
                        value = "1";
                    }   else if (flagArgs[1].startsWith("!")) {
                        action = Action.DELETE;
                    }   else if (flagArgs[1].startsWith("<-")) {
                        action = Action.REMOVE;
                    }   else {
                        action = Action.SET_VALUE;
                        value = arg.split(":")[1];
                    }
                } else if (flagArgs.length == 3) {
                    if (flagArgs[1].startsWith("->")) action = Action.INSERT;
                    else if (flagArgs[1].startsWith("<-")) action = Action.REMOVE;
                    else if (flagArgs[1].startsWith("|")) action = Action.SPLIT;
                    else if (flagArgs[1].startsWith("+")) action = Action.INCREASE;
                    else if (flagArgs[1].startsWith("-")) action = Action.DECREASE;
                    else if (flagArgs[1].startsWith("*")) action = Action.MULTIPLY;
                    else if (flagArgs[1].startsWith("/")) action = Action.DIVIDE;
                    value = flagArgs[2];
                }
            } else {
                name = arg.toUpperCase();
                action = Action.SET_BOOLEAN;
            }
        }

        String player = null;

        if (type == Type.PLAYER) {
            if (scriptEntry.getOfflinePlayer() != null)
                player = scriptEntry.getOfflinePlayer().getName();
            if (player == null && scriptEntry.getPlayer() != null)
                player = scriptEntry.getPlayer().getName();
        }

        // Check required arguments
        if (name == null)
            throw new InvalidArgumentsException("Must specify a FLAG name.");

        if (type == Type.NPC && scriptEntry.getNPC() == null)
            throw new InvalidArgumentsException("Specified NPC-type flag with no valid NPC reference.");

        if (type == Type.PLAYER && player == null)
            throw new InvalidArgumentsException("Specified PLAYER-type flag with no valid Player reference.");


        // Save objects to ScriptEntry for use with execute()
        scriptEntry.addObject("name", name);
        scriptEntry.addObject("value", value);
        scriptEntry.addObject("duration", duration);
        scriptEntry.addObject("action", action);
        scriptEntry.addObject("type", type);
        scriptEntry.addObject("player", player);
    }

    @Override
    public void execute(ScriptEntry scriptEntry) throws CommandExecutionException {

        String name = (String) scriptEntry.getObject("name");
        String value = (String) scriptEntry.getObject("value");
        Duration duration = (Duration) scriptEntry.getObject("duration");
        Action action = (Action) scriptEntry.getObject("action");
        Type type = (Type) scriptEntry.getObject("type");
        int index = -1;

        // Set working index, if specified.
        // Usage example: - FLAG FLAGNAME[3]:VALUE specifies an index of 3 should be set with VALUE.
        if (name.contains("[")) {
            try {
                index = Integer.valueOf(name.split("\\[")[1].replace("]", ""));
            } catch (Exception e) { index = -1; }
            name = name.split("\\[")[0];
        }

        Flag flag = null;
        String player = (String) scriptEntry.getObject("player");

        // Send information to debugger
        dB.report(getName(),
                aH.debugObj("Name", name)
                        + (index > 0 ? aH.debugObj("Index", String.valueOf(index)) : "")
                        + aH.debugObj("Type", type.toString())
                        + aH.debugUniqueObj("Action/Value", action.toString(), (value != null ? value : "null"))
                        + (duration.getSeconds() > 0 ? duration.debug() : "")
                        + (type == Type.NPC ? aH.debugObj("NPC", scriptEntry.getNPC().toString()) : "")
                        + (type == Type.PLAYER ? aH.debugObj("Player", player) : ""));

        // Returns existing flag (if existing), or a new flag if not
        switch (type) {
            case NPC:
                flag = denizen.flagManager().getNPCFlag(scriptEntry.getNPC().getId(), name);
                break;
            case PLAYER:
                flag = denizen.flagManager().getPlayerFlag(player, name);
                break;
            case GLOBAL:
                flag = denizen.flagManager().getGlobalFlag(name);
                break;
        }

        // Do flagAction
        switch (action) {
            case INCREASE: case DECREASE: case MULTIPLY: case DIVIDE:
                double currentValue = flag.get(index).asDouble();
                flag.set(Double.toString(math(currentValue, Double.valueOf(value), action)), index);
                break;
            case SET_BOOLEAN:
                flag.set("true", index);
                break;
            case SET_VALUE:
                flag.set(value, index);
                break;
            case INSERT:
                flag.add(value);
                break;
            case REMOVE:
                flag.remove(value, index);
                break;
            case SPLIT:
                flag.split(value);
                break;
            case DELETE:
                flag.clear();
        }

        // Set flag duration
        if (duration.getSeconds() > 0)
            flag.setExpiration(System.currentTimeMillis()
                    + Double.valueOf(duration.getSeconds() * 1000).longValue());
    }

    private double math(double currentValue, double value, Action flagAction) {
        switch (flagAction) {
            case INCREASE:
                return currentValue + value;
            case DECREASE:
                return currentValue - value;
            case MULTIPLY:
                return currentValue * value;
            case DIVIDE:
                return currentValue / value;
            default:
                break;
        }

        return 0;
    }

}
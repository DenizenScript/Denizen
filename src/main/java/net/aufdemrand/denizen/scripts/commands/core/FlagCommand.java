package net.aufdemrand.denizen.scripts.commands.core;

import net.aufdemrand.denizen.exceptions.CommandExecutionException;
import net.aufdemrand.denizen.exceptions.InvalidArgumentsException;
import net.aufdemrand.denizen.flags.FlagManager.Flag;
import net.aufdemrand.denizen.objects.*;
import net.aufdemrand.denizen.scripts.ScriptEntry;
import net.aufdemrand.denizen.scripts.commands.AbstractCommand;
import net.aufdemrand.denizen.utilities.debugging.dB;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

/**
 * Sets a Player 'Flag'. Flags can hold information to check against
 * with the FLAGGED requirement.
 *
 * @author Jeremy Schroeder
 */

public class FlagCommand extends AbstractCommand implements Listener {

    public enum Action { SET_VALUE, SET_BOOLEAN, INCREASE, DECREASE, MULTIPLY, DIVIDE, INSERT, REMOVE, SPLIT, DELETE }
    public enum Type   { GLOBAL, SERVER, NPC, DENIZEN, PLAYER }

    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {


        for (aH.Argument arg : aH.interpret(scriptEntry.getArguments())) {

            if (!scriptEntry.hasObject("duration")
                    && arg.matchesPrefix("duration"))
                scriptEntry.addObject("duration", arg.asType(Duration.class));

            else if (!scriptEntry.hasObject("target")
                    && arg.matchesEnum(Type.values()))
                scriptEntry.addObject("target", Type.valueOf(arg.getValue()));

                // Determine flagAction and set the flagName/flagValue
            else if (arg.raw_value.split(":", 3).length > 1) {
                String[] flagArgs = arg.raw_value.split(":", 3);
                scriptEntry.addObject("name", Element.valueOf(flagArgs[0].toUpperCase()));

                if (flagArgs.length == 2) {
                    if (flagArgs[1].equals("+")
                            || flagArgs[1].equals("++")) {
                        scriptEntry.addObject("action", Action.INCREASE);
                        scriptEntry.addObject("value", Element.valueOf("1"));
                    }   else if (flagArgs[1].equals("-")
                            || flagArgs[1].equals("--")) {
                        // Using equals instead of startsWith because
                        // people need to be able to set values like "-2"
                        scriptEntry.addObject("action", Action.DECREASE);
                        scriptEntry.addObject("value", Element.valueOf("1"));
                    }   else if (flagArgs[1].startsWith("!")) {
                        scriptEntry.addObject("action", Action.DELETE);
                    }   else if (flagArgs[1].startsWith("<-")) {
                        scriptEntry.addObject("action", Action.REMOVE);
                    }   else {
                        scriptEntry.addObject("action", Action.SET_VALUE);
                        scriptEntry.addObject("value", Element.valueOf(flagArgs[1]));
                    }
                } else if (flagArgs.length == 3) {
                    if (flagArgs[1].startsWith("->"))
                        scriptEntry.addObject("action", Action.INSERT);
                    else if (flagArgs[1].startsWith("<-"))
                        scriptEntry.addObject("action", Action.REMOVE);
                    else if (flagArgs[1].startsWith("|"))
                        scriptEntry.addObject("action", Action.SPLIT);
                    else if (flagArgs[1].startsWith("+"))
                        scriptEntry.addObject("action", Action.INCREASE);
                    else if (flagArgs[1].startsWith("-"))
                        scriptEntry.addObject("action", Action.DECREASE);
                    else if (flagArgs[1].startsWith("*"))
                        scriptEntry.addObject("action", Action.MULTIPLY);
                    else if (flagArgs[1].startsWith("/"))
                        scriptEntry.addObject("action", Action.DIVIDE);
                    if (!scriptEntry.hasObject("action"))
                        scriptEntry.addObject("action", Action.SET_VALUE);
                    scriptEntry.addObject("value", Element.valueOf(flagArgs[2]));
                }
            } else {
                if (!scriptEntry.hasObject("name"))
                    scriptEntry.addObject("name", arg.asElement());
            }
        }


        // Check required arguments

        if (!scriptEntry.hasObject("name"))
            throw new InvalidArgumentsException("Must specify a FLAG name.");

        scriptEntry.defaultObject("action", Action.SET_BOOLEAN);
        scriptEntry.defaultObject("value", Element.TRUE);
        scriptEntry.defaultObject("duration", new Duration(-1));
        scriptEntry.defaultObject("target", scriptEntry.hasPlayer() ? Type.PLAYER : null);
        
        // Sets the official target
        if (((Type) scriptEntry.getObject("target")).name().matches("^(DENIZEN|NPC)$")) {
            if (!scriptEntry.hasNPC() || !scriptEntry.getNPC().isValid())
                throw new InvalidArgumentsException("Invalid NPC specified!");
            scriptEntry.addObject("target", scriptEntry.getNPC().getDenizenEntity());
        }
        else if (((Type) scriptEntry.getObject("target")).name().matches("^PLAYER$")) {
            if (!scriptEntry.hasPlayer() || !scriptEntry.getPlayer().isValid())
                throw new InvalidArgumentsException("Invalid player specified!");
            scriptEntry.addObject("target", scriptEntry.getPlayer().getDenizenEntity());
        }
        else
            scriptEntry.addObject("target", "SERVER");
    }

    @Override
    public void execute(ScriptEntry scriptEntry) throws CommandExecutionException {

        String name = scriptEntry.getElement("name").asString();
        String value = scriptEntry.getElement("value").asString();
        Action action = (Action) scriptEntry.getObject("action");
        Duration duration = (Duration) scriptEntry.getObject("duration");
        dEntity target = null;
        if (scriptEntry.getObject("target") instanceof dEntity)
            target = (dEntity) scriptEntry.getObject("target");
        
        int index = -1;

        // Set working index, if specified.
        // Usage example: - FLAG FLAGNAME[3]:VALUE specifies an index of 3 should be set with VALUE.
        if (name.contains("[")) {
            try {
                index = Integer.valueOf(name.split("\\[")[1].replace("]", ""));
            } catch (Exception e) { index = -1; }
            name = name.split("\\[")[0];
        }

        // Send information to debugger
        dB.report(getName(),
                aH.debugObj("Name", name)
                        + (index > 0 ? aH.debugObj("Index", String.valueOf(index)) : "")
                        + aH.debugUniqueObj("Action/Value", action.toString(), (value != null ? value : "null"))
                        + (duration.getSeconds() > 0 ? duration.debug() : "")
                        + aH.debugObj("Target", (target != null ? (target.isNPC() ? target.getNPC() : target.isPlayer() ? target.getPlayer() : target.getType()) : "Server")));

        Flag flag = null;
        
        // Returns existing flag (if existing), or a new flag if not
        if (target == null)
            flag = denizen.flagManager().getGlobalFlag(name);
        else if (target.isPlayer())
            flag = denizen.flagManager().getPlayerFlag(target.getPlayer().getName(), name);
        else if (target.isNPC())
            flag = denizen.flagManager().getNPCFlag(target.getNPC().getId(), name);

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
        else
            flag.setExpiration(0L);
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
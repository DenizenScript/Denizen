package net.aufdemrand.denizen.scripts.commands.core;

import net.aufdemrand.denizen.exceptions.CommandExecutionException;
import net.aufdemrand.denizen.exceptions.InvalidArgumentsException;
import net.aufdemrand.denizen.flags.FlagManager.Flag;
import net.aufdemrand.denizen.objects.Element;
import net.aufdemrand.denizen.objects.dEntity;
import net.aufdemrand.denizen.scripts.ScriptEntry;
import net.aufdemrand.denizen.scripts.commands.AbstractCommand;
import net.aufdemrand.denizen.objects.Duration;
import net.aufdemrand.denizen.objects.aH;
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
    public enum Type   { GLOBAL, NPC, PLAYER }

    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {


        for (aH.Argument arg : aH.interpret(scriptEntry.getArguments())) {

            if (arg.matchesPrefix("duration")
                    && !scriptEntry.hasObject("duration"))
                scriptEntry.addObject("duration", arg.asElement());

            else if (arg.matches("NPC, DENIZEN")
                    && !scriptEntry.hasObject("entity")) {
                if (!scriptEntry.hasNPC())
                    throw new InvalidArgumentsException("Specified NPC-type flag with no valid NPC reference.");
                scriptEntry.addObject("entity", Element.valueOf(scriptEntry.getNPC().identify()));
            }
            else if (arg.matches("PLAYER")
                    && !scriptEntry.hasObject("entity")) {
                if (!scriptEntry.hasPlayer())
                    throw new InvalidArgumentsException("Specified Player-type flag with no valid Player reference.");
                scriptEntry.addObject("entity", Element.valueOf(scriptEntry.getPlayer().identify()));
            }
            else if (arg.matches("GLOBAL, SERVER")
                    && !scriptEntry.hasObject("entity")) {
                scriptEntry.addObject("entity", Element.valueOf("null"));
            }

            else if ((arg.matchesPrefix("entity")|| arg.matchesPrefix("player"))
                    && !scriptEntry.hasObject("entity"))
                scriptEntry.addObject("entity", arg.asElement());

                // Determine flagAction and set the flagName/flagValue
            else if (arg.getPrefixAndValue().split(":", 3).length > 1) {
                String[] flagArgs = arg.getPrefixAndValue().split(":", 3);
                scriptEntry.addObject("name", Element.valueOf(flagArgs[0].toUpperCase()));

                if (flagArgs.length == 2) {
                    if (flagArgs[1].equals("+")
                            || flagArgs[1].equals("++")) {
                        scriptEntry.addObject("action", Element.valueOf("INCREASE"));
                        scriptEntry.addObject("value", Element.valueOf("1"));
                    }   else if (flagArgs[1].equals("-")
                            || flagArgs[1].equals("--")) {
                        // Using equals instead of startsWith because
                        // people need to be able to set values like "-2"
                        scriptEntry.addObject("action", Element.valueOf("DECREASE"));
                        scriptEntry.addObject("value", Element.valueOf("1"));
                    }   else if (flagArgs[1].startsWith("!")) {
                        scriptEntry.addObject("action", Element.valueOf("DELETE"));
                    }   else if (flagArgs[1].startsWith("<-")) {
                        scriptEntry.addObject("action", Element.valueOf("REMOVE"));
                    }   else {
                        scriptEntry.addObject("action", Element.valueOf("SET_VALUE"));
                        scriptEntry.addObject("value", Element.valueOf(arg.getPrefixAndValue().split(":")[1]));
                    }
                } else if (flagArgs.length == 3) {
                    if (flagArgs[1].startsWith("->"))
                        scriptEntry.addObject("action", Element.valueOf("INSERT"));
                    else if (flagArgs[1].startsWith("<-"))
                        scriptEntry.addObject("action", Element.valueOf("REMOVE"));
                    else if (flagArgs[1].startsWith("|"))
                        scriptEntry.addObject("action", Element.valueOf("SPLIT"));
                    else if (flagArgs[1].startsWith("+"))
                        scriptEntry.addObject("action", Element.valueOf("INCREASE"));
                    else if (flagArgs[1].startsWith("-"))
                        scriptEntry.addObject("action", Element.valueOf("DECREASE"));
                    else if (flagArgs[1].startsWith("*"))
                        scriptEntry.addObject("action", Element.valueOf("MULTIPLY"));
                    else if (flagArgs[1].startsWith("/"))
                        scriptEntry.addObject("action", Element.valueOf("DIVIDE"));
                    if (!scriptEntry.hasObject("action"))
                        scriptEntry.addObject("action", Element.valueOf("SET_VALUE"));
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

        if (!scriptEntry.hasObject("value"))
            scriptEntry.addObject("value", new Element("true"));

        if (!scriptEntry.hasObject("entity")) {
            if (!scriptEntry.hasPlayer())
                throw new InvalidArgumentsException("Specified NPC-type flag with no valid NPC reference.");
            scriptEntry.addObject("entity", Element.valueOf(scriptEntry.getPlayer().identify()));
        }

        if (!scriptEntry.hasObject("action"))
            scriptEntry.addObject("action", Element.valueOf("SET_BOOLEAN"));
    }

    @Override
    public void execute(ScriptEntry scriptEntry) throws CommandExecutionException {

        String name = scriptEntry.getElement("name").asString();
        String value = scriptEntry.getElement("value").asString();
        Duration duration;
        if (scriptEntry.hasObject("duration"))
            duration = Duration.valueOf(scriptEntry.getElement("duration").asString());
        else
            duration = new Duration(-1d);
        Action action = Action.valueOf(scriptEntry.getElement("action").asString());
        dEntity entity = dEntity.valueOf(scriptEntry.getElement("entity").asString());
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
                        + aH.debugUniqueObj("Action/Value", action.toString(), (value != null ? value : "null"))
                        + (duration.getSeconds() > 0 ? duration.debug() : "")
                        + (entity == null?"entity='server'":entity.debug()));

        // Returns existing flag (if existing), or a new flag if not
        if (entity == null)
            flag = denizen.flagManager().getGlobalFlag(name);
        else if (entity.isNPC())
            flag = denizen.flagManager().getNPCFlag(scriptEntry.getNPC().getId(), name);
        else if (entity.isLivingEntity() && entity.getLivingEntity() instanceof Player)
            flag = denizen.flagManager().getPlayerFlag(((Player)entity.getLivingEntity()).getName(), name);
        else {
            dB.echoError("Invalid entity specified!");
            return;
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
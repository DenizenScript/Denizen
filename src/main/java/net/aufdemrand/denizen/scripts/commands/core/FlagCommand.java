package net.aufdemrand.denizen.scripts.commands.core;

import net.aufdemrand.denizen.exceptions.CommandExecutionException;
import net.aufdemrand.denizen.exceptions.InvalidArgumentsException;
import net.aufdemrand.denizen.flags.FlagManager;
import net.aufdemrand.denizen.flags.FlagManager.Flag;
import net.aufdemrand.denizen.objects.*;
import net.aufdemrand.denizen.scripts.ScriptEntry;
import net.aufdemrand.denizen.scripts.commands.AbstractCommand;
import net.aufdemrand.denizen.utilities.debugging.dB;
import org.bukkit.event.Listener;

/**
 * Sets a server or player/npc 'flag'. Flags can hold information to check against
 * with the FLAGGED requirement, or store important information.
 *
 * @author Jeremy Schroeder
 * @version 1.0
 *
 */

public class FlagCommand extends AbstractCommand implements Listener {

    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {

        boolean specified_target = false;

        for (aH.Argument arg : aH.interpret(scriptEntry.getArguments())) {

            dB.log(arg.raw_value);

            // A duration on a flag will set it to expire after the
            // specified amount of time
            if (!scriptEntry.hasObject("duration")
                    && arg.matchesPrefix("duration, d")
                    && arg.matchesArgumentType(Duration.class)) {
                scriptEntry.addObject("duration", arg.asType(Duration.class));
            }

            // Allow a p@player or n@npc entity to specify the target
            // to be flagged
            else if (!scriptEntry.hasObject("flag_target")
                    && arg.matchesArgumentType(dNPC.class)) {
                specified_target = true;
                scriptEntry.addObject("flag_target", arg.asType(dNPC.class));

            } else if (!scriptEntry.hasObject("flag_target")
                    && arg.matchesArgumentType(dPlayer.class)) {
                specified_target = true;
                scriptEntry.addObject("flag_target", arg.asType(dPlayer.class));
            }

            // Also allow attached dObjects to be specified...
            else if (!scriptEntry.hasObject("flag_target")
                    && arg.matches("npc, denizen")) {
                specified_target = true;
                scriptEntry.addObject("flag_target", scriptEntry.getNPC());

            } else if (!scriptEntry.hasObject("flag_target")
                    && arg.matches("global, server")) {
                specified_target = true;
                scriptEntry.addObject("flag_target", new Element("server"));

            } else if (!scriptEntry.hasObject("flag_target")
                    && arg.matches("player")) {
                specified_target = true;
                scriptEntry.addObject("flag_target", scriptEntry.getPlayer());
            }

            // Check if setting a boolean
            else if (!scriptEntry.hasObject("action")
                    && arg.raw_value.split(":", 3).length == 1) {
                dB.echoDebug("boolean -> " + arg.raw_value);
                scriptEntry.addObject("action", FlagManager.Action.SET_BOOLEAN);
                scriptEntry.addObject("flag_name", arg.asElement());
            }

            // Check for flag_name:value/action
            else if (!scriptEntry.hasObject("action")
                    && arg.raw_value.split(":", 3).length == 2) {
                String[] flagArgs = arg.raw_value.split(":", 2);
                scriptEntry.addObject("flag_name", new Element(flagArgs[0].toUpperCase()));

                if (flagArgs[1].equals("++") || flagArgs[1].equals("+"))
                    scriptEntry.addObject("action", FlagManager.Action.INCREASE);

                else if (flagArgs[1].equals("--") || flagArgs[1].equals("-"))
                    scriptEntry.addObject("action", FlagManager.Action.DECREASE);

                else if (flagArgs[1].equals("!"))
                    scriptEntry.addObject("action", FlagManager.Action.DELETE);

                else if (flagArgs[1].equals("<-"))
                    scriptEntry.addObject("action", FlagManager.Action.REMOVE);

                else {
                    // No ACTION, we're just setting a value...
                    scriptEntry.addObject("action", FlagManager.Action.SET_VALUE);
                    scriptEntry.addObject("value", new Element(flagArgs[1]));
                }
            }

            // Check for flag_name:action:value
            else if (!scriptEntry.hasObject("action")
                    && arg.raw_value.split(":", 3).length == 3) {
                String[] flagArgs = arg.raw_value.split(":", 3);
                scriptEntry.addObject("flag_name", new Element(flagArgs[0].toUpperCase()));
                scriptEntry.addObject("value", new Element(flagArgs[2]));

                if (flagArgs[1].startsWith("->"))
                    scriptEntry.addObject("action", FlagManager.Action.INSERT);

                else if (flagArgs[1].startsWith("<-"))
                    scriptEntry.addObject("action", FlagManager.Action.REMOVE);

                else if (flagArgs[1].startsWith("|"))
                    scriptEntry.addObject("action", FlagManager.Action.SPLIT);

                else if (flagArgs[1].startsWith("+"))
                    scriptEntry.addObject("action", FlagManager.Action.INCREASE);

                else if (flagArgs[1].startsWith("-"))
                    scriptEntry.addObject("action", FlagManager.Action.DECREASE);

                else if (flagArgs[1].startsWith("*"))
                    scriptEntry.addObject("action", FlagManager.Action.MULTIPLY);

                else if (flagArgs[1].startsWith("/"))
                    scriptEntry.addObject("action", FlagManager.Action.DIVIDE);
            }

            else dB.echoDebug("Unhandled argument: " + arg.raw_value);
        }

        // Set defaults
        if (!specified_target)
            scriptEntry.defaultObject("flag_target", scriptEntry.getPlayer());

        // Check required arguments
        if (!scriptEntry.hasObject("action"))
            throw new InvalidArgumentsException("Must specify a flag action or value.");

        if (!scriptEntry.hasObject("flag_target"))
            throw new InvalidArgumentsException("Must specify a flag target!");
    }


    @Override
    public void execute(ScriptEntry scriptEntry) throws CommandExecutionException {

        dObject flag_target = scriptEntry.getdObject("flag_target");
        Duration duration = (Duration) scriptEntry.getObject("duration");
        FlagManager.Action action = (FlagManager.Action) scriptEntry.getObject("action");
        Element value = scriptEntry.getElement("value");
        Element name = scriptEntry.getElement("flag_name");

        int index = -1;

        // Set working index, if specified.
        // Usage example: - FLAG FLAGNAME[3]:VALUE specifies an index of 3 should be set with VALUE.
        if (name.asString().contains("[")) {
            try {
                index = Integer.valueOf(name.asString().split("\\[")[1].replace("]", ""));
            } catch (Exception e) { index = -1; }
            name = Element.valueOf(name.asString().split("\\[")[0]);
        }

        // Send information to debugger
        dB.report(getName(),
                name.debug() + (index > 0 ? aH.debugObj("Index", String.valueOf(index)) : "")
                        + aH.debugUniqueObj("Action/Value", action.toString(), (value != null ? value.asString() : "null"))
                        + (duration != null ? duration.debug() : "")
                        + flag_target.debug());

        Flag flag;

        // Returns existing flag (if existing), or a new flag if not
        if (flag_target instanceof Element)
            flag = denizen.flagManager().getGlobalFlag(name.asString());

        else if (flag_target instanceof dPlayer)
            flag = denizen.flagManager().getPlayerFlag(((dPlayer) flag_target).getName(), name.asString());

        else if (flag_target instanceof dNPC)
            flag = denizen.flagManager().getNPCFlag(((dNPC) flag_target).getId(), name.asString());

        else throw new CommandExecutionException("Could not fetch a flag for this entity: " + flag_target.debug());

        // Do the action!
        flag.doAction(action, value, index);

        // Set flag duration
        if (duration != null && duration.getSeconds() > 0)
            flag.setExpiration(System.currentTimeMillis()
                    + Double.valueOf(duration.getSeconds() * 1000).longValue());

        else flag.setExpiration(0L);
    }



}
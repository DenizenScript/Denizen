package net.aufdemrand.denizen.scripts.commands.core;

import net.aufdemrand.denizen.BukkitScriptEntryData;
import net.aufdemrand.denizen.flags.FlagManager;
import net.aufdemrand.denizen.flags.FlagManager.Flag;
import net.aufdemrand.denizen.objects.dEntity;
import net.aufdemrand.denizen.objects.dNPC;
import net.aufdemrand.denizen.objects.dPlayer;
import net.aufdemrand.denizen.utilities.DenizenAPI;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.aufdemrand.denizencore.DenizenCore;
import net.aufdemrand.denizencore.exceptions.InvalidArgumentsException;
import net.aufdemrand.denizencore.objects.Duration;
import net.aufdemrand.denizencore.objects.Element;
import net.aufdemrand.denizencore.objects.aH;
import net.aufdemrand.denizencore.objects.dObject;
import net.aufdemrand.denizencore.scripts.ScriptEntry;
import net.aufdemrand.denizencore.scripts.commands.AbstractCommand;
import org.bukkit.event.Listener;

/**
 * Sets a server or player/npc 'flag'. Flags can hold information to check against
 * with the FLAGGED requirement, or store important information.
 *
 * @author Jeremy Schroeder
 * @version 1.0
 */

public class FlagCommand extends AbstractCommand implements Listener {

    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {

        boolean specified_target = false;

        for (aH.Argument arg : aH.interpret(scriptEntry.getArguments())) {

            // A duration on a flag will set it to expire after the
            // specified amount of time
            if (!scriptEntry.hasObject("duration")
                    && arg.matchesPrefix("duration", "d")
                    && arg.matchesArgumentType(Duration.class)) {
                scriptEntry.addObject("duration", arg.asType(Duration.class));
            }

            // Also allow attached dObjects to be specified...
            else if (!scriptEntry.hasObject("flag_target")
                    && arg.matches("npc", "denizen")) {
                specified_target = true;
                scriptEntry.addObject("flag_target", ((BukkitScriptEntryData) scriptEntry.entryData).getNPC());

            }
            else if (!scriptEntry.hasObject("flag_target")
                    && arg.matches("global", "server")) {
                specified_target = true;
                scriptEntry.addObject("flag_target", Element.SERVER);

            }
            else if (!scriptEntry.hasObject("flag_target")
                    && arg.matches("player")) {
                specified_target = true;
                scriptEntry.addObject("flag_target", ((BukkitScriptEntryData) scriptEntry.entryData).getPlayer());
            }

            // Allow a p@player or n@npc entity to specify the target to be flagged.
            // Don't check if the player/npc is valid until after the argument
            // is being processed to make sure the objects don't accidentally get set
            // as the name of the flag..
            else if (!scriptEntry.hasObject("flag_target")
                    && arg.startsWith("n@") && !arg.hasPrefix()) {
                if (dNPC.valueOf(arg.getValue()) == null) // TODO: Optimize
                {
                    throw new InvalidArgumentsException("Invalid NPC target.");
                }
                specified_target = true;
                scriptEntry.addObject("flag_target", arg.asType(dNPC.class));

            }
            else if (!scriptEntry.hasObject("flag_target")
                    && arg.startsWith("p@") && !arg.hasPrefix()) {
                if (dPlayer.valueOf(arg.getValue()) == null) // TODO: Optimize
                {
                    throw new InvalidArgumentsException("Invalid Player target.");
                }
                specified_target = true;
                scriptEntry.addObject("flag_target", arg.asType(dPlayer.class));
            }
            else if (!scriptEntry.hasObject("flag_target")
                    && !arg.hasPrefix()) {
                if (dEntity.valueOf(arg.getValue()) == null) // TODO: Optimize
                {
                    throw new InvalidArgumentsException("Invalid Entity target.");
                }
                specified_target = true;
                scriptEntry.addObject("flag_target", arg.asType(dEntity.class));
            }


            // Check if setting a boolean
            else if (!scriptEntry.hasObject("flag_name") &&
                    arg.raw_value.split(":", 3).length == 1) {
                scriptEntry.addObject("action", FlagManager.Action.SET_BOOLEAN);
                scriptEntry.addObject("value", Element.TRUE);
                scriptEntry.addObject("flag_name", arg.asElement());
            }

            // Check for flag_name:value/action
            else if (!scriptEntry.hasObject("flag_name") &&
                    arg.raw_value.split(":", 3).length == 2) {

                String[] flagArgs = arg.raw_value.split(":", 2);
                scriptEntry.addObject("flag_name", new Element(flagArgs[0].toUpperCase()));

                if (flagArgs[1].equals("++") || flagArgs[1].equals("+")) {
                    scriptEntry.addObject("action", FlagManager.Action.INCREASE);
                    scriptEntry.addObject("value", new Element(1));
                }
                else if (flagArgs[1].equals("--") || flagArgs[1].equals("-")) {
                    scriptEntry.addObject("action", FlagManager.Action.DECREASE);
                    scriptEntry.addObject("value", new Element(1));
                }
                else if (flagArgs[1].equals("!")) {
                    scriptEntry.addObject("action", FlagManager.Action.DELETE);
                    scriptEntry.addObject("value", Element.FALSE);
                }
                else if (flagArgs[1].equals("<-")) {
                    scriptEntry.addObject("action", FlagManager.Action.REMOVE);
                    scriptEntry.addObject("value", Element.FALSE);
                }
                else {
                    // No ACTION, we're just setting a value...
                    scriptEntry.addObject("action", FlagManager.Action.SET_VALUE);
                    scriptEntry.addObject("value", new Element(flagArgs[1]));
                }
            }

            // Check for flag_name:action:value
            else if (!scriptEntry.hasObject("flag_name") &&
                    arg.raw_value.split(":", 3).length == 3) {
                String[] flagArgs = arg.raw_value.split(":", 3);
                scriptEntry.addObject("flag_name", new Element(flagArgs[0].toUpperCase()));

                if (flagArgs[1].equals("->")) {
                    scriptEntry.addObject("action", FlagManager.Action.INSERT);
                }
                else if (flagArgs[1].equals("<-")) {
                    scriptEntry.addObject("action", FlagManager.Action.REMOVE);
                }
                else if (flagArgs[1].equals("||") || flagArgs[1].equals("|")) {
                    scriptEntry.addObject("action", FlagManager.Action.SPLIT);
                }
                else if (flagArgs[1].equals("!|")) {
                    scriptEntry.addObject("action", FlagManager.Action.SPLIT_NEW);
                }
                else if (flagArgs[1].equals("++") || flagArgs[1].equals("+")) {
                    scriptEntry.addObject("action", FlagManager.Action.INCREASE);
                }
                else if (flagArgs[1].equals("--") || flagArgs[1].equals("-")) {
                    scriptEntry.addObject("action", FlagManager.Action.DECREASE);
                }
                else if (flagArgs[1].equals("**") || flagArgs[1].equals("*")) {
                    scriptEntry.addObject("action", FlagManager.Action.MULTIPLY);
                }
                else if (flagArgs[1].equals("//") || flagArgs[1].equals("/")) {
                    scriptEntry.addObject("action", FlagManager.Action.DIVIDE);
                }
                else {
                    scriptEntry.addObject("action", FlagManager.Action.SET_VALUE);
                    scriptEntry.addObject("value", new Element(arg.raw_value.split(":", 2)[1]));
                    continue;
                }

                scriptEntry.addObject("value", new Element(flagArgs[2]));
            }
            else {
                arg.reportUnhandled();
            }
        }

        // Set defaults
        if (!specified_target) {
            scriptEntry.defaultObject("flag_target", ((BukkitScriptEntryData) scriptEntry.entryData).getPlayer());
        }

        // Check required arguments
        if (!scriptEntry.hasObject("action")) {
            throw new InvalidArgumentsException("Must specify a flag action or value.");
        }

        if (!scriptEntry.hasObject("flag_target")) {
            throw new InvalidArgumentsException("Must specify a flag target!");
        }
    }


    @Override
    public void execute(ScriptEntry scriptEntry) {

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
            }
            catch (Exception e) {
                index = -1;
            }
            name = Element.valueOf(name.asString().split("\\[")[0]);
        }

        // Send information to debugger
        if (scriptEntry.dbCallShouldDebug()) {
            dB.report(scriptEntry, getName(),
                    name.debug() + (index > 0 ? aH.debugObj("Index", String.valueOf(index)) : "")
                            + aH.debugUniqueObj("Action/Value", action.toString(), (value != null ? value.asString() : "null"))
                            + (duration != null ? duration.debug() : "")
                            + flag_target.debug());
        }

        Flag flag;

        // Returns existing flag (if existing), or a new flag if not
        if (flag_target instanceof Element) {
            flag = DenizenAPI.getCurrentInstance().flagManager().getGlobalFlag(name.asString());
        }
        else if (flag_target instanceof dPlayer) {
            flag = DenizenAPI.getCurrentInstance().flagManager().getPlayerFlag((dPlayer) flag_target, name.asString());
        }
        else if (flag_target instanceof dNPC) {
            flag = DenizenAPI.getCurrentInstance().flagManager().getNPCFlag(((dNPC) flag_target).getId(), name.asString());
        }
        else if (flag_target instanceof dEntity) {
            flag = DenizenAPI.getCurrentInstance().flagManager().getEntityFlag((dEntity) flag_target, name.asString());
        }
        else {
            dB.echoError("Could not fetch a flag for this entity: " + flag_target.debug());
            return;
        }

        // Do the action!
        flag.doAction(action, value, index);

        // Set flag duration
        if (flag.StillValid() && duration != null && duration.getSeconds() > 0) {
            flag.setExpiration(DenizenCore.currentTimeMillis
                    + Double.valueOf(duration.getSeconds() * 1000.0).longValue());
        }
        else if (flag.StillValid() && flag.expiration().getMillis() != 0L) {
            flag.setExpiration(0L);
        }
    }
}

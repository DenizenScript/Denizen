package com.denizenscript.denizen.scripts.commands.core;

import com.denizenscript.denizen.utilities.DenizenAPI;
import com.denizenscript.denizen.utilities.Utilities;
import com.denizenscript.denizen.utilities.debugging.Debug;
import com.denizenscript.denizen.flags.FlagManager;
import com.denizenscript.denizen.flags.FlagManager.Flag;
import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizen.objects.NPCTag;
import com.denizenscript.denizen.objects.PlayerTag;
import com.denizenscript.denizencore.DenizenCore;
import com.denizenscript.denizencore.exceptions.InvalidArgumentsException;
import com.denizenscript.denizencore.objects.*;
import com.denizenscript.denizencore.objects.core.DurationTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.scripts.ScriptEntry;
import com.denizenscript.denizencore.scripts.commands.AbstractCommand;
import org.bukkit.event.Listener;

public class FlagCommand extends AbstractCommand implements Listener {

    // <--[command]
    // @Name Flag
    // @Syntax flag ({player}/npc/server/<entity>) [<name>([<#>])](:<action>)[:<value>] (duration:<value>)
    // @Required 1
    // @Short Sets or modifies a flag on the player, NPC, entity, or server.
    // @Group core
    //
    // @Description
    // The flag command sets or modifies custom value storage database entries connected to
    // each player, each NPC, each entity, and the server.
    // Flags can have operations performed upon them, such as:
    // Increment a flag:
    // - flag player counter:++
    // Increase a flag by 3:
    // - flag player counter:+:3
    // Decrease a flag by 2:
    // - flag player counter:-:2
    //
    // See <@link language flags> for more info.
    //
    // All the flag values are stored by default in "plugins/denizen/saves.yml" file.
    // For an alternative way of storing values, use either yaml (See <@link command yaml>)
    // or sql (See <@link command sql>)
    //
    //
    // @Tags
    // <PlayerTag.flag[<flag>]>
    // <PlayerTag.has_flag[<flag_name>]>
    // <PlayerTag.list_flags[(regex:)<search>]>
    // <NPCTag.flag[<flag>]>
    // <NPCTag.has_flag[<flag_name>]>
    // <NPCTag.list_flags[(regex:)<search>]>
    // <EntityTag.flag[<flag_name>]>
    // <EntityTag.has_flag[<flag_name>]>
    // <EntityTag.list_flags[(regex:)<search>]>
    // <server.flag[<flag>]>
    // <server.has_flag[<flag_name>]>
    // <server.list_flags[(regex:)<search>]>
    // <server.list_online_players_flagged[<flag_name>]>
    // <server.list_players_flagged[<flag_name>]>
    // <server.list_spawned_npcs_flagged[<flag_name>]>
    // <server.list_npcs_flagged[<flag_name>]>
    // <fl@flag_name.is_expired>
    // <fl@flag_name.expiration>
    // <fl@flag_name.as_list>
    //
    // @Usage
    // Use to create or set a flag on a player.
    // - flag player playstyle:agressive
    //
    // @Usage
    // Use to flag an npc with a given tag value.
    // - flag npc location:<npc.location>
    //
    // @Usage
    // Use to apply mathematical changes to a flag's value on a unique object.
    // - flag <context.damager> damage_dealt:+:<context.damage>
    //
    // @Usage
    // Use to add an item to a server flag as a new value without removing existing values.
    // - flag server cool_people:->:p@TheBlackCoyote
    //
    // @Usage
    // Use to add both multiple items as individual new values to a server flag.
    // - flag server cool_people:|:p@mcmonkey4eva|p@morphan1
    //
    // @Usage
    // Use to remove an entry from a server flag.
    // - flag server cool_people:<-:p@morphan1
    //
    // @Usage
    // Use to clear a flag and fill it with a new list of values.
    // - flag server cool_people:!|:p@mcmonkey4eva|p@morphan1|p@xenmai
    //
    // @Usage
    // Use to completely remove a flag.
    // - flag server cool_people:!
    //
    // @Usage
    // Use to modify a specific index in a list flag.
    // - flag server myflag[3]:HelloWorld
    // -->

    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {

        boolean specified_target = false;

        for (Argument arg : scriptEntry.getProcessedArgs()) {

            // A duration on a flag will set it to expire after the
            // specified amount of time
            if (!scriptEntry.hasObject("duration")
                    && arg.matchesPrefix("duration", "d")
                    && arg.matchesArgumentType(DurationTag.class)) {
                scriptEntry.addObject("duration", arg.asType(DurationTag.class));
            }

            // Also allow attached ObjectTags to be specified...
            else if (!scriptEntry.hasObject("flag_target")
                    && arg.matches("npc", "denizen")) {
                specified_target = true;
                scriptEntry.addObject("flag_target", Utilities.getEntryNPC(scriptEntry));

            }
            else if (!scriptEntry.hasObject("flag_target")
                    && arg.matches("global", "server")) {
                specified_target = true;
                scriptEntry.addObject("flag_target", new ElementTag("server"));

            }
            else if (!scriptEntry.hasObject("flag_target")
                    && arg.matches("player")) {
                specified_target = true;
                scriptEntry.addObject("flag_target", Utilities.getEntryPlayer(scriptEntry));
            }

            // Allow a PlayerTag or NPCTag entity to specify the target to be flagged.
            // Don't check if the player/npc is valid until after the argument
            // is being processed to make sure the objects don't accidentally get set
            // as the name of the flag..
            else if (!scriptEntry.hasObject("flag_target")
                    && arg.startsWith("n@") && !arg.hasPrefix()) {
                if (NPCTag.valueOf(arg.getValue()) == null) // TODO: Optimize
                {
                    throw new InvalidArgumentsException("Invalid NPC target.");
                }
                specified_target = true;
                scriptEntry.addObject("flag_target", arg.asType(NPCTag.class));

            }
            else if (!scriptEntry.hasObject("flag_target")
                    && arg.startsWith("p@") && !arg.hasPrefix()) {
                if (PlayerTag.valueOf(arg.getValue()) == null) // TODO: Optimize
                {
                    throw new InvalidArgumentsException("Invalid Player target.");
                }
                specified_target = true;
                scriptEntry.addObject("flag_target", arg.asType(PlayerTag.class));
            }
            else if (!scriptEntry.hasObject("flag_target")
                    && !arg.hasPrefix()) {
                if (EntityTag.valueOf(arg.getValue()) == null) // TODO: Optimize
                {
                    throw new InvalidArgumentsException("Invalid Entity target.");
                }
                specified_target = true;
                scriptEntry.addObject("flag_target", arg.asType(EntityTag.class));
            }


            // Check if setting a boolean
            else if (!scriptEntry.hasObject("flag_name") &&
                    arg.raw_value.split(":", 3).length == 1) {
                scriptEntry.addObject("action", FlagManager.Action.SET_BOOLEAN);
                scriptEntry.addObject("value", new ElementTag(true));
                scriptEntry.addObject("flag_name", arg.asElement());
            }

            // Check for flag_name:value/action
            else if (!scriptEntry.hasObject("flag_name") &&
                    arg.raw_value.split(":", 3).length == 2) {

                String[] flagArgs = arg.raw_value.split(":", 2);
                scriptEntry.addObject("flag_name", new ElementTag(flagArgs[0].toUpperCase()));

                if (flagArgs[1].equals("++") || flagArgs[1].equals("+")) {
                    scriptEntry.addObject("action", FlagManager.Action.INCREASE);
                    scriptEntry.addObject("value", new ElementTag(1));
                }
                else if (flagArgs[1].equals("--") || flagArgs[1].equals("-")) {
                    scriptEntry.addObject("action", FlagManager.Action.DECREASE);
                    scriptEntry.addObject("value", new ElementTag(1));
                }
                else if (flagArgs[1].equals("!")) {
                    scriptEntry.addObject("action", FlagManager.Action.DELETE);
                    scriptEntry.addObject("value", new ElementTag(false));
                }
                else if (flagArgs[1].equals("<-")) {
                    scriptEntry.addObject("action", FlagManager.Action.REMOVE);
                    scriptEntry.addObject("value", new ElementTag(false));
                }
                else {
                    // No ACTION, we're just setting a value...
                    scriptEntry.addObject("action", FlagManager.Action.SET_VALUE);
                    scriptEntry.addObject("value", new ElementTag(flagArgs[1]));
                }
            }

            // Check for flag_name:action:value
            else if (!scriptEntry.hasObject("flag_name") &&
                    arg.raw_value.split(":", 3).length == 3) {
                String[] flagArgs = arg.raw_value.split(":", 3);
                scriptEntry.addObject("flag_name", new ElementTag(flagArgs[0].toUpperCase()));

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
                    scriptEntry.addObject("value", new ElementTag(arg.raw_value.split(":", 2)[1]));
                    continue;
                }

                scriptEntry.addObject("value", new ElementTag(flagArgs[2]));
            }
            else {
                arg.reportUnhandled();
            }
        }

        // Set defaults
        if (!specified_target) {
            scriptEntry.defaultObject("flag_target", Utilities.getEntryPlayer(scriptEntry));
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

        ObjectTag flag_target = scriptEntry.getObjectTag("flag_target");
        DurationTag duration = (DurationTag) scriptEntry.getObject("duration");
        FlagManager.Action action = (FlagManager.Action) scriptEntry.getObject("action");
        ElementTag value = scriptEntry.getElement("value");
        ElementTag name = scriptEntry.getElement("flag_name");

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
            name = ElementTag.valueOf(name.asString().split("\\[")[0]);
        }

        // Send information to debugger
        if (scriptEntry.dbCallShouldDebug()) {
            Debug.report(scriptEntry, getName(),
                    name.debug() + (index > 0 ? ArgumentHelper.debugObj("Index", String.valueOf(index)) : "")
                            + ArgumentHelper.debugUniqueObj("Action/Value", action.toString(), (value != null ? value.asString() : "null"))
                            + (duration != null ? duration.debug() : "")
                            + flag_target.debug());
        }

        Flag flag;

        // Returns existing flag (if existing), or a new flag if not
        if (flag_target instanceof ElementTag) {
            flag = DenizenAPI.getCurrentInstance().flagManager().getGlobalFlag(name.asString());
        }
        else if (flag_target instanceof PlayerTag) {
            flag = DenizenAPI.getCurrentInstance().flagManager().getPlayerFlag((PlayerTag) flag_target, name.asString());
        }
        else if (flag_target instanceof NPCTag) {
            flag = DenizenAPI.getCurrentInstance().flagManager().getNPCFlag(((NPCTag) flag_target).getId(), name.asString());
        }
        else if (flag_target instanceof EntityTag) {
            flag = DenizenAPI.getCurrentInstance().flagManager().getEntityFlag((EntityTag) flag_target, name.asString());
        }
        else {
            Debug.echoError("Could not fetch a flag for this entity: " + flag_target.debug());
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

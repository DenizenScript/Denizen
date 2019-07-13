package com.denizenscript.denizen.scripts.commands.player;

import com.denizenscript.denizen.utilities.Utilities;
import com.denizenscript.denizen.utilities.debugging.dB;
import com.denizenscript.denizen.objects.dLocation;
import com.denizenscript.denizencore.exceptions.InvalidArgumentsException;
import com.denizenscript.denizencore.objects.Element;
import com.denizenscript.denizencore.objects.aH;
import com.denizenscript.denizencore.scripts.ScriptEntry;
import com.denizenscript.denizencore.scripts.commands.AbstractCommand;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class CompassCommand extends AbstractCommand {

    // <--[command]
    // @Name Compass
    // @Syntax compass [<location>/reset]
    // @Required 1
    // @Short Redirects the player's compass to target the given location.
    // @Group player
    //
    // @Description
    // Redirects the compass of the player, who is attached to the script queue.
    //
    // This is not the compass item, but the command is controlling the pointer the item should direct at.
    // This means that all item compasses will point the same direction but differently for each player.
    //
    // The y-axis is not used but its fine to be included in the location argument.
    //
    // Reset argument will turn the direction to default (spawn or bed)
    //
    // @Tags
    // <p@player.compass_target>
    //
    // @Usage
    // Use to reset the compass direction to its default
    // - compass reset
    //
    // @Usage
    // Use to point with a compass to the player's current location
    // - compass <player.location>
    //
    // @Usage
    // Use to point with a compass to the world's spawn location
    // - compass <w@world.spawn_location>
    // -->

    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {

        // Interpret arguments

        for (aH.Argument arg : aH.interpretArguments(scriptEntry.aHArgs)) {

            if (!scriptEntry.hasObject("location")
                    && arg.matchesArgumentType(dLocation.class)) {
                scriptEntry.addObject("location", arg.asType(dLocation.class));
            }
            else if (!scriptEntry.hasObject("reset")
                    && arg.matches("reset")) {
                scriptEntry.addObject("reset", new Element(true));
            }
            else {
                arg.reportUnhandled();
            }
        }

        // Check for required information
        if (!scriptEntry.hasObject("location") && !scriptEntry.hasObject("reset")) {
            throw new InvalidArgumentsException("Missing location argument!");
        }

        scriptEntry.defaultObject("reset", new Element(false));
    }


    @Override
    public void execute(ScriptEntry scriptEntry) {

        // Fetch required objects

        dLocation location = scriptEntry.getdObject("location");
        Element reset = scriptEntry.getElement("reset");
        Player player = Utilities.getEntryPlayer(scriptEntry).getPlayerEntity();


        // Debug the execution

        if (scriptEntry.dbCallShouldDebug()) {

            dB.report(scriptEntry, getName(), (location != null ? location.debug() : "") + reset.debug());

        }

        if (reset.asBoolean()) {
            Location bed = player.getBedSpawnLocation();
            player.setCompassTarget(bed != null ? bed : player.getWorld().getSpawnLocation());
        }
        else {
            player.setCompassTarget(location);
        }


    }
}


package com.denizenscript.denizen.scripts.commands.player;

import com.denizenscript.denizen.utilities.Utilities;
import com.denizenscript.denizencore.utilities.debugging.Debug;
import com.denizenscript.denizen.objects.LocationTag;
import com.denizenscript.denizencore.exceptions.InvalidArgumentsException;
import com.denizenscript.denizencore.objects.Argument;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.scripts.ScriptEntry;
import com.denizenscript.denizencore.scripts.commands.AbstractCommand;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class CompassCommand extends AbstractCommand {

    public CompassCommand() {
        setName("compass");
        setSyntax("compass [<location>/reset]");
        setRequiredArguments(1, 1);
        isProcedural = false;
    }

    // <--[command]
    // @Name Compass
    // @Syntax compass [<location>/reset]
    // @Required 1
    // @Maximum 1
    // @Short Redirects the player's compass to target the given location.
    // @Group player
    //
    // @Description
    // Redirects the compass of the player, who is attached to the script queue.
    //
    // This is not the compass item, but the command is controlling the pointer the item should direct at.
    // This means that all item compasses will point the same direction but differently for each player.
    //
    // To affect an individual compass item, use <@link mechanism ItemTag.lodestone_location>
    //
    // The y-axis is not used but its fine to be included in the location argument.
    //
    // Reset argument will turn the direction to default (spawn or bed)
    //
    // @Tags
    // <PlayerTag.compass_target>
    //
    // @Usage
    // Use to reset the compass direction to its default.
    // - compass reset
    //
    // @Usage
    // Use to point with a compass to the player's current location.
    // - compass <player.location>
    //
    // @Usage
    // Use to point with a compass to the world's spawn location.
    // - compass <player.world.spawn_location>
    // -->

    @Override
    public void addCustomTabCompletions(TabCompletionsBuilder tab) {
        tab.addNotesOfType(LocationTag.class);
    }

    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {
        for (Argument arg : scriptEntry) {
            if (!scriptEntry.hasObject("location")
                    && arg.matchesArgumentType(LocationTag.class)) {
                scriptEntry.addObject("location", arg.asType(LocationTag.class));
            }
            else if (!scriptEntry.hasObject("reset")
                    && arg.matches("reset")) {
                scriptEntry.addObject("reset", new ElementTag(true));
            }
            else {
                arg.reportUnhandled();
            }
        }
        if (!scriptEntry.hasObject("location") && !scriptEntry.hasObject("reset")) {
            throw new InvalidArgumentsException("Missing location argument!");
        }

        scriptEntry.defaultObject("reset", new ElementTag(false));
    }

    @Override
    public void execute(ScriptEntry scriptEntry) {
        LocationTag location = scriptEntry.getObjectTag("location");
        ElementTag reset = scriptEntry.getElement("reset");
        Player player = Utilities.getEntryPlayer(scriptEntry).getPlayerEntity();
        if (scriptEntry.dbCallShouldDebug()) {
            Debug.report(scriptEntry, getName(), location, reset);
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

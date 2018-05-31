package net.aufdemrand.denizen.scripts.commands.player;

import net.aufdemrand.denizen.BukkitScriptEntryData;
import net.aufdemrand.denizen.objects.dLocation;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.aufdemrand.denizencore.exceptions.CommandExecutionException;
import net.aufdemrand.denizencore.exceptions.InvalidArgumentsException;
import net.aufdemrand.denizencore.objects.Element;
import net.aufdemrand.denizencore.objects.aH;
import net.aufdemrand.denizencore.scripts.ScriptEntry;
import net.aufdemrand.denizencore.scripts.commands.AbstractCommand;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class CompassCommand extends AbstractCommand {


    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {

        // Interpret arguments

        for (aH.Argument arg : aH.interpret(scriptEntry.getArguments())) {

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
    public void execute(ScriptEntry scriptEntry) throws CommandExecutionException {

        // Fetch required objects

        dLocation location = scriptEntry.getdObject("location");
        Element reset = scriptEntry.getElement("reset");
        Player player = ((BukkitScriptEntryData) scriptEntry.entryData).getPlayer().getPlayerEntity();


        // Debug the execution

        dB.report(scriptEntry, getName(), (location != null ? location.debug() : "") + reset.debug());

        if (reset.asBoolean()) {
            Location bed = player.getBedSpawnLocation();
            player.setCompassTarget(bed != null ? bed : player.getWorld().getSpawnLocation());
        }
        else {
            player.setCompassTarget(location);
        }


    }
}


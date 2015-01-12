package net.aufdemrand.denizen.scripts.commands.player;

import net.aufdemrand.denizen.BukkitScriptEntryData;
import net.aufdemrand.denizencore.exceptions.CommandExecutionException;
import net.aufdemrand.denizencore.exceptions.InvalidArgumentsException;
import net.aufdemrand.denizen.objects.aH;
import net.aufdemrand.denizen.objects.dLocation;
import net.aufdemrand.denizen.scripts.ScriptEntry;
import net.aufdemrand.denizen.scripts.commands.AbstractCommand;
import net.aufdemrand.denizen.utilities.debugging.dB;
import org.bukkit.entity.Player;

public class CompassCommand extends AbstractCommand {


    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {

        // Interpret arguments

        for (aH.Argument arg : aH.interpret(scriptEntry.getArguments())) {

             if (!scriptEntry.hasObject("location")
                 && arg.matchesArgumentType(dLocation.class))
                 scriptEntry.addObject("location", arg.asType(dLocation.class));

             else
                 arg.reportUnhandled();
        }

        // Check for required information
         if (!scriptEntry.hasObject("location"))
              throw new InvalidArgumentsException("Missing location argument!");
    }


    @Override
    public void execute(ScriptEntry scriptEntry) throws CommandExecutionException {

        // Fetch required objects

        dLocation location = (dLocation) scriptEntry.getObject("location");
        Player player = ((BukkitScriptEntryData)scriptEntry.entryData).getPlayer().getPlayerEntity();


        // Debug the execution

        dB.report(scriptEntry, getName(), location.debug());


        player.setCompassTarget(location);


    }
}


package net.aufdemrand.denizen.scripts.commands.player;

import net.aufdemrand.denizen.exceptions.CommandExecutionException;
import net.aufdemrand.denizen.exceptions.InvalidArgumentsException;
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

        }


        // Check for required information

         if (!scriptEntry.hasObject("location"))
              throw new InvalidArgumentsException(dB.Messages.ERROR_MISSING_OTHER, "LOCATION");

    }


    @Override
    public void execute(ScriptEntry scriptEntry) throws CommandExecutionException {

        // Fetch required objects

        dLocation location = (dLocation) scriptEntry.getObject("location");
        Player player = scriptEntry.getPlayer().getPlayerEntity();


        // Debug the execution

        dB.report(getName(), location.debug());


        player.setCompassTarget(location);


    }
}


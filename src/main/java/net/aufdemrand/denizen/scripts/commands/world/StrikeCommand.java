package net.aufdemrand.denizen.scripts.commands.world;

import net.aufdemrand.denizen.exceptions.CommandExecutionException;
import net.aufdemrand.denizen.exceptions.InvalidArgumentsException;
import net.aufdemrand.denizen.scripts.ScriptEntry;
import net.aufdemrand.denizen.scripts.commands.AbstractCommand;
import net.aufdemrand.denizen.objects.dLocation;
import net.aufdemrand.denizen.objects.aH;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.aufdemrand.denizen.utilities.debugging.dB.Messages;


public class StrikeCommand extends AbstractCommand {

	@Override
	public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {

        // Iterate through arguments
        for (aH.Argument arg : aH.interpret(scriptEntry.getArguments())) {
            if (!scriptEntry.hasObject("location")
                    && arg.matchesArgumentType(dLocation.class))
                // Location arg
                scriptEntry.addObject("location", arg.asType(dLocation.class).setPrefix("location"));

            else if (arg.matches("no_damage") || arg.matches("nodamage"))
                scriptEntry.addObject("damage", false);

		}

        // Check required args
		if (!scriptEntry.hasObject("location"))
            throw new InvalidArgumentsException(Messages.ERROR_MISSING_OTHER, "LOCATION");

        if (!scriptEntry.hasObject("damage"))
            scriptEntry.addObject("damage", true);
	}

	@Override
	public void execute(ScriptEntry scriptEntry) throws CommandExecutionException {

        // Extract objects from ScriptEntry
        dLocation location = (dLocation) scriptEntry.getObject("location");
        Boolean damage = (Boolean) scriptEntry.getObject("damage");

        // Debugger
        dB.report(getName(),
                location.debug()
                + aH.debugObj("Damageable", String.valueOf(damage)));

        // Play the sound
        if (damage)
            location.getWorld().strikeLightning(location);
        else
            location.getWorld().strikeLightningEffect(location);
	}

}

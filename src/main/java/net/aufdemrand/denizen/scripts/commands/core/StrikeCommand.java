package net.aufdemrand.denizen.scripts.commands.core;

import net.aufdemrand.denizen.exceptions.CommandExecutionException;
import net.aufdemrand.denizen.exceptions.InvalidArgumentsException;
import net.aufdemrand.denizen.scripts.ScriptEntry;
import net.aufdemrand.denizen.scripts.commands.AbstractCommand;
import net.aufdemrand.denizen.utilities.arguments.dLocation;
import net.aufdemrand.denizen.utilities.arguments.aH;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.aufdemrand.denizen.utilities.debugging.dB.Messages;


public class StrikeCommand extends AbstractCommand {

	@Override
	public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {

        // Initialize fields
        dLocation location = null;
        Boolean damage = true;

        // Iterate through arguments
		for (String arg : scriptEntry.getArguments()){
			if (aH.matchesLocation(arg))
                location = aH.getLocationFrom(arg);

			else if (aH.matchesArg("NO_DAMAGE, NODAMAGE", arg))
                damage = false;

            else throw new InvalidArgumentsException(Messages.ERROR_UNKNOWN_ARGUMENT, arg);
		}

        // Check required args
		if (location == null)
            throw new InvalidArgumentsException(Messages.ERROR_MISSING_OTHER, "LOCATION");

        // Stash args in ScriptEntry for use in execute()
        scriptEntry.addObject("location", location);
        scriptEntry.addObject("damage", damage);
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

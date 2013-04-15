package net.aufdemrand.denizen.scripts.commands.core;

import org.bukkit.Location;

import net.aufdemrand.denizen.exceptions.CommandExecutionException;
import net.aufdemrand.denizen.exceptions.InvalidArgumentsException;
import net.aufdemrand.denizen.scripts.ScriptEntry;
import net.aufdemrand.denizen.scripts.commands.AbstractCommand;
import net.aufdemrand.denizen.utilities.arguments.aH;
import net.aufdemrand.denizen.utilities.debugging.dB;

public class AnimateChestCommand extends AbstractCommand {
	
	enum ChestAction { OPEN, CLOSE }
	
	@Override
	public void parseArgs(ScriptEntry scriptEntry)
			throws InvalidArgumentsException {
		String chestAction = "OPEN";
		Location location = null;
		
		for (String arg : scriptEntry.getArguments()) {
			if (aH.matchesArg("OPEN, CLOSE", arg)) {
				chestAction = aH.getStringFrom(arg);
				dB.echoDebug("...chest action set: " + chestAction);
			} else if (aH.matchesLocation(arg)) {
				location = aH.getLocationFrom(arg);
				dB.echoDebug("...location set");
			} else throw new InvalidArgumentsException(dB.Messages.ERROR_UNKNOWN_ARGUMENT, arg);
		}
		
		if (location == null) dB.echoError("...location is invalid");
		
		scriptEntry.addObject("location", location)
			.addObject("chestAction", chestAction);
	}

	@Override
	public void execute(ScriptEntry scriptEntry)
			throws CommandExecutionException {
		Location location = (Location) scriptEntry.getObject("location");
		ChestAction action = ChestAction.valueOf(((String) scriptEntry.getObject("chestAction")).toUpperCase());
		
		switch (action) {
		case OPEN:
			scriptEntry.getPlayer().playNote(location, (byte)1, (byte)1);
			dB.echoDebug("...opening chest");
			break;
		case CLOSE:
			scriptEntry.getPlayer().playNote(location, (byte)1, (byte)0);
			dB.echoDebug("...closing chest");
			break;
		default:
			break;
		}
	}

}

package net.aufdemrand.denizen.scripts.commands.core;

import net.aufdemrand.denizen.exceptions.CommandExecutionException;
import net.aufdemrand.denizen.exceptions.InvalidArgumentsException;
import net.aufdemrand.denizen.npc.dNPC;
import net.aufdemrand.denizen.npc.traits.SittingTrait;
import net.aufdemrand.denizen.scripts.ScriptEntry;
import net.aufdemrand.denizen.scripts.commands.AbstractCommand;
import net.aufdemrand.denizen.utilities.arguments.aH;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.aufdemrand.denizen.utilities.debugging.dB.Messages;

import org.bukkit.Location;

public class SitCommand extends AbstractCommand {
	
	Location location = null;
	
	@Override
	public void parseArgs(ScriptEntry scriptEntry)
			throws InvalidArgumentsException {
		for (String arg : scriptEntry.getArguments()) {
			if (aH.matchesLocation(arg)) {
				location = aH.getLocationFrom(arg);
				dB.echoDebug("...location set.");
			} else throw new InvalidArgumentsException(Messages.ERROR_UNKNOWN_ARGUMENT, arg);
		}
		
		scriptEntry.addObject("location", location);
		
	}

	@Override
	public void execute(ScriptEntry scriptEntry)
			throws CommandExecutionException {
		Location location = (Location) scriptEntry.getObject("location");
		dNPC npc = scriptEntry.getNPC();
		
		if (!npc.getCitizen().hasTrait(SittingTrait.class))
			npc.getCitizen().addTrait(SittingTrait.class);
		
		if (npc.getCitizen().getTrait(SittingTrait.class).isSitting()) {
			dB.echoError("...NPC is already sitting");
			return;
		}
		
		if (location != null) {
			npc.getCitizen().getTrait(SittingTrait.class).sit(location);
			dB.echoDebug("...NPC teleported to chair, then sits");
		} else {
			npc.getCitizen().getTrait(SittingTrait.class).sit();
			dB.log("...npc sits");
		}
		
		
		
	}

}

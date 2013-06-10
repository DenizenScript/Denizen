package net.aufdemrand.denizen.scripts.commands.npc;

import net.aufdemrand.denizen.exceptions.CommandExecutionException;
import net.aufdemrand.denizen.exceptions.InvalidArgumentsException;
import net.aufdemrand.denizen.npc.traits.FishingTrait;
import net.aufdemrand.denizen.scripts.ScriptEntry;
import net.aufdemrand.denizen.scripts.commands.AbstractCommand;
import net.aufdemrand.denizen.objects.aH;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.aufdemrand.denizen.utilities.debugging.dB.Messages;
import net.citizensnpcs.api.npc.NPC;

import org.bukkit.Location;

public class FishCommand extends AbstractCommand {
	
	@Override
	public void parseArgs(ScriptEntry scriptEntry)
			throws InvalidArgumentsException {
		Location location = null;
		Boolean stopping = false;
		Boolean catchFish = false;
		
		for (String arg : scriptEntry.getArguments()) {
			if (aH.matchesLocation(arg)) {
				location = aH.getLocationFrom(arg);
				dB.echoDebug("...location set");
				continue;
			} else if (aH.matchesArg("CATCHFISH", arg)) {
				catchFish = true;
				dB.echoDebug("...npc will catch fish");
			} else if (aH.matchesArg("STOP", arg)) {
				stopping = true;
				dB.echoDebug("...stopping");
				continue;
			} else throw new InvalidArgumentsException(Messages.ERROR_UNKNOWN_ARGUMENT, arg);
		}
		
		scriptEntry.addObject("location", location)
			.addObject("stopping", stopping)
			.addObject("catchFish", catchFish);
	}

	@Override
	public void execute(ScriptEntry scriptEntry)
			throws CommandExecutionException {
		Boolean stopping = (Boolean) scriptEntry.getObject("stopping");
		Boolean catchFish = (Boolean) scriptEntry.getObject("catchFish");
		NPC npc = scriptEntry.getNPC().getCitizen();
		FishingTrait trait = new FishingTrait();
		
		if (!npc.hasTrait(FishingTrait.class)) 
			npc.addTrait(FishingTrait.class);
		
		if (stopping) {
			trait.stopFishing();
			return;
		}
		
		Location location = (Location) scriptEntry.getObject("location");
		if (location == null) {
			dB.echoError("...no location specified!");
			return;
		}
		
		trait.startFishing(location);
		if (catchFish) {
			trait.setCatchFish(true);
		}
		return;
		
	}
}

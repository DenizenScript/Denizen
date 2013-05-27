package net.aufdemrand.denizen.scripts.commands.core;

import net.aufdemrand.denizen.objects.dLocation;
import org.bukkit.entity.EntityType;

import net.aufdemrand.denizen.exceptions.CommandExecutionException;
import net.aufdemrand.denizen.exceptions.InvalidArgumentsException;
import net.aufdemrand.denizen.npc.traits.SittingTrait;
import net.aufdemrand.denizen.scripts.ScriptEntry;
import net.aufdemrand.denizen.scripts.commands.AbstractCommand;
import net.aufdemrand.denizen.objects.aH;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.aufdemrand.denizen.utilities.debugging.dB.Messages;
import net.citizensnpcs.api.npc.NPC;


public class SitCommand extends AbstractCommand {
	
	dLocation location = null;
	
	@Override
	public void parseArgs(ScriptEntry scriptEntry)
			throws InvalidArgumentsException {
		for (String arg : scriptEntry.getArguments()) {
			if (aH.matchesLocation(arg)) {
				location = aH.getLocationFrom(arg);
			} else throw new InvalidArgumentsException(Messages.ERROR_UNKNOWN_ARGUMENT, arg);
		}
		
		scriptEntry.addObject("location", location);
		
	}

	@Override
	public void execute(ScriptEntry scriptEntry)
			throws CommandExecutionException {
		dLocation location = (dLocation) scriptEntry.getObject("location");
		NPC npc = scriptEntry.getNPC().getCitizen();
		SittingTrait trait = npc.getTrait(SittingTrait.class);
		
		if (npc.getBukkitEntity().getType() != EntityType.PLAYER) {
			dB.echoError("...only Player type NPCs can sit!");
			return;
		}
				
		if (!npc.hasTrait(SittingTrait.class)) {
			npc.addTrait(SittingTrait.class);
			dB.echoDebug("...added sitting trait");
		}
		
		if (trait.isSitting()) {
			dB.echoError("...NPC is already sitting");
			return;
		}
		
		if (location != null) {
			trait.sit(location);
		} else {
			trait.sit();
		}
	}

}

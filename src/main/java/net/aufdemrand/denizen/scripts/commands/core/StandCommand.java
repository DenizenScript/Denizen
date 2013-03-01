package net.aufdemrand.denizen.scripts.commands.core;

import net.aufdemrand.denizen.exceptions.CommandExecutionException;
import net.aufdemrand.denizen.exceptions.InvalidArgumentsException;
import net.aufdemrand.denizen.npc.dNPC;
import net.aufdemrand.denizen.npc.traits.SittingTrait;
import net.aufdemrand.denizen.scripts.ScriptEntry;
import net.aufdemrand.denizen.scripts.commands.AbstractCommand;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.aufdemrand.denizen.utilities.debugging.dB.Messages;

public class StandCommand extends AbstractCommand{

	@Override
	public void parseArgs(ScriptEntry scriptEntry)
			throws InvalidArgumentsException {
		for (String arg: scriptEntry.getArguments())
			throw new InvalidArgumentsException(Messages.ERROR_UNKNOWN_ARGUMENT, arg);
		
	}

	@Override
	public void execute(ScriptEntry scriptEntry)
			throws CommandExecutionException {
		
		dNPC npc = scriptEntry.getNPC();
		
		if (!npc.getCitizen().hasTrait(SittingTrait.class)){
			npc.getCitizen().addTrait(SittingTrait.class);
			dB.echoDebug("...added sitting trait");
		}
			
		
		if (!npc.getCitizen().getTrait(SittingTrait.class).isSitting()) {
			dB.echoError("...NPC is already standing, removing trait");
			npc.getCitizen().removeTrait(SittingTrait.class);
			return;
		}
		
		npc.getCitizen().getTrait(SittingTrait.class).stand();
		npc.getCitizen().removeTrait(SittingTrait.class);
		
	}

}

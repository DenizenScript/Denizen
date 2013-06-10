package net.aufdemrand.denizen.scripts.commands.npc;

import net.aufdemrand.denizen.exceptions.CommandExecutionException;
import net.aufdemrand.denizen.exceptions.InvalidArgumentsException;
import net.aufdemrand.denizen.scripts.ScriptEntry;
import net.aufdemrand.denizen.scripts.commands.AbstractCommand;
import net.aufdemrand.denizen.objects.aH;
import net.aufdemrand.denizen.objects.aH.ArgumentType;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.aufdemrand.denizen.utilities.debugging.dB.Messages;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.trait.LookClose;

/**
 * Configures the LookClose Trait for a NPC.
 * 
 * @author Jeremy Schroeder
 */

public class LookcloseCommand extends AbstractCommand {

	/* LOOKCLOSE [TOGGLE:TRUE|FALSE] (RANGE:#.#) (REALISTIC)  */

	/* 
	 * Arguments: [] - Required, () - Optional
	 * 
	 */

    boolean toggle;
    boolean realistic;
    Double range;
    NPC npc;

	@Override
	public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {

		// Set some defaults based on the scriptEntry
	    npc = null;
	    range = null;
	    realistic = false;
	    toggle = true;
	    if (scriptEntry.getNPC() != null) npc = scriptEntry.getNPC().getCitizen();
	    
	    // Parse Arguments
		for (String arg : scriptEntry.getArguments()) {

			if (aH.matchesArg("REALISTIC", arg)) {
				realistic = true;
				dB.echoDebug(Messages.DEBUG_SET_TYPE, arg);

            }   else if (aH.matchesValueArg("RANGE", arg, ArgumentType.Double)) {
                range = aH.getDoubleFrom(arg);
                dB.echoDebug(Messages.DEBUG_SET_RANGE, String.valueOf(range));

            }   else if (aH.matchesToggle(arg)) {
                toggle = aH.getBooleanFrom(arg);
                dB.echoDebug(Messages.DEBUG_TOGGLE, String.valueOf(toggle));

            }	else throw new InvalidArgumentsException(Messages.ERROR_UNKNOWN_ARGUMENT, arg);
		}

        if (scriptEntry.getNPC() == null)
            throw new InvalidArgumentsException(Messages.ERROR_NO_NPCID);


        scriptEntry.addObject("realistic", realistic)
                .addObject("range", range)
                .addObject("toggle", toggle);
	}
	
	@Override
	public void execute(ScriptEntry scriptEntry) throws CommandExecutionException {



		// Get the instance of the trait that belongs to the target NPC
		LookClose trait = npc.getTrait(LookClose.class);
		trait.lookClose(toggle);
		if (realistic) {
		    trait.setRealisticLooking(true);
		}

		if (range != null) {
		    trait.setRange(range.intValue());
		}
	}
	
    @Override
    public void onEnable() {
        
    }
}
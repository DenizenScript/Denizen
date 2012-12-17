package net.aufdemrand.denizen.scripts.commands.core;

import net.aufdemrand.denizen.exceptions.CommandExecutionException;
import net.aufdemrand.denizen.exceptions.InvalidArgumentsException;
import net.aufdemrand.denizen.npc.traits.TriggerTrait;
import net.aufdemrand.denizen.scripts.ScriptEntry;
import net.aufdemrand.denizen.scripts.commands.AbstractCommand;
import net.aufdemrand.denizen.scripts.helpers.ArgumentHelper.ArgumentType;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.aufdemrand.denizen.utilities.debugging.dB.Messages;
import net.citizensnpcs.api.npc.NPC;

/**
 * Configures the TriggerTrait for a NPC.
 * 
 * @author Jeremy Schroeder
 */

public class TriggerCommand extends AbstractCommand {

	/* TRIGGER [NAME:Trigger_Name] [(TOGGLE:TRUE|FALSE)|(COOLDOWN:#.#)|(RADIUS:#)]  */

	/* 
	 * Arguments: [] - Required, () - Optional 
	 * 
	 */

    String triggerName;
    Boolean toggle;
    Double cooldown;
    Integer radius; 
    NPC npc;

	@Override
	public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {

		// Set some defaults based on the scriptEntry'
	    triggerName = null;
	    toggle = null;
	    cooldown = null;
	    radius = null; 
	    npc = null;
	    if (scriptEntry.getNPC() != null) npc = scriptEntry.getNPC().getCitizen();
	    
	    // Parse Arguments
		for (String arg : scriptEntry.getArguments()) {

			if (aH.matchesValueArg("COOLDOWN", arg, ArgumentType.Double)) {
				cooldown = aH.getDoubleFrom(arg);
				dB.echoDebug(Messages.DEBUG_SET_COOLDOWN, String.valueOf(cooldown));
				continue;

			}	else if (aH.matchesValueArg("NAME", arg, ArgumentType.String)) {
				triggerName = aH.getStringFrom(arg);
				dB.echoDebug(Messages.DEBUG_SET_NAME, triggerName);
				continue;

            }   else if (aH.matchesValueArg("RADIUS", arg, ArgumentType.Integer)) {
                radius = aH.getIntegerFrom(arg);
                dB.echoDebug(Messages.DEBUG_SET_RADIUS, String.valueOf(radius));
                continue;
                
			}   else if (aH.matchesToggle(arg)) {
                toggle = aH.getBooleanFrom(arg);
                dB.echoDebug(Messages.DEBUG_TOGGLE, String.valueOf(toggle));
                continue;

			}	else throw new InvalidArgumentsException(Messages.ERROR_UNKNOWN_ARGUMENT, arg);
		}
		
		if (triggerName == null) throw new InvalidArgumentsException(Messages.ERROR_MISSING_OTHER, "NAME");
		if (radius == null && toggle == null && cooldown == null) throw new InvalidArgumentsException(Messages.ERROR_MISSING_OTHER, "RADIUS, COOLDOWN or TOGGLE");
	}

	@Override
	public void execute(String commandName) throws CommandExecutionException {
		if (npc == null) throw new CommandExecutionException(Messages.ERROR_NO_NPCID);
		
		if (!npc.hasTrait(TriggerTrait.class)) npc.addTrait(TriggerTrait.class);
		TriggerTrait trait = npc.getTrait(TriggerTrait.class);
		
		if (radius != null) {
		    trait.setLocalRadius(triggerName, radius);
		}
		
		if (toggle != null) {
		    trait.toggleTrigger(triggerName);
		}
		
		if (cooldown != null) {
		    trait.setLocalCooldown(triggerName, cooldown);
		}
	}
	
    @Override
    public void onEnable() {
        
    }
}
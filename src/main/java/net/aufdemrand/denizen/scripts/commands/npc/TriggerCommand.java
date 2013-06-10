package net.aufdemrand.denizen.scripts.commands.npc;

import net.aufdemrand.denizen.exceptions.CommandExecutionException;
import net.aufdemrand.denizen.exceptions.InvalidArgumentsException;
import net.aufdemrand.denizen.npc.traits.TriggerTrait;
import net.aufdemrand.denizen.scripts.ScriptEntry;
import net.aufdemrand.denizen.scripts.commands.AbstractCommand;
import net.aufdemrand.denizen.objects.Duration;
import net.aufdemrand.denizen.objects.aH;
import net.aufdemrand.denizen.objects.aH.ArgumentType;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.aufdemrand.denizen.utilities.debugging.dB.Messages;
import net.citizensnpcs.api.npc.NPC;

/**
 * Configures the TriggerTrait for a NPC.
 * 
 * @author Jeremy Schroeder
 */

public class TriggerCommand extends AbstractCommand {

    private enum Toggle {TOGGLE, TRUE, FALSE}

	@Override
	public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {

        // Initialize required fields
	    String trigger = null;
        Toggle toggle = Toggle.TOGGLE;
        Duration cooldown = new Duration(-1d);
        int radius = -1;

	    // Parse arguments
		for (String arg : scriptEntry.getArguments()) {

			if (aH.matchesValueArg("COOLDOWN", arg, ArgumentType.Duration)) {
				cooldown = aH.getDurationFrom(arg);
                cooldown.setPrefix("Cooldown");
            }

            else if (aH.matchesValueArg("RADIUS", arg, ArgumentType.Integer))
                radius = aH.getIntegerFrom(arg);

			else if (aH.matchesToggle(arg))
                toggle = Toggle.valueOf(aH.getStringFrom(arg.toUpperCase()));

            else if (aH.matchesValueArg("NAME", arg, ArgumentType.String))
                trigger = aH.getStringFrom(arg);

            else if (denizen.getTriggerRegistry().get(arg) != null)
                trigger = aH.getStringFrom(arg);

			else throw new InvalidArgumentsException(Messages.ERROR_UNKNOWN_ARGUMENT, arg);
		}

        // Check required arguments
		if (trigger == null) throw new InvalidArgumentsException(Messages.ERROR_MISSING_OTHER, "NAME");

        // Store objects in ScriptEntry for execute()
        scriptEntry.addObject("trigger", trigger);
        scriptEntry.addObject("cooldown", cooldown);
        scriptEntry.addObject("toggle", toggle);
        scriptEntry.addObject("radius", radius);

	}

	@Override
	public void execute(ScriptEntry scriptEntry) throws CommandExecutionException {

        Toggle toggle = (Toggle) scriptEntry.getObject("toggle");
        String trigger = (String) scriptEntry.getObject("trigger");
        Integer radius = (Integer) scriptEntry.getObject("radius");
        Duration cooldown = (Duration) scriptEntry.getObject("cooldown");
        NPC npc = scriptEntry.getNPC().getCitizen();

        dB.echoApproval("Executing '" + getName() + "': "
                + "Trigger='" + trigger + "', "
                + "Toggle='" + toggle.toString() + "', "
                + (radius > 0 ? "Radius='" + radius + "', " : "Radius='Unchanged', ")
                + (cooldown.getSeconds() > 0 ? "Cooldown='" + cooldown.debug() + "', " : "Cooldown='Unchanged', ")
                + "NPC='" + scriptEntry.getNPC() + "'");

        // Add trigger trait
		if (!npc.hasTrait(TriggerTrait.class)) npc.addTrait(TriggerTrait.class);

        TriggerTrait trait = npc.getTrait(TriggerTrait.class);

        if (toggle == Toggle.TOGGLE)
            trait.toggleTrigger(trigger);
        else if (toggle == Toggle.TRUE)
            trait.toggleTrigger(trigger, true);
        else trait.toggleTrigger(trigger, false);

        if (radius > 0)
		    trait.setLocalRadius(trigger, radius);
		
		if (cooldown.getSeconds() > 0)
		    trait.setLocalCooldown(trigger, cooldown.getSeconds());
	}

}
package net.aufdemrand.denizen.scripts.commands.core;

import net.aufdemrand.denizen.exceptions.CommandExecutionException;
import net.aufdemrand.denizen.exceptions.InvalidArgumentsException;
import net.aufdemrand.denizen.scripts.ScriptEntry;
import net.aufdemrand.denizen.scripts.commands.AbstractCommand;
import net.aufdemrand.denizen.utilities.debugging.Debugger.Messages;
import net.citizensnpcs.api.npc.NPC;

/**
 * Unsets the Denizen from the Engage List. 
 * When ENGAGEd, a Denizen will not interact with a Player until DISENGAGEd (or timed out).
 * 
 * @author Jeremy Schroeder
 */

public class DisengageCommand extends AbstractCommand {

    /* DISENGAGE (NPCID:#) */

    /* Arguments: [] - Required, () - Optional
     * (NPCID:#) Changes the Denizen affected to the Citizens2 NPCID specified
     * 
     */

    NPC npc;

    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {

        if (scriptEntry.getDenizen() == null)
            throw new InvalidArgumentsException(Messages.ERROR_NO_NPCID);

        // Set some defaults based on the scriptEntry
        npc = scriptEntry.getDenizen().getCitizen();

    }

    @Override
    public void execute(String commandName) throws CommandExecutionException {
        denizen.getCommandRegistry().get(EngageCommand.class).setEngaged(npc, false);
    }

    @Override
    public void onEnable() {
        // TODO Auto-generated method stub
        
    }

}
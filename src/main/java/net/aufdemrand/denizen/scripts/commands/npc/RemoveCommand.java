package net.aufdemrand.denizen.scripts.commands.npc;

import net.aufdemrand.denizen.exceptions.CommandExecutionException;
import net.aufdemrand.denizen.exceptions.InvalidArgumentsException;
import net.aufdemrand.denizen.scripts.ScriptEntry;
import net.aufdemrand.denizen.scripts.commands.AbstractCommand;

/**
 * Safely removes an NPC.
 * 
 * To be expanded to use multiple NPCs and possibly other entities as well.
 *
 */
public class RemoveCommand extends AbstractCommand {

    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {

    }

    @Override
    public void execute(ScriptEntry scriptEntry) throws CommandExecutionException {
        
    	scriptEntry.getNPC().getCitizen().destroy();
    }

}
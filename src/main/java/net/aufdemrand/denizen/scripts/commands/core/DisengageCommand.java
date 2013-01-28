package net.aufdemrand.denizen.scripts.commands.core;

import net.aufdemrand.denizen.exceptions.CommandExecutionException;
import net.aufdemrand.denizen.exceptions.InvalidArgumentsException;
import net.aufdemrand.denizen.scripts.ScriptEntry;
import net.aufdemrand.denizen.scripts.commands.AbstractCommand;
import net.aufdemrand.denizen.utilities.arguments.aH;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.aufdemrand.denizen.utilities.debugging.dB.Messages;

/**
 * Unsets the Denizen from the Engage List. 
 * When ENGAGEd, a Denizen will not interact with a Player until DISENGAGEd (or timed out).
 * 
 * @author aufdemrand
 */

public class DisengageCommand extends AbstractCommand {

    /* DISENGAGE (NPCID:#) */

    /* Arguments: [] - Required, () - Optional
     * (NPCID:#) Changes the Denizen affected to the Citizens2 NPCID specified
     * 
     */

    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {

        // Make sure NPC is available
        if (scriptEntry.getNPC() == null)
            throw new InvalidArgumentsException(Messages.ERROR_NO_NPCID);

    }

    @Override
    public void execute(ScriptEntry scriptEntry) throws CommandExecutionException {

        // Report to dB
        dB.report(getName(),
                aH.debugObj("NPC", scriptEntry.getNPC().toString()));

        // Set Disengaged
        denizen.getCommandRegistry().get(EngageCommand.class)
                .setEngaged(scriptEntry.getNPC().getCitizen(), false);
    }

}
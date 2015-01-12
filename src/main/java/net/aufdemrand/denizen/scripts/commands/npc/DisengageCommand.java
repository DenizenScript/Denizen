package net.aufdemrand.denizen.scripts.commands.npc;

import net.aufdemrand.denizen.BukkitScriptEntryData;
import net.aufdemrand.denizencore.exceptions.CommandExecutionException;
import net.aufdemrand.denizencore.exceptions.InvalidArgumentsException;
import net.aufdemrand.denizen.scripts.ScriptEntry;
import net.aufdemrand.denizen.scripts.commands.AbstractCommand;
import net.aufdemrand.denizen.objects.aH;
import net.aufdemrand.denizen.utilities.debugging.dB;

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
        if (((BukkitScriptEntryData)scriptEntry.entryData).getNPC() == null)
            throw new InvalidArgumentsException("This command requires a linked NPC!");

    }

    @Override
    public void execute(ScriptEntry scriptEntry) throws CommandExecutionException {

        // Report to dB
        dB.report(scriptEntry, getName(),
                aH.debugObj("NPC", ((BukkitScriptEntryData)scriptEntry.entryData).getNPC().toString()));

        // Set Disengaged
        EngageCommand.setEngaged(((BukkitScriptEntryData)scriptEntry.entryData).getNPC().getCitizen(), false);
    }
}

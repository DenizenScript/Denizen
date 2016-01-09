package net.aufdemrand.denizen.scripts.commands.npc;

import net.aufdemrand.denizen.BukkitScriptEntryData;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.aufdemrand.denizencore.exceptions.CommandExecutionException;
import net.aufdemrand.denizencore.exceptions.InvalidArgumentsException;
import net.aufdemrand.denizencore.objects.aH;
import net.aufdemrand.denizencore.scripts.ScriptEntry;
import net.aufdemrand.denizencore.scripts.commands.AbstractCommand;

public class DisengageCommand extends AbstractCommand {

    /* DISENGAGE (NPCID:#) */

    /* Arguments: [] - Required, () - Optional
     * (NPCID:#) Changes the Denizen affected to the Citizens2 NPCID specified
     *
     */

    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {

        // Make sure NPC is available
        if (((BukkitScriptEntryData) scriptEntry.entryData).getNPC() == null) {
            throw new InvalidArgumentsException("This command requires a linked NPC!");
        }

    }

    @Override
    public void execute(ScriptEntry scriptEntry) throws CommandExecutionException {

        // Report to dB
        dB.report(scriptEntry, getName(),
                aH.debugObj("NPC", ((BukkitScriptEntryData) scriptEntry.entryData).getNPC().toString()));

        // Set Disengaged
        EngageCommand.setEngaged(((BukkitScriptEntryData) scriptEntry.entryData).getNPC().getCitizen(), false);
    }
}

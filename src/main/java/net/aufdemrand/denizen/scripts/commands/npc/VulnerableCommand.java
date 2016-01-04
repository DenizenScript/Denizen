package net.aufdemrand.denizen.scripts.commands.npc;

import net.aufdemrand.denizen.BukkitScriptEntryData;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.aufdemrand.denizencore.exceptions.CommandExecutionException;
import net.aufdemrand.denizencore.exceptions.InvalidArgumentsException;
import net.aufdemrand.denizencore.objects.aH;
import net.aufdemrand.denizencore.scripts.ScriptEntry;
import net.aufdemrand.denizencore.scripts.commands.AbstractCommand;
import net.citizensnpcs.api.npc.NPC;

public class VulnerableCommand extends AbstractCommand {

    enum Toggle {TRUE, FALSE, TOGGLE}

    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {

        // Initialize fields
        Toggle vulnerable = Toggle.TRUE;
        // TODO: UPDATE COMMAND PARSING
        for (String arg : scriptEntry.getArguments()) {
            if (aH.matchesState(arg)) {
                vulnerable = Toggle.valueOf(aH.getStringFrom(arg).toUpperCase());
            }
        }

        if (((BukkitScriptEntryData) scriptEntry.entryData).getNPC() == null) {
            throw new InvalidArgumentsException("This command requires a linked NPC!");
        }

        // Add objects that need to be passed to execute() to the scriptEntry
        scriptEntry.addObject("vulnerable", vulnerable);
    }

    @Override
    public void execute(ScriptEntry scriptEntry) throws CommandExecutionException {
        // Fetch objects
        Toggle toggle = (Toggle) scriptEntry.getObject("vulnerable");

        // Report to dB
        dB.report(scriptEntry, getName(),
                aH.debugObj("NPC", ((BukkitScriptEntryData) scriptEntry.entryData).getNPC().toString()) +
                        aH.debugObj("Toggle", toggle.toString()));

        NPC npc = ((BukkitScriptEntryData) scriptEntry.entryData).getNPC().getCitizen();

        boolean vulnerable;

        if (toggle == Toggle.TOGGLE) {
            vulnerable = !npc.data().get(NPC.DEFAULT_PROTECTED_METADATA, true);
        }

        else {
            vulnerable = Boolean.valueOf(toggle.toString());
        }

        npc.data().set(NPC.DEFAULT_PROTECTED_METADATA, !vulnerable);
    }
}

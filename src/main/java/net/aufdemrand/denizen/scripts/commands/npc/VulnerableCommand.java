package net.aufdemrand.denizen.scripts.commands.npc;

import net.aufdemrand.denizen.exceptions.CommandExecutionException;
import net.aufdemrand.denizen.exceptions.InvalidArgumentsException;
import net.aufdemrand.denizen.scripts.ScriptEntry;
import net.aufdemrand.denizen.scripts.commands.AbstractCommand;
import net.aufdemrand.denizen.objects.aH;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.aufdemrand.denizen.utilities.debugging.dB.Messages;
import net.citizensnpcs.api.npc.NPC;

/**
 * Changes/toggles an NPC's vulnerable state.
 *
 * @author aufdemrand
 *
 */
public class VulnerableCommand extends AbstractCommand {

    enum Toggle { TRUE, FALSE, TOGGLE }

    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {

        // Initialize fields
        Toggle vulnerable = Toggle.TRUE;

        for (String arg : scriptEntry.getArguments())
            if (aH.matchesState(arg))
                vulnerable = Toggle.valueOf(aH.getStringFrom(arg).toUpperCase());

        if (scriptEntry.getNPC() == null)
            throw new InvalidArgumentsException(Messages.ERROR_NO_NPCID);

        // Add objects that need to be passed to execute() to the scriptEntry
        scriptEntry.addObject("vulnerable", vulnerable);
    }

    @Override
    public void execute(ScriptEntry scriptEntry) throws CommandExecutionException {
        // Fetch objects
        Toggle toggle = (Toggle) scriptEntry.getObject("vulnerable");

        // Report to dB
        dB.report(getName(),
                aH.debugObj("NPC", scriptEntry.getNPC().toString()) +
                        aH.debugObj("Toggle", toggle.toString()));

        NPC npc = scriptEntry.getNPC().getCitizen();

        boolean vulnerable;

        if (toggle == Toggle.TOGGLE)
            vulnerable = !npc.data().get(NPC.DEFAULT_PROTECTED_METADATA, true);

        else vulnerable = Boolean.valueOf(toggle.toString());

        npc.data().set(NPC.DEFAULT_PROTECTED_METADATA, !vulnerable);
    }
}

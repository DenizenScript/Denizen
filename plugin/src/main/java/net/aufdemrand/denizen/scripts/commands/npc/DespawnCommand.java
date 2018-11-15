package net.aufdemrand.denizen.scripts.commands.npc;

import net.aufdemrand.denizen.BukkitScriptEntryData;
import net.aufdemrand.denizen.objects.dNPC;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.aufdemrand.denizencore.exceptions.CommandExecutionException;
import net.aufdemrand.denizencore.exceptions.InvalidArgumentsException;
import net.aufdemrand.denizencore.objects.aH;
import net.aufdemrand.denizencore.objects.dList;
import net.aufdemrand.denizencore.scripts.ScriptEntry;
import net.aufdemrand.denizencore.scripts.commands.AbstractCommand;
import net.citizensnpcs.api.event.DespawnReason;
import net.citizensnpcs.api.trait.trait.Spawned;

import java.util.Arrays;
import java.util.List;

public class DespawnCommand extends AbstractCommand {

    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {

        for (aH.Argument arg : aH.interpret(scriptEntry.getArguments())) {

            if (!scriptEntry.hasObject("npcs")
                    && arg.matchesArgumentList(dNPC.class)) {
                scriptEntry.addObject("npcs", arg.asType(dList.class).filter(dNPC.class));
            }
            else {
                arg.reportUnhandled();
            }
        }

        if (!scriptEntry.hasObject("npcs")) {
            if (((BukkitScriptEntryData) scriptEntry.entryData).hasNPC()) {
                scriptEntry.addObject("npcs", Arrays.asList(((BukkitScriptEntryData) scriptEntry.entryData).getNPC()));
            }
            else {
                throw new InvalidArgumentsException("Must specify a valid list of NPCs!");
            }
        }

    }

    @SuppressWarnings("unchecked")
    @Override
    public void execute(final ScriptEntry scriptEntry) throws CommandExecutionException {

        // Get objects
        List<dNPC> npcs = (List<dNPC>) scriptEntry.getObject("npcs");

        // Report to dB
        if (scriptEntry.dbCallShouldDebug()) {
            dB.report(scriptEntry, getName(),
                    aH.debugObj("NPCs", npcs.toString()));
        }

        for (dNPC npc : npcs) {
            if (npc.isSpawned()) {
                if (npc.getCitizen().hasTrait(Spawned.class)) {
                    npc.getCitizen().getTrait(Spawned.class).setSpawned(false);
                }
                npc.getCitizen().despawn(DespawnReason.PLUGIN);
            }
        }
    }
}

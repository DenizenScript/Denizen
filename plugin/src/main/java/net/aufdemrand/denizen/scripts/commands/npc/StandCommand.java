package net.aufdemrand.denizen.scripts.commands.npc;

import net.aufdemrand.denizen.BukkitScriptEntryData;
import net.aufdemrand.denizen.npc.traits.SittingTrait;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.aufdemrand.denizencore.exceptions.CommandExecutionException;
import net.aufdemrand.denizencore.exceptions.InvalidArgumentsException;
import net.aufdemrand.denizencore.objects.aH;
import net.aufdemrand.denizencore.scripts.ScriptEntry;
import net.aufdemrand.denizencore.scripts.commands.AbstractCommand;
import net.citizensnpcs.api.npc.NPC;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Ocelot;
import org.bukkit.entity.Wolf;

public class StandCommand extends AbstractCommand {

    @Override
    public void parseArgs(ScriptEntry scriptEntry)
            throws InvalidArgumentsException {
        //stand should have no additional arguments
        for (aH.Argument arg : aH.interpret(scriptEntry.getArguments())) {
            arg.reportUnhandled();
        }
        if (!((BukkitScriptEntryData) scriptEntry.entryData).hasNPC()) {
            throw new InvalidArgumentsException("This command requires a linked NPC!");
        }

    }

    @Override
    public void execute(ScriptEntry scriptEntry) throws CommandExecutionException {

        if (((BukkitScriptEntryData) scriptEntry.entryData).getNPC().getEntityType() != EntityType.PLAYER
                && ((BukkitScriptEntryData) scriptEntry.entryData).getNPC().getEntityType() != EntityType.OCELOT
                && ((BukkitScriptEntryData) scriptEntry.entryData).getNPC().getEntityType() != EntityType.WOLF) {
            dB.echoError(scriptEntry.getResidingQueue(), "...only Player, ocelot, or wolf type NPCs can sit!");
            return;
        }

        dB.report(scriptEntry, getName(), aH.debugObj("npc", ((BukkitScriptEntryData) scriptEntry.entryData).getNPC()));

        if (((BukkitScriptEntryData) scriptEntry.entryData).getNPC().getEntityType() == EntityType.OCELOT) {
            ((Ocelot) ((BukkitScriptEntryData) scriptEntry.entryData).getNPC().getEntity()).setSitting(false);
        }

        else if (((BukkitScriptEntryData) scriptEntry.entryData).getNPC().getEntityType() == EntityType.WOLF) {
            ((Wolf) ((BukkitScriptEntryData) scriptEntry.entryData).getNPC().getEntity()).setSitting(false);
        }

        else {
            NPC npc = ((BukkitScriptEntryData) scriptEntry.entryData).getNPC().getCitizen();
            SittingTrait trait = npc.getTrait(SittingTrait.class);

            if (!npc.hasTrait(SittingTrait.class)) {
                npc.addTrait(SittingTrait.class);
                dB.echoDebug(scriptEntry, "...added sitting trait");
            }

            trait.stand();
            npc.removeTrait(SittingTrait.class);
        }
    }
}

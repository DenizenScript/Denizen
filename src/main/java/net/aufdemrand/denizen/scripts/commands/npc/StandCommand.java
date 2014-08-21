package net.aufdemrand.denizen.scripts.commands.npc;

import net.aufdemrand.denizen.exceptions.CommandExecutionException;
import net.aufdemrand.denizen.exceptions.InvalidArgumentsException;
import net.aufdemrand.denizen.npc.traits.SittingTrait;
import net.aufdemrand.denizen.objects.aH;
import net.aufdemrand.denizen.scripts.ScriptEntry;
import net.aufdemrand.denizen.scripts.commands.AbstractCommand;
import net.aufdemrand.denizen.utilities.debugging.dB;
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
        if (!scriptEntry.hasNPC())
            throw new InvalidArgumentsException("This command requires a linked NPC!");

    }

    @Override
    public void execute(ScriptEntry scriptEntry) throws CommandExecutionException {

        if (scriptEntry.getNPC().getEntityType() != EntityType.PLAYER
                && scriptEntry.getNPC().getEntityType() != EntityType.OCELOT
                && scriptEntry.getNPC().getEntityType() != EntityType.WOLF) {
            dB.echoError(scriptEntry.getResidingQueue(), "...only Player, ocelot, or wolf type NPCs can sit!");
            return;
        }

        dB.report(scriptEntry, getName(), aH.debugObj("npc", scriptEntry.getNPC()));

        if (scriptEntry.getNPC().getEntityType() == EntityType.OCELOT) {
            ((Ocelot)scriptEntry.getNPC().getEntity()).setSitting(false);
        }

        else if (scriptEntry.getNPC().getEntityType() == EntityType.WOLF) {
            ((Wolf)scriptEntry.getNPC().getEntity()).setSitting(false);
        }

        else {
            NPC npc = scriptEntry.getNPC().getCitizen();
            SittingTrait trait = npc.getTrait(SittingTrait.class);

            if (!npc.hasTrait(SittingTrait.class)){
                npc.addTrait(SittingTrait.class);
                dB.echoDebug(scriptEntry, "...added sitting trait");
            }

            if (!trait.isSitting()) {
                dB.echoError(scriptEntry.getResidingQueue(), "...NPC is already standing, removing trait");
                npc.removeTrait(SittingTrait.class);
                return;
            }
            trait.stand();
            npc.removeTrait(SittingTrait.class);
        }
    }
}

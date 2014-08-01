package net.aufdemrand.denizen.scripts.commands.npc;

import net.aufdemrand.denizen.objects.dLocation;
import org.bukkit.entity.EntityType;

import net.aufdemrand.denizen.exceptions.CommandExecutionException;
import net.aufdemrand.denizen.exceptions.InvalidArgumentsException;
import net.aufdemrand.denizen.npc.traits.SittingTrait;
import net.aufdemrand.denizen.scripts.ScriptEntry;
import net.aufdemrand.denizen.scripts.commands.AbstractCommand;
import net.aufdemrand.denizen.objects.aH;
import net.aufdemrand.denizen.utilities.debugging.dB;
import org.bukkit.entity.Ocelot;
import org.bukkit.entity.Wolf;


public class SitCommand extends AbstractCommand {

    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {

        for (aH.Argument arg : aH.interpret(scriptEntry.getArguments())) {
            if (arg.matchesArgumentType(dLocation.class)
                    && !scriptEntry.hasObject("location")) {
                scriptEntry.addObject("location", arg.asType(dLocation.class));
            }
            else
                arg.reportUnhandled();
        }
        if (!scriptEntry.hasNPC())
            throw new InvalidArgumentsException("This command requires a linked NPC!");

    }

    @Override
    public void execute(ScriptEntry scriptEntry) throws CommandExecutionException {
        dLocation location = (dLocation) scriptEntry.getObject("location");
        if (scriptEntry.getNPC().getEntityType() != EntityType.PLAYER
                && scriptEntry.getNPC().getEntityType() != EntityType.OCELOT
                && scriptEntry.getNPC().getEntityType() != EntityType.WOLF) {
            dB.echoError(scriptEntry.getResidingQueue(), "...only Player, ocelot, or wolf type NPCs can sit!");
            return;
        }

        if (scriptEntry.getNPC().getEntityType() == EntityType.OCELOT) {
            ((Ocelot)scriptEntry.getNPC().getEntity()).setSitting(true);
        }

        else if (scriptEntry.getNPC().getEntityType() == EntityType.WOLF) {
            ((Wolf)scriptEntry.getNPC().getEntity()).setSitting(true);
        }

        else {
            SittingTrait trait = scriptEntry.getNPC().getCitizen().getTrait(SittingTrait.class);
            if (!scriptEntry.getNPC().getCitizen().hasTrait(SittingTrait.class)) {
                scriptEntry.getNPC().getCitizen().addTrait(SittingTrait.class);
                dB.echoDebug(scriptEntry, "...added sitting trait");
            }

            if (trait.isSitting()) {
                dB.echoError(scriptEntry.getResidingQueue(), "...NPC is already sitting");
                return;
            }

            if (location != null) {
                trait.sit(location);
            } else {
                trait.sit();
            }
        }
    }
}

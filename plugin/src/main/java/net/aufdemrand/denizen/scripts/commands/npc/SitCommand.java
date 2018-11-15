package net.aufdemrand.denizen.scripts.commands.npc;

import net.aufdemrand.denizen.BukkitScriptEntryData;
import net.aufdemrand.denizen.npc.traits.SittingTrait;
import net.aufdemrand.denizen.objects.dLocation;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.aufdemrand.denizencore.exceptions.CommandExecutionException;
import net.aufdemrand.denizencore.exceptions.InvalidArgumentsException;
import net.aufdemrand.denizencore.objects.aH;
import net.aufdemrand.denizencore.scripts.ScriptEntry;
import net.aufdemrand.denizencore.scripts.commands.AbstractCommand;
import org.bukkit.entity.EntityType;
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
            else {
                arg.reportUnhandled();
            }
        }
        if (!((BukkitScriptEntryData) scriptEntry.entryData).hasNPC()) {
            throw new InvalidArgumentsException("This command requires a linked NPC!");
        }

    }

    @Override
    public void execute(ScriptEntry scriptEntry) throws CommandExecutionException {
        dLocation location = (dLocation) scriptEntry.getObject("location");
        if (((BukkitScriptEntryData) scriptEntry.entryData).getNPC().getEntityType() != EntityType.PLAYER
                && ((BukkitScriptEntryData) scriptEntry.entryData).getNPC().getEntityType() != EntityType.OCELOT
                && ((BukkitScriptEntryData) scriptEntry.entryData).getNPC().getEntityType() != EntityType.WOLF) {
            dB.echoError(scriptEntry.getResidingQueue(), "...only Player, ocelot, or wolf type NPCs can sit!");
            return;
        }

        if (scriptEntry.dbCallShouldDebug()) {

            dB.report(scriptEntry, getName(), aH.debugObj("npc", ((BukkitScriptEntryData) scriptEntry.entryData).getNPC())
                    + (location != null ? location.debug() : ""));

        }

        if (((BukkitScriptEntryData) scriptEntry.entryData).getNPC().getEntityType() == EntityType.OCELOT) {
            ((Ocelot) ((BukkitScriptEntryData) scriptEntry.entryData).getNPC().getEntity()).setSitting(true);
        }
        else if (((BukkitScriptEntryData) scriptEntry.entryData).getNPC().getEntityType() == EntityType.WOLF) {
            ((Wolf) ((BukkitScriptEntryData) scriptEntry.entryData).getNPC().getEntity()).setSitting(true);
        }
        else {
            SittingTrait trait = ((BukkitScriptEntryData) scriptEntry.entryData).getNPC().getCitizen().getTrait(SittingTrait.class);
            if (!((BukkitScriptEntryData) scriptEntry.entryData).getNPC().getCitizen().hasTrait(SittingTrait.class)) {
                ((BukkitScriptEntryData) scriptEntry.entryData).getNPC().getCitizen().addTrait(SittingTrait.class);
                dB.echoDebug(scriptEntry, "...added sitting trait");
            }

            if (location != null) {
                trait.sit(location);
            }
            else {
                trait.sit();
            }
        }
    }
}

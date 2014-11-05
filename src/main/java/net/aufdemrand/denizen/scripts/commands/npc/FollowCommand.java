package net.aufdemrand.denizen.scripts.commands.npc;

import net.aufdemrand.denizen.BukkitScriptEntryData;
import net.aufdemrand.denizencore.exceptions.CommandExecutionException;
import net.aufdemrand.denizencore.exceptions.InvalidArgumentsException;
import net.aufdemrand.denizen.objects.Element;
import net.aufdemrand.denizen.objects.dEntity;
import net.aufdemrand.denizen.scripts.ScriptEntry;
import net.aufdemrand.denizen.scripts.commands.AbstractCommand;
import net.aufdemrand.denizen.objects.aH;
import net.aufdemrand.denizen.utilities.debugging.dB;

/**
 * Instructs the NPC to follow a player.
 *
 * @author aufdemrand
 *
 */
public class FollowCommand extends AbstractCommand {

    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {

        // Parse Arguments
        for (aH.Argument arg : aH.interpret(scriptEntry.getArguments())) {
            if (!scriptEntry.hasObject("stop") &&
                    arg.matches("STOP"))
                scriptEntry.addObject("stop", true);

            else if (!scriptEntry.hasObject("lead") &&
                    arg.matchesPrimitive(aH.PrimitiveType.Double))
                scriptEntry.addObject("lead", arg.asElement());

            else if (!scriptEntry.hasObject("target") &&
                    arg.matchesArgumentType(dEntity.class))
                scriptEntry.addObject("target", arg.asType(dEntity.class));

            else
                arg.reportUnhandled();
        }
        if (!scriptEntry.hasObject("target")) {
            if (((BukkitScriptEntryData)scriptEntry.entryData).hasPlayer())
                scriptEntry.addObject("target", ((BukkitScriptEntryData)scriptEntry.entryData).getPlayer().getDenizenEntity());
            else
                throw new InvalidArgumentsException("This command requires a linked player!");
        }
        if (!((BukkitScriptEntryData)scriptEntry.entryData).hasNPC())
            throw new InvalidArgumentsException("This command requires a linked NPC!");
    }

    @Override
    public void execute(ScriptEntry scriptEntry) throws CommandExecutionException {
        // Get objects
        Boolean stop = (Boolean) scriptEntry.getObject("stop");
        Element lead = (Element) scriptEntry.getObject("lead");
        dEntity target = (dEntity) scriptEntry.getObject("target");

        // Report to dB
        dB.report(scriptEntry, getName(),
                        (((BukkitScriptEntryData)scriptEntry.entryData).getPlayer() != null ? ((BukkitScriptEntryData)scriptEntry.entryData).getPlayer().debug() : "")
                        + (stop == null ? aH.debugObj("Action", "FOLLOW")
                        : aH.debugObj("Action", "STOP"))
                        + (lead != null ? aH.debugObj("Lead", lead.toString()) : "")
                        + target.debug());

        if (lead != null)
            ((BukkitScriptEntryData)scriptEntry.entryData).getNPC().getNavigator().getLocalParameters().distanceMargin(lead.asDouble());

        if (stop != null)
            ((BukkitScriptEntryData)scriptEntry.entryData).getNPC().getNavigator()
                    .cancelNavigation();
        else
            ((BukkitScriptEntryData)scriptEntry.entryData).getNPC().getNavigator()
                .setTarget(target.getBukkitEntity(), false);

    }
}

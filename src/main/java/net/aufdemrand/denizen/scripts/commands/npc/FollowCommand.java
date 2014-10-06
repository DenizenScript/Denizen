package net.aufdemrand.denizen.scripts.commands.npc;

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
            if (scriptEntry.hasPlayer())
                scriptEntry.addObject("target", scriptEntry.getPlayer().getDenizenEntity());
            else
                throw new InvalidArgumentsException("This command requires a linked player!");
        }
        if (!scriptEntry.hasNPC())
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
                        (scriptEntry.getPlayer() != null ? scriptEntry.getPlayer().debug() : "")
                        + (stop == null ? aH.debugObj("Action", "FOLLOW")
                        : aH.debugObj("Action", "STOP"))
                        + (lead != null ? aH.debugObj("Lead", lead.toString()) : "")
                        + target.debug());

        if (lead != null)
            scriptEntry.getNPC().getNavigator().getLocalParameters().distanceMargin(lead.asDouble());

        if (stop != null)
            scriptEntry.getNPC().getNavigator()
                    .cancelNavigation();
        else
            scriptEntry.getNPC().getNavigator()
                .setTarget(target.getBukkitEntity(), false);

    }
}

package net.aufdemrand.denizen.scripts.commands.npc;

import net.aufdemrand.denizen.exceptions.CommandExecutionException;
import net.aufdemrand.denizen.exceptions.InvalidArgumentsException;
import net.aufdemrand.denizen.objects.Element;
import net.aufdemrand.denizen.objects.dNPC;
import net.aufdemrand.denizen.scripts.ScriptEntry;
import net.aufdemrand.denizen.scripts.commands.AbstractCommand;
import net.aufdemrand.denizen.objects.aH;
import net.aufdemrand.denizen.objects.aH.ArgumentType;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.trait.LookClose;

/**
 * Configures the LookClose Trait for a NPC.
 *
 * @author Jeremy Schroeder
 */

public class LookcloseCommand extends AbstractCommand {



    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {

        // Parse Arguments
        for (aH.Argument arg : aH.interpret(scriptEntry.getArguments())) {

            if (arg.matches("realistic, realisctically"))
                scriptEntry.addObject("realistic", Element.TRUE);

            else if (arg.matchesPrimitive(aH.PrimitiveType.Integer))
                scriptEntry.addObject("range", arg.asElement());

            else if (arg.matchesPrimitive(aH.PrimitiveType.Boolean))
                scriptEntry.addObject("toggle", arg.asElement());

            else if (arg.matchesArgumentType(dNPC.class))
                scriptEntry.setNPC((dNPC) arg.asType(dNPC.class));

            else arg.reportUnhandled();
        }

        // Only required thing is a valid NPC. This may be an already linked
        // NPC, or one specified by arguments
        if (scriptEntry.getNPC() == null)
            throw new InvalidArgumentsException("This command requires a NPC!");

    }

    @Override
    public void execute(ScriptEntry scriptEntry) throws CommandExecutionException {

        dB.report(scriptEntry, getName(), scriptEntry.getNPC().debug()
                + scriptEntry.reportObject("realistic")
                + scriptEntry.reportObject("range")
                + scriptEntry.reportObject("toggle"));

        // Get the instance of the trait that belongs to the target NPC
        LookClose trait = scriptEntry.getNPC().getCitizen().getTrait(LookClose.class);

        // Handle toggle
        if (scriptEntry.hasObject("toggle"))
            trait.lookClose(scriptEntry.getElement("toggle").asBoolean());

        // Handle realistic
        if (scriptEntry.hasObject("realistic"))
            trait.setRealisticLooking(true);
        else trait.setRealisticLooking(false);

        // Handle range
        if (scriptEntry.hasObject("range")) {
            trait.setRange(scriptEntry.getElement("range").asInt());
        }

    }

}

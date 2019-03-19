package net.aufdemrand.denizen.scripts.commands.npc;

import net.aufdemrand.denizen.BukkitScriptEntryData;
import net.aufdemrand.denizen.objects.dNPC;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.aufdemrand.denizencore.exceptions.InvalidArgumentsException;
import net.aufdemrand.denizencore.objects.Element;
import net.aufdemrand.denizencore.objects.aH;
import net.aufdemrand.denizencore.scripts.ScriptEntry;
import net.aufdemrand.denizencore.scripts.commands.AbstractCommand;
import net.citizensnpcs.trait.LookClose;

public class LookcloseCommand extends AbstractCommand {

    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {

        // Parse Arguments
        for (aH.Argument arg : aH.interpret(scriptEntry.getArguments())) {

            if (arg.matches("realistic", "realistically")) {
                scriptEntry.addObject("realistic", new Element(true));
            }
            else if (arg.matchesPrimitive(aH.PrimitiveType.Integer)) {
                scriptEntry.addObject("range", arg.asElement());
            }
            else if (arg.matchesPrimitive(aH.PrimitiveType.Boolean)) {
                scriptEntry.addObject("toggle", arg.asElement());
            }
            else if (arg.matchesArgumentType(dNPC.class)) // TODO: better way of handling this?
            {
                ((BukkitScriptEntryData) scriptEntry.entryData).setNPC(arg.asType(dNPC.class));
            }
            else {
                arg.reportUnhandled();
            }
        }

        // Only required thing is a valid NPC. This may be an already linked
        // NPC, or one specified by arguments
        if (((BukkitScriptEntryData) scriptEntry.entryData).getNPC() == null) {
            throw new InvalidArgumentsException("NPC linked was missing or invalid.");
        }

    }

    @Override
    public void execute(ScriptEntry scriptEntry) {

        if (scriptEntry.dbCallShouldDebug()) {

            dB.report(scriptEntry, getName(), ((BukkitScriptEntryData) scriptEntry.entryData).getNPC().debug()
                    + aH.debugObj("realistic", scriptEntry.getObject("realistic"))
                    + aH.debugObj("range", scriptEntry.getObject("range"))
                    + aH.debugObj("toggle", scriptEntry.getObject("toggle")));

        }

        // Get the instance of the trait that belongs to the target NPC
        LookClose trait = ((BukkitScriptEntryData) scriptEntry.entryData).getNPC().getCitizen().getTrait(LookClose.class);

        // Handle toggle
        if (scriptEntry.hasObject("toggle")) {
            trait.lookClose(scriptEntry.getElement("toggle").asBoolean());
        }

        // Handle realistic
        if (scriptEntry.hasObject("realistic")) {
            trait.setRealisticLooking(true);
        }
        else {
            trait.setRealisticLooking(false);
        }

        // Handle range
        if (scriptEntry.hasObject("range")) {
            trait.setRange(scriptEntry.getElement("range").asInt());
        }

    }
}

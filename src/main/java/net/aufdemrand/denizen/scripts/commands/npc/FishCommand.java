package net.aufdemrand.denizen.scripts.commands.npc;

import net.aufdemrand.denizen.exceptions.CommandExecutionException;
import net.aufdemrand.denizen.exceptions.InvalidArgumentsException;
import net.aufdemrand.denizen.npc.traits.FishingTrait;
import net.aufdemrand.denizen.objects.Element;
import net.aufdemrand.denizen.objects.aH;
import net.aufdemrand.denizen.objects.dLocation;
import net.aufdemrand.denizen.objects.dNPC;
import net.aufdemrand.denizen.scripts.ScriptEntry;
import net.aufdemrand.denizen.scripts.commands.AbstractCommand;
import net.aufdemrand.denizen.utilities.debugging.dB;

public class FishCommand extends AbstractCommand {

    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {

        for (aH.Argument arg : aH.interpret(scriptEntry.getArguments())) {

            if (!scriptEntry.hasObject("location")
                    && arg.matchesArgumentType(dLocation.class))
                scriptEntry.addObject("location", arg.asType(dLocation.class));

            else if (!scriptEntry.hasObject("catch")
                    && arg.matchesPrefix("catch")
                    && arg.matchesEnum(FishingTrait.CatchType.values()))
                scriptEntry.addObject("catch", arg.asElement());

            else if (!scriptEntry.hasObject("stop")
                    && arg.matches("stop"))
                scriptEntry.addObject("stop", Element.TRUE);

            else if (!scriptEntry.hasObject("percent")
                    && arg.matchesPrefix("catchpercent", "percent", "chance", "c")
                    && arg.matchesPrimitive(aH.PrimitiveType.Integer))
                scriptEntry.addObject("percent", arg.asElement());

        }

        if (!scriptEntry.hasObject("location"))
            throw new InvalidArgumentsException("Must specify a valid location!");

        scriptEntry.defaultObject("catch", new Element("NONE"))
                .defaultObject("stop", Element.FALSE)
                .defaultObject("percent", new Element(65));

        if (!scriptEntry.hasNPC() || !scriptEntry.getNPC().isSpawned())
            throw new InvalidArgumentsException("This command requires a linked and spawned NPC!");

    }

    @Override
    public void execute(ScriptEntry scriptEntry) throws CommandExecutionException {

        dLocation location = scriptEntry.getdObjectAs("location", dLocation.class);
        Element katch = scriptEntry.getElement("catch");
        Element stop = scriptEntry.getElement("stop");
        Element percent = scriptEntry.getElement("percent");

        FishingTrait trait = scriptEntry.getNPC().getFishingTrait();

        dB.report(scriptEntry, getName(), location.debug() + katch.debug() + percent.debug() + stop.debug());

        if (stop.asBoolean()) {
            trait.stopFishing();
            return;
        }

        trait.setCatchPercent(percent.asInt());
        trait.setCatchType(FishingTrait.CatchType.valueOf(katch.asString().toUpperCase()));
        trait.startFishing(location);

    }
}

package net.aufdemrand.denizen.scripts.commands.npc;

import net.aufdemrand.denizen.BukkitScriptEntryData;
import net.aufdemrand.denizen.nms.interfaces.FishingHelper;
import net.aufdemrand.denizen.npc.traits.FishingTrait;
import net.aufdemrand.denizen.objects.dLocation;
import net.aufdemrand.denizen.objects.dNPC;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.aufdemrand.denizencore.exceptions.CommandExecutionException;
import net.aufdemrand.denizencore.exceptions.InvalidArgumentsException;
import net.aufdemrand.denizencore.objects.Element;
import net.aufdemrand.denizencore.objects.aH;
import net.aufdemrand.denizencore.scripts.ScriptEntry;
import net.aufdemrand.denizencore.scripts.commands.AbstractCommand;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class FishCommand extends AbstractCommand {

    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {

        for (aH.Argument arg : aH.interpret(scriptEntry.getArguments())) {

            if (!scriptEntry.hasObject("location")
                    && arg.matchesArgumentType(dLocation.class)) {
                scriptEntry.addObject("location", arg.asType(dLocation.class));
            }
            else if (!scriptEntry.hasObject("catch")
                    && arg.matchesPrefix("catch")
                    && arg.matchesEnum(FishingHelper.CatchType.values())) {
                scriptEntry.addObject("catch", arg.asElement());
            }
            else if (!scriptEntry.hasObject("stop")
                    && arg.matches("stop")) {
                scriptEntry.addObject("stop", Element.TRUE);
            }
            else if (!scriptEntry.hasObject("percent")
                    && arg.matchesPrefix("catchpercent", "percent", "chance", "c")
                    && arg.matchesPrimitive(aH.PrimitiveType.Integer)) {
                scriptEntry.addObject("percent", arg.asElement());
            }

        }

        if (!scriptEntry.hasObject("location") && !scriptEntry.hasObject("stop")) {
            throw new InvalidArgumentsException("Must specify a valid location!");
        }

        scriptEntry.defaultObject("catch", new Element("NONE"))
                .defaultObject("stop", Element.FALSE)
                .defaultObject("percent", new Element(65));

        if (!((BukkitScriptEntryData) scriptEntry.entryData).hasNPC() || !((BukkitScriptEntryData) scriptEntry.entryData).getNPC().isSpawned()) {
            throw new InvalidArgumentsException("This command requires a linked and spawned NPC!");
        }

    }

    @Override
    public void execute(ScriptEntry scriptEntry) throws CommandExecutionException {

        dLocation location = scriptEntry.getdObject("location");
        Element catchtype = scriptEntry.getElement("catch");
        Element stop = scriptEntry.getElement("stop");
        Element percent = scriptEntry.getElement("percent");

        dNPC npc = ((BukkitScriptEntryData) scriptEntry.entryData).getNPC();
        FishingTrait trait = npc.getFishingTrait();

        if (scriptEntry.dbCallShouldDebug()) {

            dB.report(scriptEntry, getName(), location.debug() + catchtype.debug() + percent.debug() + stop.debug());

        }

        if (stop.asBoolean()) {
            trait.stopFishing();
            return;
        }

        npc.getEquipmentTrait().set(0, new ItemStack(Material.FISHING_ROD));

        trait.setCatchPercent(percent.asInt());
        trait.setCatchType(FishingHelper.CatchType.valueOf(catchtype.asString().toUpperCase()));
        trait.startFishing(location);

    }
}

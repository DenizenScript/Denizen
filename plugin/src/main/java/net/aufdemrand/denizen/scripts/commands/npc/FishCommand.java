package net.aufdemrand.denizen.scripts.commands.npc;

import net.aufdemrand.denizen.BukkitScriptEntryData;
import net.aufdemrand.denizen.nms.interfaces.FishingHelper;
import net.aufdemrand.denizen.npc.traits.FishingTrait;
import net.aufdemrand.denizen.objects.dLocation;
import net.aufdemrand.denizen.objects.dNPC;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.aufdemrand.denizencore.exceptions.InvalidArgumentsException;
import net.aufdemrand.denizencore.objects.Element;
import net.aufdemrand.denizencore.objects.aH;
import net.aufdemrand.denizencore.scripts.ScriptEntry;
import net.aufdemrand.denizencore.scripts.commands.AbstractCommand;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class FishCommand extends AbstractCommand {

    // <--[command]
    // @Name Fish
    // @Syntax fish [<location>] (catch:{none}/default/junk/treasure/fish) (stop) (chance:<#>)
    // @Required 1
    // @Short Causes an NPC to begin fishing around a specified location.
    // @Group npc
    //
    // @Description
    // Causes an NPC to begin fishing at the specified location.
    // Setting catch determines what items the NPC may fish up, and
    // the chance is the odds of the NPC fishing up an item.
    //
    // Also note that it seems you must specify the same location initially chosen for the NPC to fish at
    // when stopping it.
    //
    // @Tags
    // None
    //
    // @Usage
    // Makes the NPC throw their fishing line out to where the player is looking, with a 50% chance of catching fish
    // - fish <player.location.cursor_on> catch:fish chance:50
    //
    // @Usage
    // Makes the NPC stop fishing
    // - fish <player.location.cursor_on> stop
    // -->

    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {

        for (aH.Argument arg : aH.interpretArguments(scriptEntry.aHArgs)) {

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
                scriptEntry.addObject("stop", new Element(true));
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
                .defaultObject("stop", new Element(false))
                .defaultObject("percent", new Element(65));

        if (!((BukkitScriptEntryData) scriptEntry.entryData).hasNPC() || !((BukkitScriptEntryData) scriptEntry.entryData).getNPC().isSpawned()) {
            throw new InvalidArgumentsException("This command requires a linked and spawned NPC!");
        }

    }

    @Override
    public void execute(ScriptEntry scriptEntry) {

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

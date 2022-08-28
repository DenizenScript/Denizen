package com.denizenscript.denizen.scripts.commands.npc;

import com.denizenscript.denizen.utilities.Utilities;
import com.denizenscript.denizencore.utilities.debugging.Debug;
import com.denizenscript.denizen.nms.interfaces.FishingHelper;
import com.denizenscript.denizen.npc.traits.FishingTrait;
import com.denizenscript.denizen.objects.LocationTag;
import com.denizenscript.denizen.objects.NPCTag;
import com.denizenscript.denizencore.exceptions.InvalidArgumentsException;
import com.denizenscript.denizencore.objects.Argument;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.scripts.ScriptEntry;
import com.denizenscript.denizencore.scripts.commands.AbstractCommand;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class FishCommand extends AbstractCommand {

    public FishCommand() {
        setName("fish");
        setSyntax("fish [<location>] (catch:{none}/default/junk/treasure/fish) (stop) (chance:<#>)");
        setRequiredArguments(1, 4);
        isProcedural = false;
    }

    // <--[command]
    // @Name Fish
    // @Syntax fish [<location>/stop] (catch:{none}/default/junk/treasure/fish) (chance:<#>)
    // @Required 1
    // @Maximum 4
    // @Plugin Citizens
    // @Short Causes an NPC to begin fishing around a specified location.
    // @Group npc
    //
    // @Description
    // Causes an NPC to begin fishing at the specified location.
    // Setting catch determines what items the NPC may fish up, and the chance is the odds of the NPC fishing up an item.
    //
    // @Tags
    // None
    //
    // @Usage
    // Makes the NPC throw their fishing line out to where the player is looking, with a 50% chance of catching fish.
    // - fish <player.cursor_on> catch:fish chance:50
    //
    // @Usage
    // Makes the NPC stop fishing.
    // - fish stop
    // -->

    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {
        for (Argument arg : scriptEntry) {
            if (!scriptEntry.hasObject("location")
                    && arg.matchesArgumentType(LocationTag.class)) {
                scriptEntry.addObject("location", arg.asType(LocationTag.class));
            }
            else if (!scriptEntry.hasObject("catch")
                    && arg.matchesPrefix("catch")
                    && arg.matchesEnum(FishingHelper.CatchType.class)) {
                scriptEntry.addObject("catch", arg.asElement());
            }
            else if (!scriptEntry.hasObject("stop")
                    && arg.matches("stop")) {
                scriptEntry.addObject("stop", new ElementTag(true));
            }
            else if (!scriptEntry.hasObject("percent")
                    && arg.matchesPrefix("catchpercent", "percent", "chance", "c")
                    && arg.matchesInteger()) {
                scriptEntry.addObject("percent", arg.asElement());
            }
        }
        if (!scriptEntry.hasObject("location") && !scriptEntry.hasObject("stop")) {
            throw new InvalidArgumentsException("Must specify a valid location!");
        }
        scriptEntry.defaultObject("catch", new ElementTag("NONE"))
                .defaultObject("stop", new ElementTag(false))
                .defaultObject("percent", new ElementTag(65));
        if (!Utilities.entryHasNPC(scriptEntry) || !Utilities.getEntryNPC(scriptEntry).isSpawned()) {
            throw new InvalidArgumentsException("This command requires a linked and spawned NPC!");
        }
    }

    @Override
    public void execute(ScriptEntry scriptEntry) {
        LocationTag location = scriptEntry.getObjectTag("location");
        ElementTag catchtype = scriptEntry.getElement("catch");
        ElementTag stop = scriptEntry.getElement("stop");
        ElementTag percent = scriptEntry.getElement("percent");
        if (scriptEntry.dbCallShouldDebug()) {
            Debug.report(scriptEntry, getName(), location, catchtype, percent, stop);
        }
        NPCTag npc = Utilities.getEntryNPC(scriptEntry);
        FishingTrait trait = npc.getFishingTrait();
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

package com.denizenscript.denizen.scripts.commands.world;

import com.denizenscript.denizen.objects.LocationTag;
import com.denizenscript.denizen.objects.MaterialTag;
import com.denizenscript.denizen.utilities.blocks.ModernBlockData;
import com.denizenscript.denizencore.exceptions.InvalidArgumentsException;
import com.denizenscript.denizencore.objects.*;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.core.ListTag;
import com.denizenscript.denizencore.scripts.ScriptEntry;
import com.denizenscript.denizencore.scripts.commands.AbstractCommand;
import com.denizenscript.denizencore.utilities.debugging.Debug;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;

import java.util.List;


public class AdjustBlockCommand extends AbstractCommand {

    // <--[command]
    // @Name AdjustBlock
    // @Syntax adjustblock [<location>|...] [<mechanism>](:<value>)
    // @Required 2
    // @Short Adjusts a mechanism on the material of a block at the location.
    // @Group core
    // @Guide https://guide.denizenscript.com/guides/basics/mechanisms.html
    //
    // @Description
    // Adjusts a mechanism on the material of a block at the location.
    // That is, an equivalent to <@link command adjust>, but that directly applies a "MaterialTag" mechanism onto a block.
    //
    // Input a location or list of locations, and the mechanism to apply.
    // @Tags
    // <LocationTag.material>
    //
    // @Usage
    // Use to put snow on the block at the player's feet.
    // - adjust <player.location.below> snowy:true
    //
    // -->

    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {

        for (Argument arg : scriptEntry.getProcessedArgs()) {
            if (!scriptEntry.hasObject("locations")
                    && arg.matchesArgumentList(LocationTag.class)) {
                scriptEntry.addObject("locations", arg.asType(ListTag.class).filter(LocationTag.class, scriptEntry));
            }
            else if (!scriptEntry.hasObject("mechanism")) {
                if (arg.hasPrefix()) {
                    scriptEntry.addObject("mechanism", new ElementTag(arg.getPrefix().getValue()));
                    scriptEntry.addObject("mechanism_value", arg.asElement());
                }
                else {
                    scriptEntry.addObject("mechanism", arg.asElement());
                }
            }
            else {
                arg.reportUnhandled();
            }
        }
        if (!scriptEntry.hasObject("locations")) {
            throw new InvalidArgumentsException("You must specify a location!");
        }
        if (!scriptEntry.hasObject("mechanism")) {
            throw new InvalidArgumentsException("You must specify a mechanism!");
        }
    }

    @Override
    public void execute(ScriptEntry scriptEntry) {
        ElementTag mechanismName = scriptEntry.getElement("mechanism");
        ElementTag value = scriptEntry.getElement("mechanism_value");
        List<LocationTag> locations = (List<LocationTag>) scriptEntry.getObject("locations");
        if (scriptEntry.dbCallShouldDebug()) {
            Debug.report(scriptEntry, getName(),
                            ArgumentHelper.debugList("locations", locations)
                            + mechanismName.debug()
                            + (value == null ? "" : value.debug()));
        }
        for (LocationTag location : locations) {
            Block block = location.getBlock();
            BlockData data = block.getBlockData();
            MaterialTag specialMaterial = new MaterialTag(new ModernBlockData(data));
            Mechanism mechanism = new Mechanism(mechanismName, value, scriptEntry.entryData.getTagContext());
            specialMaterial.safeAdjust(mechanism);
            block.setBlockData(data);
        }

    }
}

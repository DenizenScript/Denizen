package com.denizenscript.denizen.scripts.commands.world;

import com.denizenscript.denizen.nms.NMSHandler;
import com.denizenscript.denizen.objects.LocationTag;
import com.denizenscript.denizen.objects.MaterialTag;
import com.denizenscript.denizencore.exceptions.InvalidArgumentsException;
import com.denizenscript.denizencore.objects.*;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.core.ListTag;
import com.denizenscript.denizencore.objects.properties.PropertyParser;
import com.denizenscript.denizencore.scripts.ScriptEntry;
import com.denizenscript.denizencore.scripts.commands.AbstractCommand;
import com.denizenscript.denizencore.utilities.debugging.Debug;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;

import java.util.List;

public class AdjustBlockCommand extends AbstractCommand {

    public AdjustBlockCommand() {
        setName("adjustblock");
        setSyntax("adjustblock [<location>|...] [<mechanism>](:<value>) (no_physics)");
        setRequiredArguments(2, 3);
        isProcedural = false;
        allowedDynamicPrefixes = true;
    }

    // <--[command]
    // @Name AdjustBlock
    // @Syntax adjustblock [<location>|...] [<mechanism>](:<value>) (no_physics)
    // @Required 2
    // @Maximum 3
    // @Short Adjusts a mechanism on the material of a block at the location.
    // @Group core
    // @Guide https://guide.denizenscript.com/guides/basics/mechanisms.html
    //
    // @Description
    // Adjusts a mechanism on the material of a block at the location.
    // That is, an equivalent to <@link command adjust>, but that directly applies a "MaterialTag" mechanism onto a block.
    //
    // Input a location or list of locations, and the mechanism to apply.
    //
    // Use the "no_physics" argument to indicate that the change should not apply a physics update.
    // If not specified, physics will apply to the block and nearby blocks.
    //
    // @Tags
    // <LocationTag.material>
    //
    // @Usage
    // Use to put snow on the block at the player's feet.
    // - adjustblock <player.location.below> snowy:true
    //
    // @Usage
    // Use to switch on the lever that the player is looking at, without actually providing redstone power.
    // - adjustblock <player.cursor_on> switched:true no_physics
    //
    // -->

    @Override
    public void addCustomTabCompletions(TabCompletionsBuilder tab) {
        tab.add(PropertyParser.propertiesByClass.get(MaterialTag.class).propertiesByMechanism.keySet());
    }

    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {

        for (Argument arg : scriptEntry) {
            if (!scriptEntry.hasObject("locations")
                    && arg.matchesArgumentList(LocationTag.class)) {
                scriptEntry.addObject("locations", arg.asType(ListTag.class).filter(LocationTag.class, scriptEntry));
            }
            else if (!scriptEntry.hasObject("no_physics")
                && arg.matches("no_physics")) {
                scriptEntry.addObject("no_physics", new ElementTag(true));
            }
            else if (!scriptEntry.hasObject("mechanism")) {
                if (arg.hasPrefix()) {
                    scriptEntry.addObject("mechanism", new ElementTag(arg.getPrefix().getValue()));
                    scriptEntry.addObject("mechanism_value", arg.object);
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
        ObjectTag value = scriptEntry.getObjectTag("mechanism_value");
        ElementTag noPhysics = scriptEntry.getElement("no_physics");
        List<LocationTag> locations = (List<LocationTag>) scriptEntry.getObject("locations");
        if (scriptEntry.dbCallShouldDebug()) {
            Debug.report(scriptEntry, getName(), db("locations", locations), mechanismName, noPhysics, value);
        }
        boolean doPhysics = noPhysics == null || !noPhysics.asBoolean();
        for (LocationTag location : locations) {
            Block block = location.getBlock();
            BlockData data = block.getBlockData();
            MaterialTag specialMaterial = new MaterialTag(data);
            Mechanism mechanism = new Mechanism(mechanismName.asString(), value, scriptEntry.getContext());
            specialMaterial.safeAdjust(mechanism);
            if (doPhysics) {
                block.setBlockData(data, false);
                applyPhysicsAt(location);
            }
            else {
                ModifyBlockCommand.setBlock(block.getLocation(), specialMaterial, false, null);
            }
        }
    }

    public static void applyPhysicsAt(Location location) {
        NMSHandler.blockHelper.applyPhysics(location);
        NMSHandler.blockHelper.applyPhysics(location.clone().add(1, 0, 0));
        NMSHandler.blockHelper.applyPhysics(location.clone().add(-1, 0, 0));
        NMSHandler.blockHelper.applyPhysics(location.clone().add(0, 0, 1));
        NMSHandler.blockHelper.applyPhysics(location.clone().add(0, 0, -1));
        NMSHandler.blockHelper.applyPhysics(location.clone().add(0, 1, 0));
        NMSHandler.blockHelper.applyPhysics(location.clone().add(0, -1, 0));
    }
}

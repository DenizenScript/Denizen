package com.denizenscript.denizen.events.world;

import com.denizenscript.denizen.objects.LocationTag;
import com.denizenscript.denizen.objects.MaterialTag;
import com.denizenscript.denizen.objects.WorldTag;
import com.denizenscript.denizen.events.BukkitScriptEvent;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.core.ListTag;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.utilities.CoreUtilities;
import org.bukkit.block.BlockState;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.StructureGrowEvent;

public class StructureGrowsScriptEvent extends BukkitScriptEvent implements Listener {

    // <--[event]
    // @Events
    // structure grows (naturally/from bonemeal)
    // <structure> grows (naturally/from bonemeal)
    // plant grows (naturally/from bonemeal)
    // <plant> grows (naturally/from bonemeal)
    //
    // @Regex ^on [^\s]+ grows( naturally|from bonemeal)?$
    //
    // @Group World
    //
    // @Switch in:<area> to only process the event if it occurred within a specified area.
    //
    // @Cancellable true
    //
    // @Triggers when a structure (a tree or a mushroom) grows in a world.
    //
    // @Context
    // <context.world> returns the WorldTag the structure grew in.
    // <context.location> returns the LocationTag the structure grew at.
    // <context.structure> returns an ElementTag of the structure's type. Refer to <@link url https://hub.spigotmc.org/javadocs/spigot/org/bukkit/StructureType.html>.
    // <context.blocks> returns a ListTag of all block locations to be modified.
    // <context.new_materials> returns a ListTag of the new block materials, to go with <context.blocks>.
    //
    // -->

    public StructureGrowsScriptEvent() {
        instance = this;
    }

    public static StructureGrowsScriptEvent instance;
    public WorldTag world;
    public LocationTag location;
    public ElementTag structure;
    public StructureGrowEvent event;

    @Override
    public boolean couldMatch(ScriptPath path) {
        if (!path.eventArgLowerAt(1).equals("grows")) {
            return false;
        }
        String block = path.eventArgLowerAt(0);
        MaterialTag mat = MaterialTag.valueOf(block, CoreUtilities.basicContext);
        return block.equals("structure") || block.equals("plant") || (mat != null && mat.isStructure());
    }

    @Override
    public boolean matches(ScriptPath path) {
        String struct = path.eventArgLowerAt(0);
        if (!struct.equals("structure") && !struct.equals("plant") &&
                !struct.equals(CoreUtilities.toLowerCase(structure.asString()))) {
            return false;
        }
        if (path.eventArgLowerAt(2).equals("from") && !event.isFromBonemeal()) {
            return false;
        }
        else if (path.eventArgLowerAt(2).equals("naturally") && event.isFromBonemeal()) {
            return false;
        }
        if (!runInCheck(path, location)) {
            return false;
        }
        return super.matches(path);
    }

    @Override
    public String getName() {
        return "StructureGrow";
    }

    @Override
    public ObjectTag getContext(String name) {
        if (name.equals("world")) {
            return world;
        }
        else if (name.equals("location")) {
            return location;
        }
        else if (name.equals("structure")) {
            return structure;
        }
        else if (name.equals("blocks")) {
            ListTag blocks = new ListTag();
            for (BlockState block : event.getBlocks()) {
                blocks.addObject(new LocationTag(block.getLocation()));
            }
            return blocks;
        }
        else if (name.equals("new_materials")) {
            ListTag new_materials = new ListTag();
            for (BlockState block : event.getBlocks()) {
                new_materials.addObject(new MaterialTag(block));
            }
            return new_materials;
        }
        return super.getContext(name);
    }

    @EventHandler
    public void onStructureGrow(StructureGrowEvent event) {
        world = new WorldTag(event.getWorld());
        location = new LocationTag(event.getLocation());
        structure = new ElementTag(event.getSpecies().name());
        this.event = event;
        fire(event);
    }
}

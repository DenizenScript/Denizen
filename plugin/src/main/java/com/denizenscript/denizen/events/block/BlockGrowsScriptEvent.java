package com.denizenscript.denizen.events.block;

import com.denizenscript.denizen.objects.LocationTag;
import com.denizenscript.denizen.objects.MaterialTag;
import com.denizenscript.denizen.events.BukkitScriptEvent;
import com.denizenscript.denizen.objects.properties.material.MaterialAge;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.utilities.CoreUtilities;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockGrowEvent;

public class BlockGrowsScriptEvent extends BukkitScriptEvent implements Listener {

    // <--[event]
    // @Events
    // <block> grows
    //
    // @Group Block
    //
    // @Location true
    // @Switch from:<age> to only process the event if the material started at a specific age.
    // @Switch to:<age> to only process the event if the material ended at a specific age.
    //
    // @Cancellable true
    //
    // @Triggers when a block grows naturally in the world, EG, when wheat, sugar canes, cacti, watermelons or pumpkins grow.
    // @Context
    // <context.location> returns the LocationTag of the block that grew (still at original material state when event fires).
    // <context.material> returns the MaterialTag of the block's newly grown state.
    //
    // -->

    public BlockGrowsScriptEvent() {
        registerCouldMatcher("<block> grows");
        registerSwitches("from", "to");
    }

    public LocationTag location;
    public MaterialTag material;
    public BlockGrowEvent event;

    @Override
    public boolean couldMatch(ScriptPath path) {
        if (!super.couldMatch(path)) {
            return false;
        }
        MaterialTag mat = MaterialTag.valueOf(path.eventArgLowerAt(0), CoreUtilities.noDebugContext);
        return mat == null || !mat.isStructure();
    }

    @Override
    public boolean matches(ScriptPath path) {
        if (!runInCheck(path, location)) {
            return false;
        }
        if (!path.tryArgObject(0, material)) {
            return false;
        }
        if (path.switches.containsKey("from")) {
            if (!MaterialAge.describes(new MaterialTag(location.getBlockState()))) {
                return false;
            }
            int oldState = new MaterialAge(new MaterialTag(location.getBlockState())).getCurrent();
            if (!path.checkSwitch("from", String.valueOf(oldState))) {
                return false;
            }
        }
        if (path.switches.containsKey("to")) {
            if (!MaterialAge.describes(material)) {
                return false;
            }
            int newState = new MaterialAge(material).getCurrent();
            if (!path.checkSwitch("to", String.valueOf(newState))) {
                return false;
            }
        }
        return super.matches(path);
    }

    @Override
    public ObjectTag getContext(String name) {
        switch (name) {
            case "location": return location;
            case "material": return material;
        }
        return super.getContext(name);
    }

    @EventHandler
    public void onBlockGrows(BlockGrowEvent event) {
        location = new LocationTag(event.getBlock().getLocation());
        material = new MaterialTag(event.getNewState());
        if (material.isStructure()) {
            return;
        }
        this.event = event;
        fire(event);
    }
}

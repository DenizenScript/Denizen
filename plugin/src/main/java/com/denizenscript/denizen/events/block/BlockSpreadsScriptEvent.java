package com.denizenscript.denizen.events.block;

import com.denizenscript.denizen.objects.LocationTag;
import com.denizenscript.denizen.objects.MaterialTag;
import com.denizenscript.denizen.events.BukkitScriptEvent;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.utilities.Deprecations;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockSpreadEvent;

public class BlockSpreadsScriptEvent extends BukkitScriptEvent implements Listener {

    // <--[event]
    // @Events
    // block spreads
    //
    // @Switch type:<block> to only run if the block spreading matches the material input.
    //
    // @Group Block
    //
    // @Location true
    //
    // @Cancellable true
    //
    // @Triggers when a block spreads based on world conditions, EG, when fire spreads, or when mushrooms spread, or when vines grow.
    //
    // @Context
    // <context.source_location> returns the LocationTag of the block that spread.
    // <context.location> returns the LocationTag of the new block.
    // <context.material> returns the MaterialTag of the block that spread.
    //
    // -->

    public BlockSpreadsScriptEvent() {
        instance = this;
        registerCouldMatcher("block spreads");
        registerCouldMatcher("<block> spreads"); // NOTE: exists for historical compat reasons.
        registerSwitches("type");
    }

    public static BlockSpreadsScriptEvent instance;
    public LocationTag location;
    public MaterialTag material;
    public BlockSpreadEvent event;

    @Override
    public boolean couldMatch(ScriptPath path) {
        if (!super.couldMatch(path)) {
            return false;
        }
        if (path.eventArgLowerAt(0).equals("liquid")) {
            return false;
        }
        if (!path.eventArgLowerAt(0).equals("block")) {
            Deprecations.blockSpreads.warn(getTagContext(path));
        }
        return true;
    }

    @Override
    public boolean matches(ScriptPath path) {
        if (!runInCheck(path, location)) {
            return false;
        }
        if (!material.tryAdvancedMatcher(path.eventArgLowerAt(0))) {
            return false;
        }
        if (path.switches.containsKey("type") && !material.tryAdvancedMatcher(path.switches.get("type"))) {
            return false;
        }
        return super.matches(path);

    }

    @Override
    public String getName() {
        return "BlockSpreads";
    }

    @Override
    public ObjectTag getContext(String name) {
        switch (name) {
            case "location": return location;
            case "material": return material;
            case "source_location": return new LocationTag(event.getBlock().getLocation());
        }
        return super.getContext(name);
    }

    @EventHandler
    public void onBlockSpreads(BlockSpreadEvent event) {
        location = new LocationTag(event.getBlock().getLocation());
        material = new MaterialTag(event.getSource());
        this.event = event;
        fire(event);
    }
}

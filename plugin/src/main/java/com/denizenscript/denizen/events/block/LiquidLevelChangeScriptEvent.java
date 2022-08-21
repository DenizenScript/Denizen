package com.denizenscript.denizen.events.block;

import com.denizenscript.denizen.events.BukkitScriptEvent;
import com.denizenscript.denizen.objects.LocationTag;
import com.denizenscript.denizen.objects.MaterialTag;
import com.denizenscript.denizencore.objects.ObjectTag;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.FluidLevelChangeEvent;

public class LiquidLevelChangeScriptEvent extends BukkitScriptEvent implements Listener {

    // <--[event]
    // @Events
    // liquid|lava|water level changes
    //
    // @Group Block
    //
    // @Location true
    //
    // @Cancellable true
    //
    // @Triggers when a liquid block's level changes. Note that 'liquid spreads' is fired when a liquid first spreads, and 'level changes' is usually fired when it goes down.
    //
    // @Context
    // <context.location> returns the LocationTag the liquid block that has its level changing.
    // <context.old_material> returns the original MaterialTag data.
    // <context.new_material> returns the new MaterialTag data. Sometimes can be a different material (such as air).
    //
    // -->

    public LiquidLevelChangeScriptEvent() {
        registerCouldMatcher("liquid|lava|water level changes");
    }

    public LocationTag location;
    public MaterialTag old_material;
    public FluidLevelChangeEvent event;

    @Override
    public boolean couldMatch(ScriptPath path) {
        if (!super.couldMatch(path)) {
            return false;
        }
        if (path.eventLower.startsWith("block")) {
            return false;
        }
        return true;
    }

    @Override
    public boolean matches(ScriptPath path) {
        String mat = path.eventArgLowerAt(0);
        if (!mat.equals("liquid") && !old_material.tryAdvancedMatcher(mat)) {
            return false;
        }
        if (!runInCheck(path, location)) {
            return false;
        }
        return super.matches(path);
    }

    @Override
    public ObjectTag getContext(String name) {
        switch (name) {
            case "location": return location;
            case "old_material": return old_material;
            case "new_material": return new MaterialTag(event.getNewData());
        }
        return super.getContext(name);
    }

    @EventHandler
    public void onLiquidLevelChange(FluidLevelChangeEvent event) {
        location = new LocationTag(event.getBlock().getLocation());
        old_material = new MaterialTag(event.getBlock());
        this.event = event;
        fire(event);
    }
}

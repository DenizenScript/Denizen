package com.denizenscript.denizen.events.block;

import com.denizenscript.denizen.objects.LocationTag;
import com.denizenscript.denizen.events.BukkitScriptEvent;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.ObjectTag;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockRedstoneEvent;

public class RedstoneScriptEvent extends BukkitScriptEvent implements Listener {

    // <--[event]
    // @Events
    // redstone recalculated
    //
    // @Group Block
    //
    // @Location true
    //
    // @Warning This event fires very very rapidly!
    //
    // @Triggers when a redstone wire is recalculated.
    //
    // @Context
    // <context.location> returns the location of the block.
    // <context.old_current> returns what the redstone power level was.
    // <context.new_current> returns what the redstone power level is becoming.
    //
    // @Determine
    // ElementTag (Number) set the current value to a specific value.
    //
    // -->

    public RedstoneScriptEvent() {
        registerCouldMatcher("redstone recalculated");
    }


    public LocationTag location;
    public BlockRedstoneEvent event;

    @Override
    public boolean matches(ScriptPath path) {
        if (!runInCheck(path, location)) {
            return false;
        }
        return super.matches(path);
    }

    @Override
    public boolean applyDetermination(ScriptPath path, ObjectTag determinationObj) {
        if (determinationObj instanceof ElementTag element && element.isInt()) {
            event.setNewCurrent(element.asInt());
            return true;
        }
        return super.applyDetermination(path, determinationObj);
    }

    @Override
    public ObjectTag getContext(String name) {
        switch (name) {
            case "location": return location;
            case "old_current": return new ElementTag(event.getOldCurrent());
            case "new_current": return new ElementTag(event.getNewCurrent());
        }
        return super.getContext(name);
    }

    @EventHandler
    public void onBlockRedstone(BlockRedstoneEvent event) {
        location = new LocationTag(event.getBlock().getLocation());
        this.event = event;
        fire(event);
    }
}

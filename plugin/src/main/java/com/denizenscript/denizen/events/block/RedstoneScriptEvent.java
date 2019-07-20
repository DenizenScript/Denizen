package com.denizenscript.denizen.events.block;

import com.denizenscript.denizen.objects.LocationTag;
import com.denizenscript.denizen.events.BukkitScriptEvent;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.scripts.containers.ScriptContainer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockRedstoneEvent;

public class RedstoneScriptEvent extends BukkitScriptEvent implements Listener {

    // <--[event]
    // @Events
    // redstone recalculated
    //
    // @Regex ^on redstone recalculated$
    //
    // @Group Block
    //
    // @Switch in <area>
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
        instance = this;
    }

    public static RedstoneScriptEvent instance;

    public LocationTag location;
    public BlockRedstoneEvent event;

    @Override
    public boolean couldMatch(ScriptPath path) {
        return path.eventLower.startsWith("redstone recalculated");
    }

    @Override
    public boolean matches(ScriptPath path) {
        return runInCheck(path, location);
    }

    @Override
    public String getName() {
        return "RedstoneRecalculated";
    }

    @Override
    public boolean applyDetermination(ScriptContainer container, String determination) {
        if (!isDefaultDetermination(determination)) {
            ElementTag power = new ElementTag(determination);
            if (power.isInt()) {
                event.setNewCurrent(power.asInt());
                return true;
            }
        }
        return super.applyDetermination(container, determination);
    }

    @Override
    public ObjectTag getContext(String name) {
        if (name.equals("location")) {
            return location;
        }
        else if (name.equals("old_current")) {
            return new ElementTag(event.getOldCurrent());
        }
        else if (name.equals("new_current")) {
            return new ElementTag(event.getNewCurrent());
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

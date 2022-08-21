package com.denizenscript.denizen.events.block;

import com.denizenscript.denizen.objects.LocationTag;
import com.denizenscript.denizen.objects.MaterialTag;
import com.denizenscript.denizen.events.BukkitScriptEvent;
import com.denizenscript.denizencore.objects.ObjectTag;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.LeavesDecayEvent;

public class LeafDecaysScriptEvent extends BukkitScriptEvent implements Listener {

    // <--[event]
    // @Events
    // leaves decay
    // <block> decay
    //
    // @Group Block
    //
    // @Location true
    //
    // @Cancellable true
    //
    // @Triggers when leaves decay.
    //
    // @Context
    // <context.location> returns the LocationTag of the leaves.
    // <context.material> returns the MaterialTag of the leaves.
    //
    // -->

    public LeafDecaysScriptEvent() {
        registerCouldMatcher("leaves decay");
        registerCouldMatcher("<block> decay");
    }

    public LocationTag location;
    public MaterialTag material;
    public LeavesDecayEvent event;

    @Override
    public boolean matches(ScriptPath path) {
        String mat = path.eventArgLowerAt(0);
        if (!runInCheck(path, location)) {
            return false;
        }
        if (!mat.equals("leaves") && !material.tryAdvancedMatcher(mat)) {
            return false;
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
    public void onLeafDecays(LeavesDecayEvent event) {
        location = new LocationTag(event.getBlock().getLocation());
        material = new MaterialTag(event.getBlock());
        this.event = event;
        fire(event);
    }
}

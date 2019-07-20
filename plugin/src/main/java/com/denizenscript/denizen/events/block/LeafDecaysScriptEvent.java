package com.denizenscript.denizen.events.block;

import com.denizenscript.denizen.objects.LocationTag;
import com.denizenscript.denizen.objects.MaterialTag;
import com.denizenscript.denizen.events.BukkitScriptEvent;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.scripts.containers.ScriptContainer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.LeavesDecayEvent;

public class LeafDecaysScriptEvent extends BukkitScriptEvent implements Listener {

    // <--[event]
    // @Events
    // leaves decay
    // <block> decay
    //
    // @Regex ^on [^\s]+ decay$
    //
    // @Switch in <area>
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
        instance = this;
    }

    public static LeafDecaysScriptEvent instance;
    public LocationTag location;
    public MaterialTag material;
    public LeavesDecayEvent event;

    @Override
    public boolean couldMatch(ScriptPath path) {
        return path.eventArgLowerAt(1).equals("decay");
    }

    @Override
    public boolean matches(ScriptPath path) {
        String mat = path.eventArgLowerAt(0);
        if (!runInCheck(path, location)) {
            return false;
        }
        if (!mat.equals("leaves") && !tryMaterial(material, mat)) {
            return false;
        }
        return true;
    }

    @Override
    public String getName() {
        return "LeafDecays";
    }

    @Override
    public boolean applyDetermination(ScriptContainer container, String determination) {
        return super.applyDetermination(container, determination);
    }

    @Override
    public ObjectTag getContext(String name) {
        if (name.equals("location")) {
            return location;
        }
        else if (name.equals("material")) {
            return material;
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

package com.denizenscript.denizen.events.world;

import com.denizenscript.denizen.objects.InventoryTag;
import com.denizenscript.denizen.objects.LocationTag;
import com.denizenscript.denizen.events.BukkitScriptEvent;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.scripts.containers.ScriptContainer;
import com.denizenscript.denizencore.utilities.CoreUtilities;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.BrewEvent;

public class BrewsScriptEvent extends BukkitScriptEvent implements Listener {

    // <--[event]
    // @Events
    // brewing stand brews
    //
    // @Regex ^on brewing stand brews$
    // @Switch in <area>
    //
    // @Cancellable true
    //
    // @Triggers when a brewing stand brews a potion.
    //
    // @Context
    // <context.location> returns the LocationTag of the brewing stand.
    // <context.inventory> returns the InventoryTag of the brewing stand's contents.
    //
    // -->

    public BrewsScriptEvent() {
        instance = this;
    }

    public static BrewsScriptEvent instance;
    public InventoryTag inventory;
    public LocationTag location;
    public BrewEvent event;

    @Override
    public boolean couldMatch(ScriptContainer scriptContainer, String s) {
        return CoreUtilities.toLowerCase(s).startsWith("brewing stand brews");
    }

    @Override
    public boolean matches(ScriptPath path) {
        return runInCheck(path, location);
    }

    @Override
    public String getName() {
        return "Brews";
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
        else if (name.equals("inventory")) {
            return inventory;
        }
        return super.getContext(name);
    }

    @EventHandler
    public void onBrews(BrewEvent event) {
        location = new LocationTag(event.getBlock().getLocation());
        inventory = InventoryTag.mirrorBukkitInventory(event.getContents());
        this.event = event;
        fire(event);
    }
}

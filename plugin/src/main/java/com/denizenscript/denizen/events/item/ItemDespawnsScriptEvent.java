package com.denizenscript.denizen.events.item;

import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizen.objects.ItemTag;
import com.denizenscript.denizen.objects.LocationTag;
import com.denizenscript.denizen.events.BukkitScriptEvent;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.utilities.CoreUtilities;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ItemDespawnEvent;

public class ItemDespawnsScriptEvent extends BukkitScriptEvent implements Listener {

    // <--[event]
    // @Events
    // item despawns
    // <item> despawns
    //
    // @Regex ^on [^\s]+ despawns$
    //
    // @Switch in:<area> to only process the event if it occurred within a specified area.
    //
    // @Cancellable true
    //
    // @Triggers when an item entity despawns.
    //
    // @Context
    // <context.item> returns the ItemTag of the entity.
    // <context.entity> returns the EntityTag.
    // <context.location> returns the location of the entity to be despawned.
    //
    // -->

    public ItemDespawnsScriptEvent() {
        instance = this;
    }

    public static ItemDespawnsScriptEvent instance;
    public ItemTag item;
    public LocationTag location;
    public EntityTag entity;
    public ItemDespawnEvent event;

    @Override
    public boolean couldMatch(ScriptPath path) {
        if (!path.eventArgLowerAt(1).equals("despawns")) {
            return false;
        }
        if (!couldMatchItem(path.eventArgLowerAt(0))) {
            return false;
        }
        return true;
    }

    @Override
    public boolean matches(ScriptPath path) {
        String item_test = CoreUtilities.getXthArg(0, path.eventLower);

        if (!tryItem(item, item_test)) {
            return false;
        }

        if (!runInCheck(path, location)) {
            return false;
        }

        return super.matches(path);
    }

    @Override
    public String getName() {
        return "ItemDespawns";
    }

    @Override
    public ObjectTag getContext(String name) {
        if (name.equals("location")) {
            return location;
        }
        else if (name.equals("item")) {
            return item;
        }
        else if (name.equals("entity")) {
            return entity;
        }
        return super.getContext(name);
    }

    @EventHandler
    public void onItemDespawns(ItemDespawnEvent event) {
        location = new LocationTag(event.getLocation());
        item = new ItemTag(event.getEntity().getItemStack());
        entity = new EntityTag(event.getEntity());
        this.event = event;
        fire(event);
    }
}

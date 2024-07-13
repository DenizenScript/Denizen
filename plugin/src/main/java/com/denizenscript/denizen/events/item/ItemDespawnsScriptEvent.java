package com.denizenscript.denizen.events.item;

import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizen.objects.ItemTag;
import com.denizenscript.denizen.objects.LocationTag;
import com.denizenscript.denizen.events.BukkitScriptEvent;
import com.denizenscript.denizencore.objects.ObjectTag;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ItemDespawnEvent;

public class ItemDespawnsScriptEvent extends BukkitScriptEvent implements Listener {

    // <--[event]
    // @Events
    // <item> despawns
    //
    // @Group Item
    //
    // @Location true
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
        registerCouldMatcher("<item> despawns");
    }

    public ItemTag item;
    public LocationTag location;
    public EntityTag entity;
    public ItemDespawnEvent event;

    @Override
    public boolean matches(ScriptPath path) {
        if (!path.tryArgObject(0, item)) {
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
            case "item": return item;
            case "entity": return entity;
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

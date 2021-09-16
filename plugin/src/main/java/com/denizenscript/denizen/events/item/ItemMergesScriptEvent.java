package com.denizenscript.denizen.events.item;

import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizen.objects.ItemTag;
import com.denizenscript.denizen.objects.LocationTag;
import com.denizenscript.denizen.events.BukkitScriptEvent;
import com.denizenscript.denizencore.objects.ObjectTag;
import org.bukkit.entity.Item;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ItemMergeEvent;

public class ItemMergesScriptEvent extends BukkitScriptEvent implements Listener {

    // <--[event]
    // @Events
    // item merges
    // <item> merges
    //
    // @Regex ^on [^\s]+ merges$
    //
    // @Group Item
    //
    // @Location true
    //
    // @Cancellable true
    //
    // @Triggers when an item entity merges into another item entity.
    //
    // @Context
    // <context.item> returns the ItemTag of the entity.
    // <context.entity> returns the EntityTag.
    // <context.target> returns the EntityTag being merged into.
    // <context.location> returns the location of the entity to be spawned.
    //
    // -->

    public ItemMergesScriptEvent() {
        instance = this;
    }

    public static ItemMergesScriptEvent instance;
    public ItemTag item;
    public LocationTag location;
    public EntityTag entity;
    public ItemMergeEvent event;

    @Override
    public boolean couldMatch(ScriptPath path) {
        if (!path.eventArgLowerAt(1).equals("merges")) {
            return false;
        }
        if (!couldMatchItem(path.eventArgLowerAt(0))) {
            return false;
        }
        return true;
    }

    @Override
    public boolean matches(ScriptPath path) {
        String item_test = path.eventArgLowerAt(0);
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
        return "ItemMerges";
    }

    @Override
    public ObjectTag getContext(String name) {
        switch (name) {
            case "location": return location;
            case "item": return item;
            case "entity": return entity;
            case "target": return new EntityTag(event.getTarget());
        }
        return super.getContext(name);
    }

    @EventHandler
    public void onItemMerges(ItemMergeEvent event) {
        Item entity = event.getEntity();
        Item target = event.getTarget();
        location = new LocationTag(target.getLocation());
        item = new ItemTag(entity.getItemStack());
        this.entity = new EntityTag(entity);
        this.event = event;
        fire(event);
    }
}

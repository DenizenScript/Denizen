package com.denizenscript.denizen.events.entity;

import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizen.objects.ItemTag;
import com.denizenscript.denizen.objects.LocationTag;
import com.denizenscript.denizen.objects.MaterialTag;
import com.denizenscript.denizen.events.BukkitScriptEvent;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.scripts.containers.ScriptContainer;
import com.denizenscript.denizencore.utilities.CoreUtilities;
import org.bukkit.entity.Item;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ItemMergeEvent;

public class ItemMergesScriptEvent extends BukkitScriptEvent implements Listener {

    // <--[event]
    // @Events
    // item merges
    // <item> merges
    // <material> merges
    //
    // @Regex ^on [^\s]+ merges$
    // @Switch in <area>
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
    public boolean couldMatch(ScriptContainer scriptContainer, String s) {
        String lower = CoreUtilities.toLowerCase(s);
        String cmd = CoreUtilities.getXthArg(1, lower);
        return cmd.equals("merges") && couldMatchItem(CoreUtilities.getXthArg(0, lower));
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

        return true;
    }

    @Override
    public String getName() {
        return "ItemMerges";
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
        else if (name.equals("target")) {
            return new EntityTag(event.getTarget());
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

package com.denizenscript.denizen.events.entity;

import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizen.objects.ItemTag;
import com.denizenscript.denizen.objects.LocationTag;
import com.denizenscript.denizen.events.BukkitScriptEvent;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.scripts.containers.ScriptContainer;
import com.denizenscript.denizencore.utilities.CoreUtilities;
import org.bukkit.entity.Item;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ItemSpawnEvent;

public class ItemSpawnsScriptEvent extends BukkitScriptEvent implements Listener {

    // <--[event]
    // @Events
    // item spawns
    // <item> spawns
    // <material> spawns
    //
    // @Regex ^on [^\s]+ spawns$
    //
    // @Switch in <area>
    //
    // @Cancellable true
    //
    // @Triggers when an item entity spawns.
    //
    // @Context
    // <context.item> returns the ItemTag of the entity.
    // <context.entity> returns the EntityTag.
    // <context.location> returns the location of the entity to be spawned.
    //
    // -->

    public ItemSpawnsScriptEvent() {
        instance = this;
    }

    public static ItemSpawnsScriptEvent instance;
    public ItemTag item;
    public LocationTag location;
    public EntityTag entity;
    public ItemSpawnEvent event;

    // TODO: De-collide with 'entity spawns'

    @Override
    public boolean couldMatch(ScriptContainer scriptContainer, String s) {
        String lower = CoreUtilities.toLowerCase(s);
        String arg = CoreUtilities.getXthArg(2, lower);
        if (arg.length() > 0 && !arg.equals("in")) {
            return false;
        }
        return CoreUtilities.xthArgEquals(1, lower, "spawns");
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
        return "ItemSpawns";
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
    public void onItemSpawns(ItemSpawnEvent event) {
        Item entity = event.getEntity();
        location = new LocationTag(event.getLocation());
        item = new ItemTag(entity.getItemStack());
        this.entity = new EntityTag(entity);
        this.event = event;
        EntityTag.rememberEntity(entity);
        fire(event);
        EntityTag.forgetEntity(entity);
    }
}

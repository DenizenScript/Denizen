package com.denizenscript.denizen.events.entity;

import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizen.objects.ItemTag;
import com.denizenscript.denizen.objects.LocationTag;
import com.denizenscript.denizen.events.BukkitScriptEvent;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.scripts.containers.ScriptContainer;
import com.denizenscript.denizencore.utilities.CoreUtilities;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ItemDespawnEvent;

public class ItemDespawnsScriptEvent extends BukkitScriptEvent implements Listener {

    // TODO: de-colide with entity despawns
    // <--[event]
    // @Events
    // item despawns
    // <item> despawns
    // <material> despawns
    //
    // @Regex ^on [^\s]+ despawns$
    // @Switch in <area>
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
    public boolean couldMatch(ScriptContainer scriptContainer, String s) {
        String lower = CoreUtilities.toLowerCase(s);
        String cmd = CoreUtilities.getXthArg(1, lower);
        return cmd.equals("despawns");
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

        return true;
    }

    @Override
    public String getName() {
        return "ItemDespawns";
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

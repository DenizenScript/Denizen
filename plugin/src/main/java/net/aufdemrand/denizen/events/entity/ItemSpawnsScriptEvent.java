package net.aufdemrand.denizen.events.entity;

import net.aufdemrand.denizen.events.BukkitScriptEvent;
import net.aufdemrand.denizen.objects.dEntity;
import net.aufdemrand.denizen.objects.dItem;
import net.aufdemrand.denizen.objects.dLocation;
import net.aufdemrand.denizencore.objects.dObject;
import net.aufdemrand.denizencore.scripts.containers.ScriptContainer;
import net.aufdemrand.denizencore.utilities.CoreUtilities;
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
    // @Switch in <area>
    //
    // @Cancellable true
    //
    // @Triggers when an item entity spawns.
    //
    // @Context
    // <context.item> returns the dItem of the entity.
    // <context.entity> returns the dEntity.
    // <context.location> returns the location of the entity to be spawned.
    //
    // -->

    public ItemSpawnsScriptEvent() {
        instance = this;
    }

    public static ItemSpawnsScriptEvent instance;
    public dItem item;
    public dLocation location;
    public dEntity entity;
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
        String lower = path.eventLower;
        String item_test = CoreUtilities.getXthArg(0, lower);

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
    public boolean applyDetermination(ScriptContainer container, String determination) {
        return super.applyDetermination(container, determination);
    }

    @Override
    public dObject getContext(String name) {
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
        location = new dLocation(event.getLocation());
        item = new dItem(entity.getItemStack());
        this.entity = new dEntity(entity);
        this.event = event;
        dEntity.rememberEntity(entity);
        fire(event);
        dEntity.forgetEntity(entity);
    }
}

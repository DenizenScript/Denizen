package net.aufdemrand.denizen.events.entity;

import net.aufdemrand.denizen.events.BukkitScriptEvent;
import net.aufdemrand.denizen.objects.dEntity;
import net.aufdemrand.denizen.objects.dItem;
import net.aufdemrand.denizen.objects.dLocation;
import net.aufdemrand.denizen.objects.dMaterial;
import net.aufdemrand.denizen.utilities.DenizenAPI;
import net.aufdemrand.denizencore.objects.dObject;
import net.aufdemrand.denizencore.scripts.containers.ScriptContainer;
import net.aufdemrand.denizencore.utilities.CoreUtilities;
import org.bukkit.Bukkit;
import org.bukkit.entity.Item;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ItemMergeEvent;

public class ItemMergesScriptEvent extends BukkitScriptEvent implements Listener {

    // <--[event]
    // @Events
    // item merges (in <area>)
    // <item> merges (in <area>)
    // <material> merges (in <area>)
    //
    // @Regex ^on [^\s]+ merges( in ((notable (cuboid|ellipsoid))|([^\s]+)))?$
    //
    // @Cancellable true
    //
    // @Triggers when an item entity merges into another item entity.
    //
    // @Context
    // <context.item> returns the dItem of the entity.
    // <context.entity> returns the dEntity.
    // <context.target> returns the dEntity being merged into.
    // <context.location> returns the location of the entity to be spawned.
    //
    // -->

    public ItemMergesScriptEvent() {
        instance = this;
    }

    public static ItemMergesScriptEvent instance;
    public dItem item;
    public dLocation location;
    public dEntity entity;
    public ItemMergeEvent event;

    @Override
    public boolean couldMatch(ScriptContainer scriptContainer, String s) {
        String lower = CoreUtilities.toLowerCase(s);
        String cmd = CoreUtilities.getXthArg(1, lower);
        String entTest = CoreUtilities.getXthArg(0, lower);
        return cmd.equals("merges")
                && (entTest.equals("item") || dMaterial.matches(entTest) || dItem.matches(entTest));
    }

    @Override
    public boolean matches(ScriptPath path) {
        String s = path.event;
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
        return "ItemMerges";
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
        else if (name.equals("target")) {
            return new dEntity(event.getTarget());
        }
        return super.getContext(name);
    }

    @EventHandler
    public void onItemMerges(ItemMergeEvent event) {
        Item entity = event.getEntity();
        Item target = event.getTarget();
        location = new dLocation(target.getLocation());
        item = new dItem(entity.getItemStack());
        this.entity = new dEntity(entity);
        cancelled = event.isCancelled();
        this.event = event;
        fire();
        event.setCancelled(cancelled);
    }
}

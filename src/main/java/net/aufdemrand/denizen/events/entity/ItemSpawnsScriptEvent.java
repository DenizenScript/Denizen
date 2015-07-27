package net.aufdemrand.denizen.events.entity;

import net.aufdemrand.denizen.events.BukkitScriptEvent;
import net.aufdemrand.denizen.objects.dEntity;
import net.aufdemrand.denizen.objects.dItem;
import net.aufdemrand.denizen.objects.dLocation;
import net.aufdemrand.denizen.utilities.DenizenAPI;
import net.aufdemrand.denizencore.objects.dObject;
import net.aufdemrand.denizencore.scripts.containers.ScriptContainer;
import net.aufdemrand.denizencore.utilities.CoreUtilities;
import org.bukkit.Bukkit;
import org.bukkit.entity.Item;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ItemSpawnEvent;

import java.util.HashMap;

public class ItemSpawnsScriptEvent extends BukkitScriptEvent implements Listener {

    // <--[event]
    // @Events
    // item spawns (in <area>)
    // <item> spawns (in <area>)
    // <material> spawns (in <area>)
    //
    // @Regex ^on [^\s]+ spawns( in ((notable (cuboid|ellipsoid))|([^\s]+)))?$
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
        String cmd = CoreUtilities.getXthArg(1, lower);
        return cmd.equals("spawns");
    }

    @Override
    public boolean matches(ScriptContainer scriptContainer, String s) {
        String lower = CoreUtilities.toLowerCase(s);
        String item_test = CoreUtilities.getXthArg(0, lower);

        if (!item_test.equals("item") && !tryItem(item, item_test)) {
            return false;
        }

        if (!runInCheck(scriptContainer, s, lower, location)) {
            return false;
        }

        return true;
    }

    @Override
    public String getName() {
        return "ItemSpawns";
    }

    @Override
    public void init() {
        Bukkit.getServer().getPluginManager().registerEvents(this, DenizenAPI.getCurrentInstance());
    }

    @Override
    public void destroy() {
        ItemSpawnEvent.getHandlerList().unregister(this);
    }

    @Override
    public boolean applyDetermination(ScriptContainer container, String determination) {
        return super.applyDetermination(container, determination);
    }

    @Override
    public HashMap<String, dObject> getContext() {
        HashMap<String, dObject> context = super.getContext();
        context.put("location", location);
        context.put("item", item);
        context.put("entity", entity);
        return context;
    }

    @EventHandler(ignoreCancelled = true)
    public void onItemSpawns(ItemSpawnEvent event) {
        Item entity = event.getEntity();
        location = new dLocation(event.getLocation());
        item = new dItem(entity.getItemStack());
        this.entity = new dEntity(entity);
        cancelled = event.isCancelled();
        this.event = event;
        dEntity.rememberEntity(entity);
        fire();
        dEntity.forgetEntity(entity);
        event.setCancelled(cancelled);
    }
}

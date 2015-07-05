package net.aufdemrand.denizen.events.player;

import net.aufdemrand.denizen.BukkitScriptEntryData;
import net.aufdemrand.denizen.events.BukkitScriptEvent;
import net.aufdemrand.denizen.objects.dEntity;
import net.aufdemrand.denizen.objects.dItem;
import net.aufdemrand.denizen.objects.dLocation;
import net.aufdemrand.denizen.utilities.DenizenAPI;
import net.aufdemrand.denizencore.objects.dObject;
import net.aufdemrand.denizencore.scripts.ScriptEntryData;
import net.aufdemrand.denizencore.scripts.containers.ScriptContainer;
import net.aufdemrand.denizencore.utilities.CoreUtilities;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerDropItemEvent;

import java.util.HashMap;

public class PlayerDropsItemScriptEvent extends BukkitScriptEvent implements Listener {

    // <--[event]
    // @Events
    // player drops item (in <area>)
    // player drops <item> (in <area>)
    //
    // @Cancellable true
    //
    // @Triggers when a player drops an item.
    //
    // @Context
    // <context.item> returns the dItem.
    // <context.entity> returns a dEntity of the item.
    // <context.location> returns a dLocation of the item's location.
    //
    // -->

    public PlayerDropsItemScriptEvent() {
        instance = this;
    }

    public static PlayerDropsItemScriptEvent instance;
    public dItem item;
    public dEntity entity;
    public dLocation location;
    public PlayerDropItemEvent event;

    @Override
    public boolean couldMatch(ScriptContainer scriptContainer, String s) {
        return CoreUtilities.toLowerCase(s).startsWith("player drops");
    }

    @Override
    public boolean matches(ScriptContainer scriptContainer, String s) {
        String lower = CoreUtilities.toLowerCase(s);

        if (dEntity.isNPC(event.getPlayer())) {
            return false;
        }
        String iCheck = CoreUtilities.getXthArg(2, lower);
        if (!iCheck.equals("item") && !tryItem(item, iCheck)) {
            return false;
        }
        if (!runInCheck(scriptContainer, s, lower, location)) {
            return false;
        }
        return true;
    }

    @Override
    public String getName() {
        return "PlayerDropsItem";
    }

    @Override
    public void init() {
        Bukkit.getServer().getPluginManager().registerEvents(this, DenizenAPI.getCurrentInstance());
    }

    @Override
    public void destroy() {
        PlayerDropItemEvent.getHandlerList().unregister(this);
    }

    @Override
    public boolean applyDetermination(ScriptContainer container, String determination) {
        return super.applyDetermination(container, determination);
    }

    @Override
    public ScriptEntryData getScriptEntryData() {
        return new BukkitScriptEntryData(dEntity.isPlayer(event.getPlayer()) ? dEntity.getPlayerFrom(event.getPlayer()) : null, null);
    }

    @Override
    public HashMap<String, dObject> getContext() {
        HashMap<String, dObject> context = super.getContext();
        context.put("item", item);
        context.put("entity", entity);
        context.put("location", location);
        return context;
    }

    @EventHandler
    public void onPlayerDropsItem(PlayerDropItemEvent event) {
        location = new dLocation(event.getPlayer().getLocation());
        item = new dItem(event.getItemDrop().getItemStack());
        entity = new dEntity(event.getItemDrop());
        cancelled = event.isCancelled();
        this.event = event;
        fire();
        event.setCancelled(cancelled);
    }
}

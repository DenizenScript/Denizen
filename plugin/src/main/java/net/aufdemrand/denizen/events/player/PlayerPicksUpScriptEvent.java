package net.aufdemrand.denizen.events.player;

import net.aufdemrand.denizen.BukkitScriptEntryData;
import net.aufdemrand.denizen.events.BukkitScriptEvent;
import net.aufdemrand.denizen.objects.dEntity;
import net.aufdemrand.denizen.objects.dItem;
import net.aufdemrand.denizen.objects.dLocation;
import net.aufdemrand.denizen.objects.dPlayer;
import net.aufdemrand.denizen.utilities.DenizenAPI;
import net.aufdemrand.denizencore.objects.dObject;
import net.aufdemrand.denizencore.scripts.ScriptEntryData;
import net.aufdemrand.denizencore.scripts.containers.ScriptContainer;
import net.aufdemrand.denizencore.utilities.CoreUtilities;
import org.bukkit.Bukkit;
import org.bukkit.entity.Item;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerPickupItemEvent;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class PlayerPicksUpScriptEvent extends BukkitScriptEvent implements Listener {

    // <--[event]
    // @Events
    // player picks up item (in <area>)
    // player picks up <item> (in <area>)
    // player takes item (in <area>)
    // player takes <item> (in <area>)
    //
    // @Regex ^on player (picks up|takes) [^\s]+( in ((notable (cuboid|ellipsoid))|([^\s]+)))?$
    //
    // @Cancellable true
    //
    // @Triggers when a player picks up an item.
    //
    // @Context
    // <context.item> returns the dItem.
    // <context.entity> returns a dEntity of the item.
    // <context.location> returns a dLocation of the item's location.
    //
    // @Determine
    // "ITEM:" + dItem to changed the item being picked up.
    //
    // -->

    public PlayerPicksUpScriptEvent() {
        instance = this;
    }

    public static PlayerPicksUpScriptEvent instance;
    public dItem item;
    public boolean itemChanged;
    public dEntity entity;
    public dLocation location;
    public PlayerPickupItemEvent event;

    private static final Set<UUID> editedItems = new HashSet<UUID>();

    @Override
    public boolean couldMatch(ScriptContainer scriptContainer, String s) {
        String lower = CoreUtilities.toLowerCase(s);
        if (CoreUtilities.xthArgEquals(3, lower, "from")) {
            return false;
        }
        return lower.startsWith("player picks up") || lower.startsWith("player takes");
    }

    @Override
    public boolean matches(ScriptPath path) {
        String s = path.event;
        String lower = path.eventLower;
        String iTest = CoreUtilities.xthArgEquals(1, lower, "picks") ?
                CoreUtilities.getXthArg(3, lower) : CoreUtilities.getXthArg(2, lower);
        if (!tryItem(item, iTest)) {
            return false;
        }
        return runInCheck(scriptContainer, s, lower, location);
    }

    @Override
    public String getName() {
        return "PlayerPicksUp";
    }

    @Override
    public void init() {
        Bukkit.getServer().getPluginManager().registerEvents(this, DenizenAPI.getCurrentInstance());
    }

    @Override
    public void destroy() {
        PlayerPickupItemEvent.getHandlerList().unregister(this);
    }

    @Override
    public boolean applyDetermination(ScriptContainer container, String determination) {
        String lower = CoreUtilities.toLowerCase(determination);
        if (lower.startsWith("item:")) {
            item = dItem.valueOf(determination.substring("item:".length()));
            itemChanged = true;
            return true;
        }
        return super.applyDetermination(container, determination);
    }

    @Override
    public ScriptEntryData getScriptEntryData() {
        return new BukkitScriptEntryData(new dPlayer(event.getPlayer()), null);
    }

    @Override
    public dObject getContext(String name) {
        if (name.equals("item")) {
            return item;
        }
        else if (name.equals("entity")) {
            return entity;
        }
        else if (name.equals("location")) {
            return location;
        }
        return super.getContext(name);
    }

    @EventHandler
    public void onPlayerPicksUp(PlayerPickupItemEvent event) {
        if (dEntity.isNPC(event.getPlayer())) {
            return;
        }
        Item itemEntity = event.getItem();
        UUID itemUUID = itemEntity.getUniqueId();
        if (editedItems.contains(itemUUID)) {
            editedItems.remove(itemUUID);
            return;
        }
        location = new dLocation(itemEntity.getLocation());
        item = new dItem(itemEntity.getItemStack());
        entity = new dEntity(itemEntity);
        cancelled = event.isCancelled();
        itemChanged = false;
        this.event = event;
        fire();
        if (itemChanged) {
            itemEntity.setItemStack(item.getItemStack());
            editedItems.add(itemUUID);
            event.setCancelled(true);
        }
        else {
            event.setCancelled(cancelled);
        }
    }
}

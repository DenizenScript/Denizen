package com.denizenscript.denizen.events.player;

import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizen.objects.ItemTag;
import com.denizenscript.denizen.utilities.implementation.BukkitScriptEntryData;
import com.denizenscript.denizen.events.BukkitScriptEvent;
import com.denizenscript.denizen.objects.LocationTag;
import com.denizenscript.denizen.objects.PlayerTag;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.scripts.ScriptEntryData;
import org.bukkit.entity.Item;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerDropItemEvent;

public class PlayerDropsItemScriptEvent extends BukkitScriptEvent implements Listener {

    // <--[event]
    // @Events
    // player drops item
    // player drops <item>
    //
    // @Regex ^on player drops [^\s]+$
    //
    // @Switch in:<area> to only process the event if it occurred within a specified area.
    //
    // @Cancellable true
    //
    // @Triggers when a player drops an item.
    //
    // @Context
    // <context.item> returns the ItemTag.
    // <context.entity> returns a EntityTag of the item.
    // <context.location> returns a LocationTag of the item's location.
    //
    // @Player Always.
    //
    // -->

    public PlayerDropsItemScriptEvent() {
        instance = this;
    }

    public static PlayerDropsItemScriptEvent instance;
    public ItemTag item;
    public LocationTag location;
    public PlayerDropItemEvent event;

    @Override
    public boolean couldMatch(ScriptPath path) {
        if (!path.eventLower.startsWith("player drops")) {
            return false;
        }
        if (!couldMatchItem(path.eventArgLowerAt(2))) {
            return false;
        }
        return true;
    }

    @Override
    public boolean matches(ScriptPath path) {

        String iCheck = path.eventArgLowerAt(2);
        if (!iCheck.equals("item") && !tryItem(item, iCheck)) {
            return false;
        }
        if (!runInCheck(path, location)) {
            return false;
        }
        return super.matches(path);
    }

    @Override
    public String getName() {
        return "PlayerDropsItem";
    }

    @Override
    public ScriptEntryData getScriptEntryData() {
        return new BukkitScriptEntryData(new PlayerTag(event.getPlayer()), null);
    }

    @Override
    public ObjectTag getContext(String name) {
        if (name.equals("item")) {
            return item;
        }
        else if (name.equals("entity")) {
            return new EntityTag(event.getItemDrop());
        }
        else if (name.equals("location")) {
            return location;
        }
        return super.getContext(name);
    }

    @EventHandler
    public void onPlayerDropsItem(PlayerDropItemEvent event) {
        if (EntityTag.isNPC(event.getPlayer())) {
            return;
        }
        location = new LocationTag(event.getPlayer().getLocation());
        Item itemDrop = event.getItemDrop();
        EntityTag.rememberEntity(itemDrop);
        item = new ItemTag(itemDrop.getItemStack());
        this.event = event;
        fire(event);
    }
}

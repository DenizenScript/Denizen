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
import org.bukkit.event.player.PlayerDropItemEvent;

public class PlayerDropsItemScriptEvent extends BukkitScriptEvent implements Listener {

    // <--[event]
    // @Events
    // player drops item (in <area>)
    // player drops <item> (in <area>)
    //
    // @Regex ^on player drops [^\s]+( in ((notable (cuboid|ellipsoid))|([^\s]+)))?$
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
    public boolean matches(ScriptPath path) {
        String s = path.event;
        String lower = path.eventLower;

        String iCheck = CoreUtilities.getXthArg(2, lower);
        if (!iCheck.equals("item") && !tryItem(item, iCheck)) {
            return false;
        }
        if (!runInCheck(path, location)) {
            return false;
        }
        return true;
    }

    @Override
    public String getName() {
        return "PlayerDropsItem";
    }

    @Override
    public boolean applyDetermination(ScriptContainer container, String determination) {
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
    public void onPlayerDropsItem(PlayerDropItemEvent event) {
        if (dEntity.isNPC(event.getPlayer())) {
            return;
        }
        location = new dLocation(event.getPlayer().getLocation());
        Item itemDrop = event.getItemDrop();
        dEntity.rememberEntity(itemDrop);
        item = new dItem(itemDrop.getItemStack());
        entity = new dEntity(itemDrop);
        cancelled = event.isCancelled();
        this.event = event;
        fire();
        event.setCancelled(cancelled);
    }
}

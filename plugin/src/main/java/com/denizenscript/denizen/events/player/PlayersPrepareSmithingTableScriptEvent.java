package com.denizenscript.denizen.events.player;

import com.denizenscript.denizen.events.BukkitScriptEvent;
import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizen.objects.InventoryTag;
import com.denizenscript.denizen.objects.ItemTag;
import com.denizenscript.denizen.objects.PlayerTag;
import com.denizenscript.denizen.utilities.implementation.BukkitScriptEntryData;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.scripts.ScriptEntryData;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.PrepareSmithingEvent;

public class PlayersPrepareSmithingTableScriptEvent extends BukkitScriptEvent implements Listener {

    // <--[event]
    // @Events
    // player prepares smithing item
    // player prepares smithing <item>
    //
    // @Regex ^on player prepares smithing [^\s]+$
    //
    // @Group Player
    //
    // @Triggers when a player prepares to upgrade an item on a smithing table.
    //
    // @Warning The player doing the smithing is estimated and may be inaccurate.
    //
    // @Context
    // <context.inventory> returns the InventoryTag of the smithing table inventory.
    // <context.item> returns the ItemTag after upgrading.
    //
    // @Determine
    // ItemTag to change the item that results from the upgrade.
    //
    // @Player Always.
    //
    // -->

    public PlayersPrepareSmithingTableScriptEvent() {
    }

    public PrepareSmithingEvent event;
    public ItemTag result;
    public PlayerTag player;

    @Override
    public boolean couldMatch(ScriptPath path) {
        if (!path.eventLower.startsWith("player prepares smithing")) {
            return false;
        }
        if (!couldMatchItem(path.eventArgLowerAt(3))) {
            return false;
        }
        return true;
    }

    @Override
    public boolean matches(ScriptPath path) {
        if (!result.tryAdvancedMatcher(path.eventArgLowerAt(3))) {
            return false;
        }
        return super.matches(path);
    }

    @Override
    public boolean applyDetermination(ScriptPath path, ObjectTag determinationObj) {
        String determination = determinationObj.toString();
        if (ItemTag.matches(determination)) {
            result = determinationObj.asType(ItemTag.class, getTagContext(path));
            event.setResult(result.getItemStack());
            return true;
        }
        return super.applyDetermination(path, determinationObj);
    }

    @Override
    public ScriptEntryData getScriptEntryData() {
        return new BukkitScriptEntryData(player, null);
    }

    @Override
    public ObjectTag getContext(String name) {
        if (name.equals("item")) {
            return result;
        }
        else if (name.equals("inventory")) {
            return InventoryTag.mirrorBukkitInventory(event.getInventory());
        }
        return super.getContext(name);
    }

    @EventHandler
    public void onCraftItem(PrepareSmithingEvent event) {
        if (event.getInventory().getViewers().isEmpty()) {
            return;
        }
        HumanEntity humanEntity = event.getInventory().getViewers().get(0);
        if (EntityTag.isNPC(humanEntity)) {
            return;
        }
        this.event = event;
        result = new ItemTag(event.getResult());
        this.player = EntityTag.getPlayerFrom(humanEntity);
        this.cancelled = false;
        fire(event);
    }
}

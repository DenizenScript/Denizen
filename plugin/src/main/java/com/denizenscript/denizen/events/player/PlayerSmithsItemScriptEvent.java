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
import org.bukkit.event.inventory.SmithItemEvent;

public class PlayerSmithsItemScriptEvent extends BukkitScriptEvent implements Listener {

    // <--[event]
    // @Events
    // player smiths item
    // player smiths <item>
    //
    // @Regex ^on player smiths [^\s]+$
    //
    // @Group Player
    //
    // @Cancellable true
    //
    // @Triggers when a player upgrades an item on a smithing table.
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

    public PlayerSmithsItemScriptEvent() {
    }

    public SmithItemEvent event;
    public ItemTag result;
    public PlayerTag player;

    @Override
    public boolean couldMatch(ScriptPath path) {
        if (!path.eventLower.startsWith("player smiths")) {
            return false;
        }
        if (!couldMatchItem(path.eventArgLowerAt(2))) {
            return false;
        }
        return true;
    }

    @Override
    public boolean matches(ScriptPath path) {
        if (!path.tryArgObject(2, result)) {
            return false;
        }
        return super.matches(path);
    }

    @Override
    public boolean applyDetermination(ScriptPath path, ObjectTag determinationObj) {
        String determination = determinationObj.toString();
        if (ItemTag.matches(determination)) {
            result = determinationObj.asType(ItemTag.class, getTagContext(path));
            event.getInventory().setResult(result.getItemStack());
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
    public void onCraftItem(SmithItemEvent event) {
        HumanEntity humanEntity = event.getView().getPlayer();
        if (EntityTag.isNPC(humanEntity)) {
            return;
        }
        this.event = event;
        result = new ItemTag(event.getInventory().getResult());
        this.player = EntityTag.getPlayerFrom(humanEntity);
        fire(event);
    }
}

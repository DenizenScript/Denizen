package com.denizenscript.denizen.events.entity;

import com.denizenscript.denizen.objects.dEntity;
import com.denizenscript.denizen.objects.dInventory;
import com.denizenscript.denizen.objects.dItem;
import com.denizenscript.denizen.objects.dLocation;
import com.denizenscript.denizen.BukkitScriptEntryData;
import com.denizenscript.denizen.events.BukkitScriptEvent;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.ArgumentHelper;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.scripts.ScriptEntryData;
import com.denizenscript.denizencore.scripts.containers.ScriptContainer;
import com.denizenscript.denizencore.utilities.CoreUtilities;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.enchantment.EnchantItemEvent;

public class ItemEnchantedScriptEvent extends BukkitScriptEvent implements Listener {

    // TODO: Find more appropriate package than 'entity' for this. Probably 'player'.

    // <--[event]
    // @Events
    // item enchanted
    // <item> enchanted
    //
    // @Regex ^on [^\s]+ enchanted$
    // @Switch in <area>
    //
    // @Cancellable true
    //
    // @Triggers when an item is enchanted.
    //
    // @Context
    // <context.entity> returns the dEntity of the enchanter (if applicable)
    // <context.location> returns the dLocation of the enchanting table.
    // <context.inventory> returns the dInventory of the enchanting table.
    // <context.item> returns the dItem to be enchanted.
    // <context.button> returns which button was pressed to initiate the enchanting.
    // <context.cost> returns the experience level cost of the enchantment.
    //
    // @Determine
    // Element(Number) to set the experience level cost of the enchantment.
    // "RESULT:" + dItem to change the item result (only affects metadata (like enchantments), not material/quantity/etc!).
    // "ENCHANTS:" + dItem to change the resultant enchantments based on a dItem.
    // -->

    public ItemEnchantedScriptEvent() {
        instance = this;
    }

    public static ItemEnchantedScriptEvent instance;
    public dEntity entity;
    public dLocation location;
    public dInventory inventory;
    public dItem item;
    public ElementTag button;
    public int cost;
    public EnchantItemEvent event;
    public boolean itemEdited;
    public dItem enchantsRes;

    @Override
    public boolean couldMatch(ScriptContainer scriptContainer, String s) {
        String lower = CoreUtilities.toLowerCase(s);
        String cmd = CoreUtilities.getXthArg(1, lower);
        return cmd.equals("enchanted");
    }

    @Override
    public boolean matches(ScriptPath path) {
        String itemTest = path.eventArgLowerAt(0);

        if (!itemTest.equals("item") && !tryItem(item, itemTest)) {
            return false;
        }

        if (!runInCheck(path, location)) {
            return false;
        }

        return true;
    }

    @Override
    public String getName() {
        return "ItemEnchanted";
    }

    @Override
    public boolean applyDetermination(ScriptContainer container, String determination) {
        if (ArgumentHelper.matchesInteger(determination)) {
            cost = ArgumentHelper.getIntegerFrom(determination);
            return true;
        }
        else if (CoreUtilities.toLowerCase(determination).startsWith("result:")) {
            String ditem = determination.substring("result:".length());
            item = dItem.valueOf(ditem, container);
            itemEdited = true;
            return true;
        }
        else if (CoreUtilities.toLowerCase(determination).startsWith("enchants:")) {
            String ditem = determination.substring("enchants:".length());
            enchantsRes = dItem.valueOf(ditem, container);
            return true;
        }
        else {
            return super.applyDetermination(container, determination);
        }
    }

    @Override
    public ScriptEntryData getScriptEntryData() {
        return new BukkitScriptEntryData(entity.isPlayer() ? entity.getDenizenPlayer() : null,
                entity.isCitizensNPC() ? entity.getDenizenNPC() : null);
    }

    @Override
    public ObjectTag getContext(String name) {
        if (name.equals("entity")) {
            return entity;
        }
        else if (name.equals("location")) {
            return location;
        }
        else if (name.equals("inventory")) {
            return inventory;
        }
        else if (name.equals("item")) {
            return item;
        }
        else if (name.equals("button")) {
            return button;
        }
        else if (name.equals("cost")) {
            return new ElementTag(cost);
        }
        return super.getContext(name);
    }

    @EventHandler
    public void onItemEnchanted(EnchantItemEvent event) {
        entity = new dEntity(event.getEnchanter());
        location = new dLocation(event.getEnchantBlock().getLocation());
        inventory = dInventory.mirrorBukkitInventory(event.getInventory());
        item = new dItem(event.getItem());
        button = new ElementTag(event.whichButton());
        cost = event.getExpLevelCost();
        itemEdited = false;
        this.event = event;
        enchantsRes = null;
        fire(event);
        event.setExpLevelCost(cost);
        if (itemEdited) {
            event.getItem().setItemMeta(item.getItemStack().getItemMeta());
        }
        if (enchantsRes != null) {
            event.getEnchantsToAdd().clear();
            event.getEnchantsToAdd().putAll(enchantsRes.getItemStack().getItemMeta().getEnchants());
        }
    }
}

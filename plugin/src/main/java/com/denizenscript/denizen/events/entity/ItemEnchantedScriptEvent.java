package com.denizenscript.denizen.events.entity;

import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizen.objects.InventoryTag;
import com.denizenscript.denizen.objects.ItemTag;
import com.denizenscript.denizen.objects.LocationTag;
import com.denizenscript.denizen.utilities.implementation.BukkitScriptEntryData;
import com.denizenscript.denizen.events.BukkitScriptEvent;
import com.denizenscript.denizencore.objects.core.ElementTag;
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
    //
    // @Switch in:<area> to only process the event if it occurred within a specified area.
    //
    // @Cancellable true
    //
    // @Triggers when an item is enchanted.
    //
    // @Context
    // <context.entity> returns the EntityTag of the enchanter (if applicable)
    // <context.location> returns the LocationTag of the enchanting table.
    // <context.inventory> returns the InventoryTag of the enchanting table.
    // <context.item> returns the ItemTag to be enchanted.
    // <context.button> returns which button was pressed to initiate the enchanting.
    // <context.cost> returns the experience level cost of the enchantment.
    //
    // @Determine
    // Element(Number) to set the experience level cost of the enchantment.
    // "RESULT:" + ItemTag to change the item result (only affects metadata (like enchantments), not material/quantity/etc!).
    // "ENCHANTS:" + ItemTag to change the resultant enchantments based on a ItemTag.
    // -->

    public ItemEnchantedScriptEvent() {
        instance = this;
    }

    public static ItemEnchantedScriptEvent instance;
    public EntityTag entity;
    public LocationTag location;
    public InventoryTag inventory;
    public ItemTag item;
    public ElementTag button;
    public int cost;
    public EnchantItemEvent event;
    public boolean itemEdited;
    public ItemTag enchantsRes;

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
    public boolean applyDetermination(ScriptPath path, ObjectTag determinationObj) {
        if (determinationObj instanceof ElementTag) {
            if (((ElementTag) determinationObj).isInt()) {
                cost = ((ElementTag) determinationObj).asInt();
                return true;
            }
            String determination = determinationObj.toString();
            String lower = CoreUtilities.toLowerCase(determination);
            if (lower.startsWith("result:")) {
                String itemText = determination.substring("result:".length());
                item = ItemTag.valueOf(itemText, path.container);
                itemEdited = true;
                return true;
            }
            else if (lower.startsWith("enchants:")) {
                String itemText = determination.substring("enchants:".length());
                enchantsRes = ItemTag.valueOf(itemText, path.container);
                return true;
            }
        }
        return super.applyDetermination(path, determinationObj);
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
        entity = new EntityTag(event.getEnchanter());
        location = new LocationTag(event.getEnchantBlock().getLocation());
        inventory = InventoryTag.mirrorBukkitInventory(event.getInventory());
        item = new ItemTag(event.getItem());
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

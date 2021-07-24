package com.denizenscript.denizen.events.item;

import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizen.objects.InventoryTag;
import com.denizenscript.denizen.objects.ItemTag;
import com.denizenscript.denizen.objects.LocationTag;
import com.denizenscript.denizen.utilities.Utilities;
import com.denizenscript.denizen.utilities.implementation.BukkitScriptEntryData;
import com.denizenscript.denizen.events.BukkitScriptEvent;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.core.MapTag;
import com.denizenscript.denizencore.scripts.ScriptEntryData;
import com.denizenscript.denizencore.utilities.CoreUtilities;
import com.denizenscript.denizencore.utilities.text.StringHolder;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.enchantment.EnchantItemEvent;

import java.util.Map;

public class ItemEnchantedScriptEvent extends BukkitScriptEvent implements Listener {

    // <--[event]
    // @Events
    // item enchanted
    // <item> enchanted
    //
    // @Regex ^on [^\s]+ enchanted$
    //
    // @Group Item
    //
    // @Location true
    //
    // @Cancellable true
    //
    // @Switch enchant:<name> to only process the event if any of the enchantments being added match the given name.
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
    // <context.enchants> returns a MapTag of enchantment names to the level that will be applied.
    //
    // @Determine
    // ElementTag(Number) to set the experience level cost of the enchantment.
    // "RESULT:" + ItemTag to change the item result (only affects metadata (like enchantments), not material/quantity/etc!).
    // "ENCHANTS:" + MapTag to change the resultant enchantments.
    //
    // @Player when the enchanter is a player.
    //
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

    @Override
    public boolean couldMatch(ScriptPath path) {
        if (!path.eventArgLowerAt(1).equals("enchanted")) {
            return false;
        }
        if (!couldMatchItem(path.eventArgLowerAt(0))) {
            return false;
        }
        return true;
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
        if (path.switches.containsKey("enchant")) {
            boolean any = false;
            for (Enchantment enchant : event.getEnchantsToAdd().keySet()) {
                any = runGenericSwitchCheck(path, "enchant", enchant.getKey().getKey());
                if (any) {
                    break;
                }
            }
            if (!any) {
                return false;
            }
        }
        return super.matches(path);
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
                event.setExpLevelCost(cost);
                return true;
            }
            String determination = determinationObj.toString();
            String lower = CoreUtilities.toLowerCase(determination);
            if (lower.startsWith("result:")) {
                String itemText = determination.substring("result:".length());
                item = ItemTag.valueOf(itemText, path.container);
                event.getItem().setItemMeta(item.getItemMeta());
                return true;
            }
            else if (lower.startsWith("enchants:")) {
                event.getEnchantsToAdd().clear();
                String itemText = determination.substring("enchants:".length());
                if (itemText.startsWith("map@")) {
                    MapTag map = MapTag.valueOf(itemText, getTagContext(path));
                    for (Map.Entry<StringHolder, ObjectTag> enchantments : map.map.entrySet()) {
                        event.getEnchantsToAdd().put(Utilities.getEnchantmentByName(enchantments.getKey().low), enchantments.getValue().asType(ElementTag.class, getTagContext(path)).asInt());
                    }
                }
                else {
                    ItemTag enchantsRes = ItemTag.valueOf(itemText, path.container);
                    event.getEnchantsToAdd().putAll(enchantsRes.getItemMeta().getEnchants());
                }
                return true;
            }
        }
        return super.applyDetermination(path, determinationObj);
    }

    @Override
    public ScriptEntryData getScriptEntryData() {
        return new BukkitScriptEntryData(entity);
    }

    @Override
    public ObjectTag getContext(String name) {
        switch (name) {
            case "entity":
                return entity;
            case "location":
                return location;
            case "inventory":
                return inventory;
            case "item":
                return item;
            case "button":
                return button;
            case "cost":
                return new ElementTag(cost);
            case "enchants":
                MapTag map = new MapTag();
                for (Map.Entry<Enchantment, Integer> enchant : event.getEnchantsToAdd().entrySet()) {
                    map.putObject(enchant.getKey().getKey().getKey(), new ElementTag(enchant.getValue()));
                }
                return map;
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
        this.event = event;
        fire(event);
    }
}

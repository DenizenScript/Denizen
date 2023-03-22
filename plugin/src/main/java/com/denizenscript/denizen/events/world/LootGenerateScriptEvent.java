package com.denizenscript.denizen.events.world;

import com.denizenscript.denizen.events.BukkitScriptEvent;
import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizen.objects.InventoryTag;
import com.denizenscript.denizen.objects.ItemTag;
import com.denizenscript.denizen.utilities.implementation.BukkitScriptEntryData;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.core.ListTag;
import com.denizenscript.denizencore.scripts.ScriptEntryData;
import com.denizenscript.denizencore.utilities.CoreUtilities;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.LootGenerateEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;

public class LootGenerateScriptEvent extends BukkitScriptEvent implements Listener {

    // <--[event]
    // @Events
    // loot generates
    //
    // @Group World
    //
    // @Location true
    //
    // @Switch for:<type> to only process the event if a certain inventory type is receiving loot (like 'for:chest').
    //
    // @Cancellable true
    //
    // @Triggers when loot is generated somewhere in the world (like a vanilla chest being opened for the first time).
    //
    // @Context
    // <context.entity> returns an entity that caused loot generation, if any.
    // <context.inventory> returns the InventoryTag that loot is generating into.
    // <context.items> returns a ListTag of the items being generated.
    // <context.loot_table_id> returns an element indicating the minecraft key for the loot-table that was generated.
    //
    // @Determine
    // "LOOT:<ListTag(ItemTag)>" to change the list of items that will generate as loot.
    //
    // @Player when the linked entity is a player.
    //
    // -->

    public LootGenerateScriptEvent() {
        registerCouldMatcher("loot generates");
        registerSwitches("for");
    }

    public LootGenerateEvent event;

    @Override
    public boolean matches(ScriptPath path) {
        if (!runInCheck(path, event.getLootContext().getLocation())) {
            return false;
        }
        if (path.switches.containsKey("for")) {
            if (event.getInventoryHolder() == null || !runGenericSwitchCheck(path, "for", event.getInventoryHolder().getInventory().getType().name())) {
                return false;
            }
        }
        return super.matches(path);
    }

    @Override
    public ScriptEntryData getScriptEntryData() {
        return new BukkitScriptEntryData(event.getEntity() != null ? new EntityTag(event.getEntity()) : null);
    }

    @Override
    public boolean applyDetermination(ScriptPath path, ObjectTag determinationObj) {
        if (determinationObj instanceof ElementTag) {
            String determination = determinationObj.toString();
            String determinationLower = CoreUtilities.toLowerCase(determination);
            if (determinationLower.startsWith("loot:")) {
                ListTag list = ListTag.valueOf(determination.substring("loot:".length()), getTagContext(path));
                ArrayList<ItemStack> newLoot = new ArrayList<>(list.size());
                for (ItemTag item : list.filter(ItemTag.class, getTagContext(path))) {
                    newLoot.add(item.getItemStack());
                }
                event.setLoot(newLoot);
                return true;
            }
        }
        return super.applyDetermination(path, determinationObj);
    }

    @Override
    public ObjectTag getContext(String name) {
        if (name.equals("inventory") && event.getInventoryHolder() != null) {
            return InventoryTag.mirrorBukkitInventory(event.getInventoryHolder().getInventory());
        }
        else if (name.equals("entity") && event.getEntity() != null) {
            return new EntityTag(event.getEntity()).getDenizenObject();
        }
        else if (name.equals("items")) {
            ListTag result = new ListTag();
            for (ItemStack item : event.getLoot()) {
                result.addObject(new ItemTag(item));
            }
            return result;
        }
        else if (name.equals("loot_table_id")) {
            return new ElementTag(event.getLootTable().getKey().toString());
        }
        return super.getContext(name);
    }

    @EventHandler
    public void onLootGenerate(LootGenerateEvent event) {
        this.event = event;
        fire(event);
    }
}

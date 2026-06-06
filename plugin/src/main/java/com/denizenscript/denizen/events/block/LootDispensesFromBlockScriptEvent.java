package com.denizenscript.denizen.events.block;

import com.denizenscript.denizen.events.BukkitScriptEvent;
import com.denizenscript.denizen.objects.*;
import com.denizenscript.denizen.utilities.implementation.BukkitScriptEntryData;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.core.ListTag;
import com.denizenscript.denizencore.scripts.ScriptEntryData;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockDispenseLootEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class LootDispensesFromBlockScriptEvent extends BukkitScriptEvent implements Listener {

    // <--[event]
    // @Events
    // loot dispenses from <block>
    //
    // @Group Block
    //
    // @Location true
    //
    // @Cancellable true
    //
    // @Player when the loot dispensing is triggered by a player.
    //
    // @Triggers when a block dispenses loot containing multiple items.
    //
    // @Context
    // <context.loot> returns a ListTag(ItemTag) of loot items.
    // <context.location> returns a LocationTag of the block that is dispensing the items.
    //
    // @Determine
    // "LOOT:<ListTag(ItemTag)>" to set the loot items being dispensed.
    //
    // -->

    public LootDispensesFromBlockScriptEvent() {
        registerCouldMatcher("loot dispenses from <block>");
        this.<LootDispensesFromBlockScriptEvent, ListTag>registerDetermination("loot", ListTag.class, (evt, context, input) -> {
            List<ItemStack> items = new ArrayList<>(input.size());
            for (ItemTag item : input.filter(ItemTag.class, context)) {
                items.add(item.getItemStack());
            }
            evt.event.setDispensedLoot(items);
        });
    }

    public MaterialTag block;
    public LocationTag location;
    public BlockDispenseLootEvent event;

    @Override
    public boolean matches(ScriptPath path) {
        if (!path.tryArgObject(3, block)) {
            return false;
        }
        if (!runInCheck(path, location)) {
            return false;
        }
        return super.matches(path);
    }

    @Override
    public ScriptEntryData getScriptEntryData() {
        return new BukkitScriptEntryData(event.getPlayer());
    }

    @Override
    public ObjectTag getContext(String name) {
        return switch (name) {
            case "loot" -> new ListTag(event.getDispensedLoot(), ItemTag::new);
            case "location" -> location;
            default -> super.getContext(name);
        };
    }

    @EventHandler
    public void onLootDispensesFromBlock(BlockDispenseLootEvent event) {
        block = new MaterialTag(event.getBlock().getType());
        location = new LocationTag(event.getBlock().getLocation());
        this.event = event;
        fire(event);
    }
}

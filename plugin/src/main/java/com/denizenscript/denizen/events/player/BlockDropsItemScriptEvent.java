package com.denizenscript.denizen.events.player;

import com.denizenscript.denizen.events.BukkitScriptEvent;
import com.denizenscript.denizen.objects.*;
import com.denizenscript.denizen.utilities.implementation.BukkitScriptEntryData;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.core.ListTag;
import com.denizenscript.denizencore.scripts.ScriptEntryData;
import org.bukkit.entity.Item;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockDropItemEvent;

import java.util.ArrayList;
import java.util.List;

public class BlockDropsItemScriptEvent extends BukkitScriptEvent implements Listener {

    // <--[event]
    // @Events
    // block drops item from breaking
    // <block> drops <item> from breaking
    //
    // @Regex ^on [^\s]+ drops [^\s]+ from breaking$
    //
    // @Group Player
    //
    // @Location true
    //
    // @Cancellable true
    //
    // @Triggers when a items drop from a block due to a player breaking the block in survival mode.
    //
    // @Context
    // <context.location> returns the LocationTag the block was broken at.
    // <context.material> returns the MaterialTag of the block that was broken.
    // <context.drop_entities> returns a ListTag of EntityTags of type DROPPED_ITEM. To get the list of ItemTags, just tack ".parse[item]" onto this context tag.
    //
    // @Player Always.
    //
    // -->

    public BlockDropsItemScriptEvent() {
    }

    public LocationTag location;
    public MaterialTag material;
    public BlockDropItemEvent event;

    @Override
    public boolean couldMatch(ScriptPath path) {
        if (!path.eventArgLowerAt(1).equals("drops") || !path.eventArgsLowEqualStartingAt(3, "from", "breaking")) {
            return false;
        }
        if (!couldMatchBlock(path.eventArgLowerAt(0))) {
            return false;
        }
        if (!couldMatchItem(path.eventArgLowerAt(2))) {
            return false;
        }
        return true;
    }

    @Override
    public boolean matches(ScriptPath path) {
        if (!path.tryArgObject(0, material)) {
            return false;
        }
        String item = path.eventArgLowerAt(2);
        if (!item.equals("item")) {
            boolean anyMatch = false;
            for (Item itemEnt : event.getItems()) {
                if (new ItemTag(itemEnt.getItemStack()).tryAdvancedMatcher(item)) {
                    anyMatch = true;
                    break;
                }
            }
            if (!anyMatch) {
                return false;
            }
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
        switch (name) {
            case "location":
                return location;
            case "material":
                return material;
            case "drop_entities":
                ListTag toRet = new ListTag();
                for (Item item : event.getItems()) {
                    toRet.addObject(new EntityTag(item));
                }
                return toRet;
        }
        return super.getContext(name);
    }

    @EventHandler
    public void onBlockDropsItem(BlockDropItemEvent event) {
        if (EntityTag.isNPC(event.getPlayer())) {
            return;
        }
        material = new MaterialTag(event.getBlockState());
        location = new LocationTag(event.getBlock().getLocation());
        List<Item> items = new ArrayList<>(event.getItems());
        for (Item item : items) {
            EntityTag.rememberEntity(item);
        }
        this.event = event;
        fire(event);
        for (Item item : items) {
            EntityTag.forgetEntity(item);
        }
    }
}

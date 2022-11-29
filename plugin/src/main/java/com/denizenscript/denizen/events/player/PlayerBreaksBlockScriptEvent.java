package com.denizenscript.denizen.events.player;

import com.denizenscript.denizen.objects.*;
import com.denizenscript.denizen.utilities.implementation.BukkitScriptEntryData;
import com.denizenscript.denizen.events.BukkitScriptEvent;
import com.denizenscript.denizencore.objects.*;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.core.ListTag;
import com.denizenscript.denizencore.scripts.ScriptEntryData;
import com.denizenscript.denizencore.utilities.CoreUtilities;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

import java.util.Arrays;
import java.util.HashSet;

public class PlayerBreaksBlockScriptEvent extends BukkitScriptEvent implements Listener {

    // <--[event]
    // @Events
    // player breaks block
    // player breaks <material>
    //
    // @Regex ^on player breaks [^\s]+$
    //
    // @Synonyms player mines block,player mines ore,player digs block
    //
    // @Group Player
    //
    // @Location true
    // @Switch with:<item> to only process the event when the player is breaking the block with a specified item.
    //
    // @Cancellable true
    //
    // @Triggers when a player breaks a block.
    //
    // @Context
    // <context.location> returns the LocationTag the block was broken at.
    // <context.material> returns the MaterialTag of the block that was broken.
    // <context.xp> returns how much XP will be dropped.
    // <context.should_drop_items> returns whether the event will drop items.
    //
    // @Determine
    // "NOTHING" to make the block drop no items.
    // ListTag(ItemTag) to make the block drop a specified list of items.
    // ElementTag(Number) to set the amount of xp to drop.
    //
    // @Player Always.
    //
    // -->

    public PlayerBreaksBlockScriptEvent() {
    }

    public LocationTag location;
    public MaterialTag material;
    public BlockBreakEvent event;

    public static HashSet<String> notRelevantBreakables = new HashSet<>(Arrays.asList("item", "held", "hanging", "painting", "item_frame", "leash_hitch"));

    @Override
    public boolean couldMatch(ScriptPath path) {
        if (!path.eventLower.startsWith("player breaks")) {
            return false;
        }
        if (notRelevantBreakables.contains(path.eventArgLowerAt(2))) {
            return false;
        }
        if (!couldMatchBlock(path.eventArgLowerAt(2))) {
            return false;
        }
        return true;
    }

    @Override
    public boolean matches(ScriptPath path) {
        String mat = path.eventArgLowerAt(2);
        if (!material.tryAdvancedMatcher(mat)) {
            return false;
        }
        if (!runInCheck(path, location)) {
            return false;
        }
        if (!runWithCheck(path, new ItemTag(event.getPlayer().getEquipment().getItemInMainHand()))) {
            return false;
        }
        // Deprecated in favor of with: format
        if (path.eventArgLowerAt(3).equals("with")
                && !path.tryArgObject(4, new ItemTag(event.getPlayer().getEquipment().getItemInMainHand()))) {
            return false;
        }
        return super.matches(path);
    }

    @Override
    public boolean applyDetermination(ScriptPath path, ObjectTag determinationObj) {
        String determination = determinationObj.toString();
        Block block = event.getBlock();
        if (determinationObj instanceof ElementTag) {
            String lower = CoreUtilities.toLowerCase(determination);
            if (lower.equals("nothing")) {
                event.setExpToDrop(0);
                event.setDropItems(false);
                return true;
            }
            else if (((ElementTag) determinationObj).isInt()) {
                event.setExpToDrop(((ElementTag) determinationObj).asInt());
                return true;
            }
        }
        if (Argument.valueOf(determination).matchesArgumentList(ItemTag.class)) {
            event.setDropItems(false);
            for (ItemTag newItem : ListTag.valueOf(determination, getTagContext(path)).filter(ItemTag.class, path.container, true)) {
                block.getWorld().dropItemNaturally(block.getLocation(), newItem.getItemStack()); // Drop each item
            }
            return true;
        }
        return super.applyDetermination(path, determinationObj);
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
            case "xp":
                return new ElementTag(event.getExpToDrop());
            case "should_drop_items":
                return new ElementTag(event.isDropItems());
        }
        return super.getContext(name);
    }

    @EventHandler
    public void onPlayerBreaksBlock(BlockBreakEvent event) {
        if (EntityTag.isNPC(event.getPlayer())) {
            return;
        }
        material = new MaterialTag(event.getBlock());
        location = new LocationTag(event.getBlock().getLocation());
        this.event = event;
        fire(event);
    }
}

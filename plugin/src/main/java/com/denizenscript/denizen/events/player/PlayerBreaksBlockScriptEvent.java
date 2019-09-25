package com.denizenscript.denizen.events.player;

import com.denizenscript.denizen.objects.*;
import com.denizenscript.denizen.BukkitScriptEntryData;
import com.denizenscript.denizen.events.BukkitScriptEvent;
import com.denizenscript.denizencore.objects.*;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.core.ListTag;
import com.denizenscript.denizencore.scripts.ScriptEntryData;
import com.denizenscript.denizencore.utilities.CoreUtilities;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

public class PlayerBreaksBlockScriptEvent extends BukkitScriptEvent implements Listener {

    // <--[event]
    // @Events
    // player breaks block
    // player breaks <material>
    //
    // @Regex ^on player breaks [^\s]+$
    //
    // @Switch in <area>
    // @Switch with <item>
    //
    // @Cancellable true
    //
    // @Triggers when a player breaks a block.
    //
    // @Context
    // <context.location> returns the LocationTag the block was broken at.
    // <context.material> returns the MaterialTag of the block that was broken.
    // <context.xp> returns how much XP will be dropped.
    //
    // @Determine
    // "NOTHING" to make the block drop no items.
    // ListTag(ItemTag) to make the block drop a specified list of items.
    // Element(Number) to set the amount of xp to drop.
    //
    // -->

    public PlayerBreaksBlockScriptEvent() {
        instance = this;
    }

    public static PlayerBreaksBlockScriptEvent instance;
    public LocationTag location;
    public MaterialTag material;
    public BlockBreakEvent event;

    @Override
    public boolean couldMatch(ScriptPath path) {
        if (path.eventArgLowerAt(2).equals("item") || path.eventArgLowerAt(3).equals("held")) {
            return false;
        }
        return path.eventLower.startsWith("player breaks");
    }

    @Override
    public boolean matches(ScriptPath path) {
        String mat = path.eventArgLowerAt(2);
        if (!tryMaterial(material, mat)) {
            return false;
        }
        if (!runInCheck(path, location)) {
            return false;
        }
        if (!runWithCheck(path, new ItemTag(event.getPlayer().getItemInHand()))) {
            return false;
        }
        // Deprecated in favor of with: format
        if (path.eventArgLowerAt(3).equals("with")
                && !tryItem(new ItemTag(event.getPlayer().getItemInHand()), path.eventArgLowerAt(4))) {
            return false;
        }
        return true;
    }

    @Override
    public String getName() {
        return "PlayerBreaksBlock";
    }

    @Override
    public boolean applyDetermination(ScriptPath path, ObjectTag determinationObj) {
        String determination = determinationObj.toString();
        Block block = event.getBlock();
        if (determinationObj instanceof ElementTag) {
            String lower = CoreUtilities.toLowerCase(determination);
            if (lower.equals("nothing")) {
                cancelled = true;
                block.setType(Material.AIR);
                return true;
            }
            else if (((ElementTag) determinationObj).isInt()) {
                event.setExpToDrop(((ElementTag) determinationObj).asInt());
                return true;
            }
        }
        if (Argument.valueOf(determination).matchesArgumentList(ItemTag.class)) {
            cancelled = true;
            block.setType(Material.AIR);
            for (ItemTag newItem : ListTag.valueOf(determination).filter(ItemTag.class, path.container, true)) {
                block.getWorld().dropItemNaturally(block.getLocation(), newItem.getItemStack()); // Drop each item
            }
            return true;
        }
        return super.applyDetermination(path, determinationObj);
    }

    @Override
    public ScriptEntryData getScriptEntryData() {
        return new BukkitScriptEntryData(PlayerTag.mirrorBukkitPlayer(event.getPlayer()), null);
    }

    @Override
    public ObjectTag getContext(String name) {
        if (name.equals("location")) {
            return location;
        }
        else if (name.equals("material")) {
            return material;
        }
        else if (name.equals("xp")) {
            return new ElementTag(event.getExpToDrop());
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

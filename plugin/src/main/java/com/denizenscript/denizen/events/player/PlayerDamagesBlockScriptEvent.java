package com.denizenscript.denizen.events.player;

import com.denizenscript.denizen.objects.*;
import com.denizenscript.denizen.utilities.implementation.BukkitScriptEntryData;
import com.denizenscript.denizen.events.BukkitScriptEvent;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.scripts.ScriptEntryData;
import com.denizenscript.denizencore.utilities.CoreUtilities;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockDamageEvent;

public class PlayerDamagesBlockScriptEvent extends BukkitScriptEvent implements Listener {

    // <--[event]
    // @Events
    // player damages block
    // player damages <material>
    //
    // @Regex ^on player damages [^\s]+$
    //
    // @Group Player
    //
    // @Location true
    // @Switch with:<item> to only process the event when the player is hitting the block with a specified item.
    //
    // @Cancellable true
    //
    // @Triggers when a block is damaged by a player.
    //
    // @Context
    // <context.location> returns the LocationTag the block that was damaged.
    // <context.material> returns the MaterialTag of the block that was damaged.
    //
    // @Determine
    // "INSTABREAK" to make the block get broken instantly.
    //
    // @Player Always.
    //
    // -->

    public PlayerDamagesBlockScriptEvent() {
        instance = this;
    }

    public static PlayerDamagesBlockScriptEvent instance;
    public LocationTag location;
    public MaterialTag material;
    public BlockDamageEvent event;

    @Override
    public boolean couldMatch(ScriptPath path) {
        if (!path.eventLower.startsWith("player damages")) {
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
        return super.matches(path);
    }

    @Override
    public String getName() {
        return "PlayerDamagesBlock";
    }

    @Override
    public boolean applyDetermination(ScriptPath path, ObjectTag determinationObj) {
        if (determinationObj instanceof ElementTag) {
            if (CoreUtilities.equalsIgnoreCase(determinationObj.toString(), "instabreak")) {
                event.setInstaBreak(true);
                return true;
            }
        }
        return super.applyDetermination(path, determinationObj);
    }

    @Override
    public ScriptEntryData getScriptEntryData() {
        return new BukkitScriptEntryData(event.getPlayer());
    }

    @Override
    public ObjectTag getContext(String name) {
        if (name.equals("location")) {
            return location;
        }
        else if (name.equals("material")) {
            return material;
        }
        return super.getContext(name);
    }

    @EventHandler
    public void onPlayerDamagesBlock(BlockDamageEvent event) {
        if (EntityTag.isNPC(event.getPlayer())) {
            return;
        }
        material = new MaterialTag(event.getBlock());
        location = new LocationTag(event.getBlock().getLocation());
        this.event = event;
        fire(event);
    }
}

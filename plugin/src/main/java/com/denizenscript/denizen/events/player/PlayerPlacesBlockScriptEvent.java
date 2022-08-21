package com.denizenscript.denizen.events.player;

import com.denizenscript.denizen.objects.*;
import com.denizenscript.denizen.utilities.implementation.BukkitScriptEntryData;
import com.denizenscript.denizen.events.BukkitScriptEvent;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.scripts.ScriptEntryData;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;

public class PlayerPlacesBlockScriptEvent extends BukkitScriptEvent implements Listener {

    // <--[event]
    // @Events
    // player places block
    // player places <item>
    //
    // @Regex ^on player places [^\s]+$
    //
    // @Group Player
    //
    // @Location true
    // @Switch using:<hand type> to only process the event if the player is using the specified hand type (HAND or OFF_HAND).
    //
    // @Cancellable true
    //
    // @Triggers when a player places a block.
    //
    // @Context
    // <context.location> returns the LocationTag of the block that was placed.
    // <context.material> returns the MaterialTag of the block that was placed.
    // <context.old_material> returns the MaterialTag of the block that was replaced.
    // <context.item_in_hand> returns the ItemTag of the item in hand.
    // <context.hand> returns the name of the hand that the block was in (HAND or OFF_HAND).
    //
    // @Player Always.
    //
    // -->

    public PlayerPlacesBlockScriptEvent() {
        instance = this;
    }

    public static PlayerPlacesBlockScriptEvent instance;
    public LocationTag location;
    public MaterialTag material;
    public ElementTag hand;
    public ItemTag item_in_hand;
    public BlockPlaceEvent event;

    @Override
    public boolean couldMatch(ScriptPath path) {
        if (!path.eventLower.startsWith("player places")) {
            return false;
        }
        if (!couldMatchBlockOrItem(path.eventArgLowerAt(2))) {
            return false;
        }
        return true;
    }

    @Override
    public boolean matches(ScriptPath path) {
        String mat = path.eventArgLowerAt(2);
        if (!item_in_hand.tryAdvancedMatcher(mat) && !material.tryAdvancedMatcher(mat)) {
            return false;
        }
        if (!runGenericSwitchCheck(path, "using", hand.asString())) {
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
        switch (name) {
            case "location":
                return location;
            case "material":
                return material;
            case "old_material":
                return new MaterialTag(event.getBlockReplacedState());
            case "item_in_hand":
                return item_in_hand;
            case "hand":
                return hand;
        }
        return super.getContext(name);
    }

    @EventHandler
    public void onPlayerPlacesBlock(BlockPlaceEvent event) {
        if (EntityTag.isNPC(event.getPlayer())) {
            return;
        }
        hand = new ElementTag(event.getHand().name());
        material = new MaterialTag(event.getBlock());
        location = new LocationTag(event.getBlock().getLocation());
        item_in_hand = new ItemTag(event.getItemInHand());
        this.event = event;
        fire(event);
    }
}

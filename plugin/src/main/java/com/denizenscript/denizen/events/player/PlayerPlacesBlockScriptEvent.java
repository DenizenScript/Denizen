package com.denizenscript.denizen.events.player;

import com.denizenscript.denizen.events.BukkitScriptEvent;
import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizen.objects.ItemTag;
import com.denizenscript.denizen.objects.LocationTag;
import com.denizenscript.denizen.objects.MaterialTag;
import com.denizenscript.denizen.utilities.implementation.BukkitScriptEntryData;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
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
    // @Group Player
    //
    // @Location true
    //
    // @Switch using:<hand_type> to only process the event if the player is using the specified hand type (HAND or OFF_HAND).
    // @Switch against:<location> to only process the event if block that this new block is being placed against matches the specified LocationTag matcher.
    // @Switch type:<material> to only process the event if the block placed matches the MaterialTag matcher input.
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
    // <context.against> returns the LocationTag of the block this block was placed against.
    //
    // @Player Always.
    //
    // @Example
    // on player places block:
    //
    // @Example
    // after player places torch using:off_hand:
    //
    // @Example
    // on player places cactus against:sand:
    //
    // @Example
    // # This example process the event only if the player places any block that isn't tnt.
    // on player places block type:!tnt:
    // - announce "<player.name> has placed a block that isn't TNT. Lucky!"
    //
    // -->

    public PlayerPlacesBlockScriptEvent() {
        registerCouldMatcher("player places <material>");
        registerSwitches("using", "against", "type");
    }

    public BlockPlaceEvent event;
    public LocationTag location, against;
    public MaterialTag material;
    public ElementTag hand;
    public ItemTag item_in_hand;

    @Override
    public boolean matches(ScriptPath path) {
        String mat = path.eventArgLowerAt(2);
        if (!item_in_hand.tryAdvancedMatcher(mat, path.context) && !material.tryAdvancedMatcher(mat, path.context)) {
            return false;
        }
        if (!runGenericSwitchCheck(path, "using", hand.asString())) {
            return false;
        }
        if (!runInCheck(path, location)) {
            return false;
        }
        if (!path.tryObjectSwitch("against", against)) {
            return false;
        }
        if (!path.tryObjectSwitch("type", material)) {
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
            case "location": return location;
            case "material": return material;
            case "old_material": return new MaterialTag(event.getBlockReplacedState());
            case "item_in_hand": return item_in_hand;
            case "hand": return hand;
            case "against": return against;
        }
        return super.getContext(name);
    }

    @EventHandler
    public void onPlayerPlacesBlock(BlockPlaceEvent event) {
        if (EntityTag.isNPC(event.getPlayer())) {
            return;
        }
        hand = new ElementTag(event.getHand());
        material = new MaterialTag(event.getBlock());
        location = new LocationTag(event.getBlock().getLocation());
        against = new LocationTag(event.getBlockAgainst().getLocation());
        item_in_hand = new ItemTag(event.getItemInHand());
        this.event = event;
        fire(event);
    }
}

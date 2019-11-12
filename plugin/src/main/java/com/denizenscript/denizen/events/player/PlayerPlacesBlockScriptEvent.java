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
    // player places <material>
    //
    // @Regex ^on player places [^\s]+$
    //
    // @Switch in:<area> to only process the event if it occurred within a specified area.
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
        String mat = path.eventArgLowerAt(2);
        return path.eventLower.startsWith("player places")
                && (!mat.equals("hanging") && !mat.equals("painting") && !mat.equals("item_frame") && !mat.equals("leash_hitch"));
    }

    @Override
    public boolean matches(ScriptPath path) {

        String mat = path.eventArgLowerAt(2);
        if (!tryItem(item_in_hand, mat) && !tryMaterial(material, mat)) {
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
    public String getName() {
        return "PlayerPlacesBlock";
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
        else if (name.equals("old_material")) {
            return new MaterialTag(event.getBlockReplacedState());
        }
        else if (name.equals("item_in_hand")) {
            return item_in_hand;
        }
        else if (name.equals("hand")) {
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

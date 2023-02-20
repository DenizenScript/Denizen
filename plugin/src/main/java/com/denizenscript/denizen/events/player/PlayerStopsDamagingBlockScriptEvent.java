package com.denizenscript.denizen.events.player;

import com.denizenscript.denizen.events.BukkitScriptEvent;
import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizen.objects.ItemTag;
import com.denizenscript.denizen.objects.LocationTag;
import com.denizenscript.denizen.objects.MaterialTag;
import com.denizenscript.denizen.utilities.implementation.BukkitScriptEntryData;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.scripts.ScriptEntryData;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockDamageAbortEvent;

public class PlayerStopsDamagingBlockScriptEvent extends BukkitScriptEvent implements Listener {

    // <--[event]
    // @Events
    // player stops damaging <block>
    //
    // @Group Player
    //
    // @Location true
    //
    // @Triggers when a block stops being damaged by a player.
    //
    // @Switch with:<item> to only process the event when the player stops hitting the block with a specified item.
    //
    // @Context
    // <context.location> returns the LocationTag the block no longer being damaged.
    // <context.material> returns the MaterialTag of the block no longer being damaged.
    //
    // @Player Always.
    //
    // @Example
    // on player stops damaging block:
    // - narrate "You were so close to breaking that block! You got this!"
    //
    // @Example
    // on player stops damaging infested*:
    // - narrate "It's Silverfish time!"
    // - spawn silverfish|silverfish|silverfish|silverfish|silverfish <context.location> persistent
    // -->

    public LocationTag location;
    public MaterialTag material;
    public BlockDamageAbortEvent event;

    public PlayerStopsDamagingBlockScriptEvent() {
        registerCouldMatcher("player stops damaging <block>");
        registerSwitches("with");
    }

    @Override
    public boolean matches(ScriptPath path) {
        if (!runInCheck(path, location)) {
            return false;
        }
        if (!path.tryArgObject(3, material)) {
            return false;
        }
        if (!runWithCheck(path, new ItemTag(event.getItemInHand()))) {
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
            case "location" -> location;
            case "material" -> material;
            default -> super.getContext(name);
        };
    }

    @EventHandler
    public void playerStopsDamagingBlockEvent(BlockDamageAbortEvent event) {
        if (EntityTag.isNPC(event.getPlayer())) {
            return;
        }
        location = new LocationTag(event.getBlock().getLocation());
        material = new MaterialTag(event.getBlock());
        this.event = event;
        fire(event);
    }
}

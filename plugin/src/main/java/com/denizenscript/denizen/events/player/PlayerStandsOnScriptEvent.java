package com.denizenscript.denizen.events.player;

import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizen.objects.LocationTag;
import com.denizenscript.denizen.objects.MaterialTag;
import com.denizenscript.denizen.utilities.implementation.BukkitScriptEntryData;
import com.denizenscript.denizen.events.BukkitScriptEvent;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.scripts.ScriptEntryData;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

public class PlayerStandsOnScriptEvent extends BukkitScriptEvent implements Listener {

    // <--[event]
    // @Events
    // player stands on material
    // player stands on (<material>)
    //
    // @Regex ^on player stands on [^\s]+$
    //
    // @Group Player
    //
    // @Location true
    //
    // @Cancellable true
    //
    // @Triggers when a player stands on a physical-interactable block (such as a pressure plate, tripwire, or redstone ore).
    // @Context
    // <context.location> returns the LocationTag the player is interacting with.
    // <context.material> returns the MaterialTag the player is interacting with.
    //
    // @Player Always.
    //
    // -->

    public PlayerStandsOnScriptEvent() {
    }

    PlayerStandsOnScriptEvent instance;
    PlayerInteractEvent event;
    LocationTag location;
    MaterialTag material;

    @Override
    public boolean couldMatch(ScriptPath path) {
        if (!path.eventLower.startsWith("player stands on")) {
            return false;
        }
        if (!couldMatchBlock(path.eventArgLowerAt(3))) {
            return false;
        }
        return true;
    }

    @Override
    public boolean matches(ScriptPath path) {
        String mat = path.eventArgLowerAt(3);
        if (mat.length() > 0 && !mat.equals("in") && !material.tryAdvancedMatcher(mat)) {
            return false;
        }
        if (!runInCheck(path, location)) {
            return false;
        }
        return super.matches(path);
    }

    @Override
    public ScriptEntryData getScriptEntryData() {
        return new BukkitScriptEntryData(EntityTag.getPlayerFrom(event.getPlayer()), null);
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
    public void playerStandsOn(PlayerInteractEvent event) {
        if (event.getAction() != Action.PHYSICAL) {
            return;
        }
        material = new MaterialTag(event.getClickedBlock());
        location = new LocationTag(event.getClickedBlock().getLocation());
        this.event = event;
        fire(event);
    }
}

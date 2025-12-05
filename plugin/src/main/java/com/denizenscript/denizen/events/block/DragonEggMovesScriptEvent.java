package com.denizenscript.denizen.events.block;

import com.denizenscript.denizen.objects.LocationTag;
import com.denizenscript.denizen.events.BukkitScriptEvent;
import com.denizenscript.denizen.objects.MaterialTag;
import com.denizenscript.denizencore.objects.ObjectTag;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockFromToEvent;

public class DragonEggMovesScriptEvent extends BukkitScriptEvent implements Listener {

    // <--[event]
    // @Events
    // dragon egg moves
    //
    // @Group Block
    //
    // @Location true
    //
    // @Cancellable true
    //
    // @Triggers when a dragon egg moves.
    //
    // @Context
    // <context.location> returns the LocationTag the egg started at.
    // <context.destination> returns the LocationTag the egg teleported to.
    //
    // -->

    public DragonEggMovesScriptEvent() {
        registerCouldMatcher("dragon egg moves");
    }

    public LocationTag location;
    public LocationTag destination;
    public BlockFromToEvent event;

    @Override
    public boolean matches(ScriptPath path) {
        if (!runInCheck(path, location) && !runInCheck(path, destination)) {
            return false;
        }
        return super.matches(path);
    }

    @Override
    public ObjectTag getContext(String name) {
        return switch (name) {
            case "location" -> location;
            case "destination" -> destination;
            case "material" -> new MaterialTag(Material.DRAGON_EGG); // for historical compatibility reasons
            default -> super.getContext(name);
        };
    }

    @EventHandler
    public void onDragonEggMove(BlockFromToEvent event) {
        if (event.getBlock().getType() != Material.DRAGON_EGG) { // BlockFromToEvent also fires with LiquidSpreadScriptEvent
            return;
        }
        destination = new LocationTag(event.getToBlock().getLocation());
        location = new LocationTag(event.getBlock().getLocation());
        this.event = event;
        fire(event);
    }
}

package com.denizenscript.denizen.events.block;

import com.denizenscript.denizen.objects.LocationTag;
import com.denizenscript.denizen.events.BukkitScriptEvent;
import com.denizenscript.denizen.utilities.BukkitImplDeprecations;
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
    // <context.old_location> returns the LocationTag the egg started at.
    // <context.new_location> returns the LocationTag the egg teleported to.
    //
    // -->

    public DragonEggMovesScriptEvent() {
        registerCouldMatcher("dragon egg moves");
    }

    public LocationTag old_location;
    public LocationTag new_location;
    public BlockFromToEvent event;

    @Override
    public boolean matches(ScriptPath path) {
        if (event.getBlock().getType() != Material.DRAGON_EGG) { // BlockFromToEvent also fires with LiquidSpreadScriptEvent
            return false;
        }
        if (!runInCheck(path, old_location) && !runInCheck(path, new_location)) {
            return false;
        }
        return super.matches(path);
    }

    @Override
    public ObjectTag getContext(String name) {
        return switch (name) {
            case "old_location" -> old_location;
            case "new_location" -> new_location;
            case "location" -> {
                BukkitImplDeprecations.dragonEggMoveEventContexts.warn();
                yield old_location;
            }
            case "destination" -> {
                BukkitImplDeprecations.dragonEggMoveEventContexts.warn();
                yield new_location;
            }
            default -> super.getContext(name);
        };
    }

    @EventHandler
    public void onDragonEggMove(BlockFromToEvent event) {
        new_location = new LocationTag(event.getToBlock().getLocation());
        old_location = new LocationTag(event.getBlock().getLocation());
        this.event = event;
        fire(event);
    }
}

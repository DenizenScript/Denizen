package com.denizenscript.denizen.events.block;

import com.denizenscript.denizen.objects.LocationTag;
import com.denizenscript.denizen.objects.MaterialTag;
import com.denizenscript.denizen.events.BukkitScriptEvent;
import com.denizenscript.denizen.utilities.BukkitImplDeprecations;
import com.denizenscript.denizencore.objects.ObjectTag;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockFromToEvent;

public class LiquidSpreadScriptEvent extends BukkitScriptEvent implements Listener {

    // <--[event]
    // @Events
    // liquid spreads
    //
    // @Switch type:<block> to only run if the block spreading matches the material input.
    //
    // @Group Block
    //
    // @Location true
    //
    // @Cancellable true
    //
    // @Triggers when a liquid block spreads.
    //
    // @Context
    // <context.destination> returns the LocationTag the block spread to.
    // <context.source> returns the LocationTag the source of the liquid spreading.
    // <context.material> returns the MaterialTag of the block that was spread.
    //
    // -->

    public LiquidSpreadScriptEvent() {
        registerCouldMatcher("liquid spreads");
        registerSwitches("type");
    }

    public MaterialTag material;
    public LocationTag location;
    public LocationTag destination;
    public BlockFromToEvent event;

    @Override
    public boolean matches(ScriptPath path) {
        if (event.getBlock().getType() == Material.DRAGON_EGG) { // BlockFromToEvent also fires with DragonEggMovesScriptEvent
            return false;
        }
        if (!path.tryObjectSwitch("type", material)) {
            return false;
        }
        if (!runInCheck(path, location) && !runInCheck(path, destination)) {
            return false;
        }
        return super.matches(path);
    }

    @Override
    public ObjectTag getContext(String name) {
        return switch (name) {
            case "source" -> location;
            case "location" -> {
                BukkitImplDeprecations.liquidSpreadEventContext.warn();
                yield location;
            }
            case "destination" -> destination;
            case "material" -> material;
            default -> super.getContext(name);
        };
    }

    @EventHandler
    public void onLiquidSpreads(BlockFromToEvent event) {
        destination = new LocationTag(event.getToBlock().getLocation());
        location = new LocationTag(event.getBlock().getLocation());
        material = new MaterialTag(event.getBlock());
        this.event = event;
        fire(event);
    }
}

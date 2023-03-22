package com.denizenscript.denizen.events.block;

import com.denizenscript.denizen.events.BukkitScriptEvent;
import com.denizenscript.denizen.objects.ItemTag;
import com.denizenscript.denizen.objects.LocationTag;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.BrewingStandFuelEvent;

public class BrewingStandFueledScriptEvent extends BukkitScriptEvent implements Listener {

    // <--[event]
    // @Events
    // brewing stand fueled (with <item>)
    //
    // @Group Block
    //
    // @Location true
    //
    // @Cancellable true
    //
    // @Triggers when a brewing stand receives an item to use as fuel.
    //
    // @Context
    // <context.location> returns the LocationTag of the brewing stand.
    // <context.item> returns the ItemTag being inserted as fuel.
    // <context.fuel_power> returns the fuel power level being added. Each unit of fuel can power one brewing operation.
    // <context.consuming> returns a boolean indicating whether the fuel item will be consumed.
    //
    // @Determine
    // "FUEL_POWER:<ElementTag(Number)>" to set the fuel power level to be added.
    // "CONSUMING" to indicate that the fuel item should be consumed.
    // "NOT_CONSUMING" to indicate that the fuel item should not be consumed.
    //
    // -->

    public BrewingStandFueledScriptEvent() {
        registerCouldMatcher("brewing stand fueled (with <item>)");
    }

    public LocationTag location;
    public ItemTag item;
    public BrewingStandFuelEvent event;

    @Override
    public boolean matches(ScriptPath path) {
        if (!runInCheck(path, location)) {
            return false;
        }
        if (path.eventArgLowerAt(3).equals("with") && !path.tryArgObject(4, item)) {
            return false;
        }
        return super.matches(path);
    }

    @Override
    public boolean applyDetermination(ScriptPath path, ObjectTag determinationObj) {
        if (determinationObj instanceof ElementTag element) {
            String val = element.asString();
            if (val.startsWith("fuel_power:")) {
                event.setFuelPower(Integer.parseInt(val.substring("fuel_power:".length())));
                return true;
            }
            else if (val.equalsIgnoreCase("consuming")) {
                event.setConsuming(true);
                return true;
            }
            else if (val.equalsIgnoreCase("not_consuming")) {
                event.setConsuming(false);
                return true;
            }
        }
        return super.applyDetermination(path, determinationObj);
    }

    @Override
    public ObjectTag getContext(String name) {
        switch (name) {
            case "location": return location;
            case "item": return item;
            case "fuel_power": return new ElementTag(event.getFuelPower());
            case "consuming": return new ElementTag(event.isConsuming());
        }
        return super.getContext(name);
    }

    @EventHandler
    public void onBrewingStandFueled(BrewingStandFuelEvent event) {
        location = new LocationTag(event.getBlock().getLocation());
        item = new ItemTag(event.getFuel());
        this.event = event;
        fire(event);
    }
}

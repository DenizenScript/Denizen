package net.aufdemrand.denizen.events.world;

import net.aufdemrand.denizen.events.BukkitScriptEvent;
import net.aufdemrand.denizen.objects.dItem;
import net.aufdemrand.denizen.objects.dLocation;
import net.aufdemrand.denizencore.objects.aH;
import net.aufdemrand.denizencore.objects.dObject;
import net.aufdemrand.denizencore.scripts.containers.ScriptContainer;
import net.aufdemrand.denizencore.utilities.CoreUtilities;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.FurnaceBurnEvent;

public class FurnaceBurnsItemScriptEvent extends BukkitScriptEvent implements Listener {

    // <--[event]
    // @Events
    // furnace burns item
    // furnace burns <item>
    //
    // @Regex ^on furnace burns [^\s]+$
    // @Switch in <area>
    //
    // @Cancellable true
    //
    // @Triggers when a furnace burns an item used as fuel.
    //
    // @Context
    // <context.location> returns the dLocation of the furnace.
    // <context.item> returns the dItem burnt.
    //
    // @Determine
    // Element(Number) to set the burn time for this fuel.
    //
    // -->

    public FurnaceBurnsItemScriptEvent() {
        instance = this;
    }

    public static FurnaceBurnsItemScriptEvent instance;
    public dItem item;
    public dLocation location;
    private Integer burntime;
    public FurnaceBurnEvent event;

    @Override
    public boolean couldMatch(ScriptContainer scriptContainer, String s) {
        return CoreUtilities.toLowerCase(s).startsWith("furnace burns");
    }

    @Override
    public boolean matches(ScriptPath path) {
        String lower = path.eventLower;
        String iTest = path.eventArgLowerAt(2);
        return tryItem(item, iTest)
                && runInCheck(path, location);
    }

    @Override
    public String getName() {
        return "FurnaceBurns";
    }

    @Override
    public boolean applyDetermination(ScriptContainer container, String determination) {
        if (aH.matchesInteger(determination)) {
            burntime = aH.getIntegerFrom(determination);
            return true;
        }
        return super.applyDetermination(container, determination);
    }

    @Override
    public dObject getContext(String name) {
        if (name.equals("location")) {
            return location;
        }
        else if (name.equals("item")) {
            return item;
        }
        return super.getContext(name);
    }

    @EventHandler
    public void onBrews(FurnaceBurnEvent event) {
        location = new dLocation(event.getBlock().getLocation());
        item = new dItem(event.getFuel());
        burntime = event.getBurnTime();
        this.event = event;
        fire(event);
        event.setBurnTime(burntime);
    }
}

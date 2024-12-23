package com.denizenscript.denizen.events.block;

import com.denizenscript.denizen.events.BukkitScriptEvent;
import com.denizenscript.denizen.objects.LocationTag;
import com.denizenscript.denizen.objects.MaterialTag;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import org.bukkit.block.data.type.Farmland;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.MoistureChangeEvent;

public class MoistureChangeScriptEvent extends BukkitScriptEvent implements Listener {

    // <--[event]
    // @Events
    // farmland moisture level changes
    //
    // @Group Block
    //
    // @Location true
    //
    // @Triggers when a farmland's moisture level changes.
    //
    // @Switch from:<level> to only process the event when the previous moisture level matches the input.
    // @Switch to:<level> to only process the event when the new moisture level matches the input.
    //
    // @Context
    // <context.location> returns the LocationTag of the farmland block.
    // <context.material> returns the MaterialTag of the farmland block that changed the moisture level.
    // <context.old_level> returns the ElementTag(Number) of the previous moisture level.
    // <context.new_level> returns the ElementTag(Number) of the new moisture level.
    //
    // @Cancellable true
    //
    // @Example
    // # Announce when farmland begins to dry out.
    // on farmland moisture level changes from:7 to:6:
    // - announce "Farmland at location <context.location.simple> lost its water source and began to dry!"
    //
    // -->

    public MoistureChangeScriptEvent() {
        registerCouldMatcher("farmland moisture level changes");
        registerSwitches("from", "to");
    }

    public MoistureChangeEvent event;
    public LocationTag location;
    public Farmland oldFarmland;
    public Farmland newFarmland;

    @Override
    public boolean matches(ScriptPath path) {
        if (!runInCheck(path, location)) {
            return false;
        }
        if (!path.checkSwitch("from", String.valueOf(oldFarmland.getMoisture()))) {
            return false;
        }
        if (!path.checkSwitch("to", String.valueOf(newFarmland.getMoisture()))) {
            return false;
        }
        return super.matches(path);
    }

    @Override
    public ObjectTag getContext(String name) {
        return switch (name) {
            case "location" -> location;
            case "material" -> new MaterialTag(oldFarmland);
            case "old_level" -> new ElementTag(oldFarmland.getMoisture());
            case "new_level" -> new ElementTag(newFarmland.getMoisture());
            default -> super.getContext(name);
        };
    }

    @EventHandler
    public void onMoistureChange(MoistureChangeEvent event) {
        location = new LocationTag(event.getBlock().getLocation());
        oldFarmland = (Farmland) event.getBlock().getBlockData();
        newFarmland = (Farmland) event.getNewState().getBlockData();
        this.event = event;
        fire(event);
    }
}

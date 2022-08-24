package com.denizenscript.denizen.events.world;

import com.denizenscript.denizen.events.BukkitScriptEvent;
import com.denizenscript.denizen.utilities.world.RaidData;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.utilities.CoreUtilities;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.raid.RaidStopEvent;

public class RaidStopsScriptEvent extends BukkitScriptEvent implements Listener {

    public RaidStopsScriptEvent() {
        registerCouldMatcher("raid stops");
        registerSwitches("reason");
    }

    public RaidStopEvent event;

    @Override
    public boolean matches(ScriptPath path) {
        if (path.switches.containsKey("reason") && !path.checkSwitch("reason", CoreUtilities.toLowerCase(event.getReason().name()))) {
            return false;
        }
        if (!runInCheck(path, event.getRaid().getLocation())) {
            return false;
        }
        return super.matches(path);
    }

    @Override
    public ObjectTag getContext(String name) {
        switch (name) {
            case "reason":
                return new ElementTag(CoreUtilities.toLowerCase(event.getReason().name()), true);
            case "raid":
                return RaidData.toMap(event.getRaid());
        }
        return super.getContext(name);
    }

    @EventHandler
    public void onRaidStops(RaidStopEvent event) {
        this.event = event;
        fire(event);
    }
}

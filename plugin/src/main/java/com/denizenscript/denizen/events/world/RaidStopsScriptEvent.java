package com.denizenscript.denizen.events.world;

import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.utilities.CoreUtilities;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.raid.RaidStopEvent;

public class RaidStopsScriptEvent extends RaidScriptEvent<RaidStopEvent> implements Listener {

    public RaidStopsScriptEvent() {
        super(true);
        registerCouldMatcher("raid stops");
        registerSwitches("reason");
    }

    @Override
    public boolean matches(ScriptPath path) {
        if (!runGenericSwitchCheck(path, "reason", event.getReason().name())) {
            return false;
        }
        return super.matches(path);
    }

    @Override
    public ObjectTag getContext(String name) {
        switch (name) {
            case "reason":
                return new ElementTag(CoreUtilities.toLowerCase(event.getReason().name()), true);
        }
        return super.getContext(name);
    }

    @EventHandler
    public void onRaidStops(RaidStopEvent event) {
        this.event = event;
        fire(event);
    }
}

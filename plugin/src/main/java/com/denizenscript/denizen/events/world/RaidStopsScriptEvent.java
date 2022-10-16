package com.denizenscript.denizen.events.world;

import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.raid.RaidStopEvent;

public class RaidStopsScriptEvent extends RaidScriptEvent<RaidStopEvent> implements Listener {

    // <--[event]
    // @Events
    // raid stops
    //
    // @Group World
    //
    // @Location true
    //
    // @Switch reason:<reason> to only process the event if the raid stopped for a certain reason.
    //
    // @Triggers when a village raid stops for any reason.
    //
    // @Context
    // <context.raid> returns the raid data. See <@link language Raid Event Data>.
    // <context.reason> returns the reason for stopping. See <@link url https://hub.spigotmc.org/javadocs/bukkit/org/bukkit/event/raid/RaidStopEvent.Reason.html>.
    //
    // -->

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
                return new ElementTag(event.getReason());
        }
        return super.getContext(name);
    }

    @EventHandler
    public void onRaidStops(RaidStopEvent event) {
        this.event = event;
        fire(event);
    }
}

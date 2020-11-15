package com.denizenscript.denizen.events.server;

import com.denizenscript.denizen.events.BukkitScriptEvent;

public class ServerStopScriptEvent extends BukkitScriptEvent {

    // <--[event]
    // @Events
    // shutdown
    //
    // @Regex ^on shutdown$
    //
    // @Group Server
    //
    // @Warning not all plugins will be loaded and delayed scripts will be dropped.
    // Also note that this event is not guaranteed to always run (eg if the server crashes).
    //
    // @Triggers when the server is shutting down.
    //
    // @Context
    // None.
    //
    // -->

    public ServerStopScriptEvent() {
        instance = this;
    }

    public static ServerStopScriptEvent instance;

    @Override
    public boolean couldMatch(ScriptPath path) {
        if (!path.eventLower.startsWith("shutdown")) {
            return false;
        }
        return true;
    }

    @Override
    public String getName() {
        return "ServerShutdown";
    }
}

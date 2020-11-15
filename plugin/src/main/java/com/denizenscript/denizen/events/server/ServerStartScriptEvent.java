package com.denizenscript.denizen.events.server;

import com.denizenscript.denizen.events.BukkitScriptEvent;

public class ServerStartScriptEvent extends BukkitScriptEvent {

    // <--[event]
    // @Events
    // server start
    //
    // @Regex ^on server start$
    //
    // @Group Server
    //
    // @Triggers when the server starts.
    //
    // -->

    public ServerStartScriptEvent() {
        instance = this;
    }

    public static ServerStartScriptEvent instance;

    @Override
    public boolean couldMatch(ScriptPath path) {
        if (!path.eventLower.startsWith("server start")) {
            return false;
        }
        return true;
    }

    @Override
    public String getName() {
        return "ServerStart";
    }
}

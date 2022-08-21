package com.denizenscript.denizen.events.server;

import com.denizenscript.denizen.events.BukkitScriptEvent;

public class ServerStartScriptEvent extends BukkitScriptEvent {

    // <--[event]
    // @Events
    // server start
    //
    // @Group Server
    //
    // @Triggers when the server starts.
    //
    // -->

    public ServerStartScriptEvent() {
        instance = this;
        registerCouldMatcher("server start");
    }

    public static ServerStartScriptEvent instance;
}

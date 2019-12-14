package com.denizenscript.denizen.events.core;

import com.denizenscript.denizen.events.BukkitScriptEvent;
import com.denizenscript.denizencore.objects.ObjectTag;

public class ServerStartScriptEvent extends BukkitScriptEvent {

    // <--[event]
    // @Events
    // server start
    //
    // @Regex ^on server start$
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
        return path.eventLower.startsWith("server start");
    }

    @Override
    public boolean matches(ScriptPath path) {
        return super.matches(path);
    }

    @Override
    public String getName() {
        return "ServerStart";
    }

    @Override
    public ObjectTag getContext(String name) {
        return super.getContext(name);
    }
}

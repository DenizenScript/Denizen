package com.denizenscript.denizen.events.server;

import com.denizenscript.denizen.events.BukkitScriptEvent;
import com.denizenscript.denizencore.events.ScriptEvent;
import com.denizenscript.denizencore.scripts.containers.core.WorldScriptContainer;

public class ServerPrestartScriptEvent extends BukkitScriptEvent {

    // <--[event]
    // @Events
    // server prestart
    //
    // @Group Server
    //
    // @Triggers before the server finishes starting... fired after some saves are loaded, but before other data is loaded. Use with extreme caution.
    //
    // @Warning This event uses special pre-loading tricks to fire before everything else. Use extreme caution.
    //
    // @Example
    // on server prestart:
    // - createworld my_extra_world
    //
    // -->

    public ServerPrestartScriptEvent() {
        instance = this;
        registerCouldMatcher("server prestart");
    }

    public static ServerPrestartScriptEvent instance;

    public void specialHackRunEvent() {
        for (WorldScriptContainer container : ScriptEvent.worldContainers) {
            if (container.containsScriptSection("events.on server prestart") && container.shouldEnable()) {
                ScriptPath path = new ScriptPath(container, "server prestart", "on server prestart");
                clone().run(path);
            }
        }
    }
}

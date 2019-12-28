package com.denizenscript.denizen.events.core;

import com.denizenscript.denizen.events.BukkitScriptEvent;
import com.denizenscript.denizencore.events.ScriptEvent;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.scripts.containers.core.WorldScriptContainer;

public class ServerPrestartScriptEvent extends BukkitScriptEvent {

    // <--[event]
    // @Events
    // server prestart
    //
    // @Regex ^on server prestart$
    //
    // @Triggers before the server finishes starting... fired after some saves are loaded, but before other data is loaded. Use with extreme caution.
    //
    // @Warning This event uses special pre-loading tricks to fire before everything else. Use extreme caution.
    //
    // -->

    public ServerPrestartScriptEvent() {
        instance = this;
    }

    public static ServerPrestartScriptEvent instance;

    @Override
    public boolean couldMatch(ScriptPath path) {
        return path.eventLower.startsWith("server prestart");
    }

    @Override
    public String getName() {
        return "ServerPrestart";
    }

    @Override
    public ObjectTag getContext(String name) {
        return super.getContext(name);
    }

    public void specialHackRunEvent() {
        for (WorldScriptContainer container : ScriptEvent.worldContainers) {
            if (container.contains("events.on server prestart")) {
                ScriptPath path = new ScriptPath(container, "server prestart");
                run(path);
            }
        }
    }
}

package com.denizenscript.denizen.paper.events;

import com.denizenscript.denizen.events.BukkitScriptEvent;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import io.papermc.paper.event.server.ServerResourcesReloadedEvent;

public class ServerResourcesReloadedScriptEvent extends BukkitScriptEvent implements Listener {

    // <--[event]
    // @Events
    // server resources reloaded
    //
    // @Plugin Paper
    //
    // @Group Paper
    //
    // @Switch cause:<cause> to only process the event if the cause of the resource reload matches the specified cause.
    //
    // @Triggers when vanilla resources (such as datapacks) are reloaded (by vanilla commands or by plugins).
    //
    // @Context
    // <context.cause> Returns the cause of the resource reload. Refer to <@link url https://jd.papermc.io/paper/1.19/io/papermc/paper/event/server/ServerResourcesReloadedEvent.Cause.html>
    //
    // -->

    public ServerResourcesReloadedScriptEvent() {
        instance = this;
        registerCouldMatcher("server resources reloaded");
        registerSwitches("cause");
    }

    public static ServerResourcesReloadedScriptEvent instance;
    public ElementTag cause;

    @Override
    public boolean matches(ScriptPath path) {
        if (!runGenericSwitchCheck(path, "cause", cause.asString())) {
            return false;
        }
        return super.matches(path);
    }

    @Override
    public String getName() {
        return "ServerResourcesReloaded";
    }

    @Override
    public ObjectTag getContext(String name) {
        switch (name) {
            case "cause": return cause;
        }
        return super.getContext(name);
    }

    @EventHandler
    public void onServerResourcesReloaded(ServerResourcesReloadedEvent event) {
        cause = new ElementTag(event.getCause().name());
        fire(event);
    }
}

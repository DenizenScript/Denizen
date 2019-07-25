package com.denizenscript.denizen.events.player;

import com.denizenscript.denizen.events.BukkitScriptEvent;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.core.ListTag;
import com.denizenscript.denizencore.objects.ObjectTag;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.ServerListPingEvent;

public class ListPingScriptEvent extends BukkitScriptEvent implements Listener {

    // <--[event]
    // @Events
    // server list ping
    //
    // @Regex ^on server list ping$
    //
    // @Triggers when the server is pinged for a client's server list.
    // @Context
    // <context.motd> returns the MOTD that will show.
    // <context.max_players> returns the number of max players that will show.
    // <context.num_players> returns the number of online players that will show.
    // <context.address> returns the IP address requesting the list.
    //
    // @Determine
    // Element(Number) to change the max player amount that will show
    // ElementTag to change the MOTD that will show.
    //
    // -->

    public ListPingScriptEvent() {
        instance = this;
    }

    public static ListPingScriptEvent instance;

    public ServerListPingEvent event;

    @Override
    public boolean couldMatch(ScriptPath path) {
        return path.eventLower.startsWith("server list ping");
    }

    @Override
    public boolean matches(ScriptPath path) {
        return true;
    }

    @Override
    public String getName() {
        return "ServerListPing";
    }

    @Override
    public boolean applyDetermination(ScriptPath path, ObjectTag determinationObj) {
        String determination = determinationObj.toString();
        if (determination.length() > 0 && !determination.equalsIgnoreCase("none")) {
            String[] values = determination.split("[\\|" + ListTag.internal_escape + "]", 2);
            if (new ElementTag(values[0]).isInt()) {
                event.setMaxPlayers(new ElementTag(values[0]).asInt());
                if (values.length == 1) {
                    return true;
                }
            }
            if (values.length == 2) {
                event.setMotd(values[1]);
            }
            else {
                event.setMotd(values[0]);
            }
            return true;
        }
        else {
            return super.applyDetermination(path, determinationObj);
        }
    }

    @Override
    public ObjectTag getContext(String name) {
        if (name.equals("motd")) {
            return new ElementTag(event.getMotd());
        }
        else if (name.equals("max_players")) {
            return new ElementTag(event.getMaxPlayers());
        }
        else if (name.equals("num_players")) {
            return new ElementTag(event.getNumPlayers());
        }
        else if (name.equals("address")) {
            return new ElementTag(event.getAddress().toString());
        }
        return super.getContext(name);
    }

    @EventHandler
    public void onListPing(ServerListPingEvent event) {
        this.event = event;
        fire(event);
    }
}

package net.aufdemrand.denizen.events.player;

import net.aufdemrand.denizen.events.BukkitScriptEvent;
import net.aufdemrand.denizen.utilities.DenizenAPI;
import net.aufdemrand.denizencore.objects.Element;
import net.aufdemrand.denizencore.objects.dList;
import net.aufdemrand.denizencore.objects.dObject;
import net.aufdemrand.denizencore.scripts.containers.ScriptContainer;
import net.aufdemrand.denizencore.utilities.CoreUtilities;
import org.bukkit.Bukkit;
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
    // Element(Number)|Element to set the max player amount and change the MOTD.
    // Element to change the MOTD that will show.
    //
    // -->

    public ListPingScriptEvent() {
        instance = this;
    }

    public static ListPingScriptEvent instance;

    public Element motd;
    public Element max_players;
    public Element num_players;
    public Element address;
    public ServerListPingEvent event;

    @Override
    public boolean couldMatch(ScriptContainer scriptContainer, String s) {
        String lower = CoreUtilities.toLowerCase(s);
        return lower.startsWith("server list ping");
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
    public boolean applyDetermination(ScriptContainer container, String determination) {
        if (determination.length() > 0 && !determination.equalsIgnoreCase("none")) {
            String[] values = determination.split("[\\|" + dList.internal_escape + "]", 2);
            if (new Element(values[0]).isInt()) {
                max_players = new Element(values[0]);
                if (values.length == 1) {
                    return true;
                }
            }
            if (values.length == 2) {
                motd = new Element(values[1]);
            }
            else {
                motd = new Element(values[0]);
            }
            return true;
        }
        else {
            return super.applyDetermination(container, determination);
        }
    }

    @Override
    public dObject getContext(String name) {
        if (name.equals("motd")) {
            return motd;
        }
        else if (name.equals("max_players")) {
            return max_players;
        }
        else if (name.equals("num_players")) {
            return num_players;
        }
        else if (name.equals("address")) {
            return address;
        }
        return super.getContext(name);
    }

    @EventHandler
    public void onListPing(ServerListPingEvent event) {
        motd = new Element(event.getMotd());
        max_players = new Element(event.getMaxPlayers());
        num_players = new Element(event.getNumPlayers());
        address = new Element(event.getAddress().toString());
        this.event = event;
        fire();
        event.setMaxPlayers(max_players.asInt());
        event.setMotd(motd.asString());
    }
}

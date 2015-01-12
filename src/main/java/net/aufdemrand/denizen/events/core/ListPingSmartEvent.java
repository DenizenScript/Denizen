package net.aufdemrand.denizen.events.core;

import net.aufdemrand.denizen.scripts.containers.core.BukkitWorldScriptHelper;
import net.aufdemrand.denizencore.events.OldSmartEvent;
import net.aufdemrand.denizencore.objects.Element;
import net.aufdemrand.denizencore.objects.dList;
import net.aufdemrand.denizencore.objects.dObject;
import net.aufdemrand.denizen.utilities.DenizenAPI;
import net.aufdemrand.denizen.utilities.debugging.dB;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.ServerListPingEvent;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ListPingSmartEvent implements OldSmartEvent, Listener {


    ///////////////////
    // SMARTEVENT METHODS
    ///////////////


    @Override
    public boolean shouldInitialize(Set<String> events) {

        // Loop through event names from loaded world script events
        for (String event : events) {

            // Use a regex pattern to narrow down matches
            Matcher m = Pattern.compile("on server list ping", Pattern.CASE_INSENSITIVE)
                    .matcher(event);

            if (m.matches()) {
                // Event names are simple enough to just go ahead and pass on any match.
                return true;
            }
        }
        // No matches at all, just fail.
        return false;
    }


    @Override
    public void _initialize() {
        DenizenAPI.getCurrentInstance().getServer().getPluginManager()
                .registerEvents(this, DenizenAPI.getCurrentInstance());
        dB.log("Loaded List Ping SmartEvent.");
    }


    @Override
    public void breakDown() {
        ServerListPingEvent.getHandlerList().unregister(this);
    }

    //////////////
    //  MECHANICS
    ///////////

    // <--[event]
    // @Events
    // server list ping
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
    @EventHandler
    public void onListPing(ServerListPingEvent event) {
        Map<String, dObject> context = new HashMap<String, dObject>();
        context.put("motd", new Element(event.getMotd()));
        context.put("max_players", new Element(event.getMaxPlayers()));
        context.put("num_players", new Element(event.getNumPlayers()));
        context.put("address", new Element(event.getAddress().toString()));
        String determination = BukkitWorldScriptHelper.doEvents(Arrays.asList("server list ping"), null, null, context);
        String[] values = determination.split("[\\|" + dList.internal_escape + "]", 2);
        if (new Element(values[0]).isInt())
            event.setMaxPlayers(new Element(values[0]).asInt());
        if (determination.length() > 0 && !determination.equalsIgnoreCase("none")) {
            if (values.length == 2)
                event.setMotd(values[1]);
            else
                event.setMotd(determination);
        }
    }
}

package com.denizenscript.denizen.events.server;

import com.denizenscript.denizen.events.BukkitScriptEvent;
import com.denizenscript.denizen.utilities.DenizenAPI;
import com.denizenscript.denizen.utilities.Utilities;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.utilities.CoreUtilities;
import com.denizenscript.denizencore.utilities.debugging.Debug;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.ServerListPingEvent;
import org.bukkit.util.CachedServerIcon;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public class ListPingScriptEvent extends BukkitScriptEvent implements Listener {

    // <--[event]
    // @Events
    // server list ping
    //
    // @Regex ^on server list ping$
    //
    // @Group Server
    //
    // @Triggers when the server is pinged for a client's server list.
    // @Context
    // <context.motd> returns the MOTD that will show.
    // <context.max_players> returns the number of max players that will show.
    // <context.num_players> returns the number of online players that will show.
    // <context.address> returns the IP address requesting the list.
    //
    // @Determine
    // ElementTag(Number) to change the max player amount that will show.
    // "ICON:" + ElementTag of a file path to an icon image, to change the icon that will display.
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
        if (!path.eventLower.startsWith("server list ping")) {
            return false;
        }
        return true;
    }

    @Override
    public String getName() {
        return "ServerListPing";
    }

    // Despite the 'cached' class name, there's no actual internal cache.
    public static HashMap<String, CachedServerIcon> iconCache = new HashMap<>();

    @Override
    public boolean applyDetermination(ScriptPath path, ObjectTag determinationObj) {
        String determination = determinationObj.toString();
        if (determination.startsWith("icon:")) {
            String iconFile = determination.substring("icon:".length());
            CachedServerIcon icon = iconCache.get(iconFile);
            if (icon != null) {
                event.setServerIcon(icon);
                return true;
            }
            File file = new File(iconFile);
            if (!Utilities.canReadFile(file)) {
                Debug.echoError("Cannot read icon file '" + iconFile + "'");
                return false;
            }
            try {
                icon = Bukkit.loadServerIcon(file);
            }
            catch (Exception ex) {
                Debug.echoError(ex);
            }
            if (icon != null) {
                iconCache.put(iconFile, icon);
                event.setServerIcon(icon);
            }
            return true;

        }
        if (determination.length() > 0 && !determination.equalsIgnoreCase("none")) {
            List<String> values = CoreUtilities.split(determination, '|', 2);
            if (new ElementTag(values.get(0)).isInt()) {
                event.setMaxPlayers(new ElementTag(values.get(0)).asInt());
                if (values.size() == 2) {
                    event.setMotd(values.get(1));
                }
            }
            else {
                event.setMotd(determination);
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
        if (!Bukkit.isPrimaryThread()) {
            BukkitScriptEvent altEvent = (BukkitScriptEvent) clone();
            Future future = Bukkit.getScheduler().callSyncMethod(DenizenAPI.getCurrentInstance(), () -> {
                altEvent.fire();
                return null;
            });
            try {
                future.get(5, TimeUnit.SECONDS);
            }
            catch (Throwable ex) {
                Debug.echoError(ex);
            }
            return;
        }
        fire(event);
    }
}

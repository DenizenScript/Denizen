package com.denizenscript.denizen.events.server;

import com.denizenscript.denizen.Denizen;
import com.denizenscript.denizen.events.BukkitScriptEvent;
import com.denizenscript.denizen.nms.NMSHandler;
import com.denizenscript.denizen.nms.NMSVersion;
import com.denizenscript.denizen.utilities.Utilities;
import com.denizenscript.denizencore.objects.ArgumentHelper;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.core.ListTag;
import com.denizenscript.denizencore.utilities.debugging.Debug;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.ServerListPingEvent;
import org.bukkit.util.CachedServerIcon;

import java.io.File;
import java.util.HashMap;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public class ListPingScriptEvent extends BukkitScriptEvent implements Listener {

    // <--[event]
    // @Events
    // server list ping
    //
    // @Group Server
    //
    // @Triggers when the server is pinged for a client's server list.
    //
    // @Context
    // <context.motd> returns the MOTD that will show.
    // <context.max_players> returns the number of max players that will show.
    // <context.num_players> returns the number of online players that will show.
    // <context.address> returns the IP address requesting the list.
    // <context.hostname> returns an ElementTag of the server address that is being pinged. Available only on MC 1.19+.
    // <context.protocol_version> returns the protocol ID of the server's version (only on Paper).
    // <context.version_name> returns the name of the server's version (only on Paper).
    // <context.client_protocol_version> returns the client's protocol version ID (only on Paper).
    //
    // @Determine
    // "MAX_PLAYERS:<ElementTag(Number)>" to change the max player amount that will show.
    // "ICON:<ElementTag>" of a file path to an icon image, to change the icon that will display.
    // "PROTOCOL_VERSION:<ElementTag(Number)>" to change the protocol ID number of the server's version (only on Paper).
    // "VERSION_NAME:<ElementTag>" to change the server's version name (only on Paper).
    // "EXCLUDE_PLAYERS:<ListTag(PlayerTag)>" to exclude a set of players from showing in the player count or preview of online players (only on Paper).
    // "ALTERNATE_PLAYER_TEXT:<ListTag>" to set custom text for the player list section of the server status (only on Paper). (Requires "Allow restricted actions" in Denizen/config.yml). Usage of this to present lines that look like player names (but aren't) is forbidden.
    // "MOTD:<ElementTag>" to change the MOTD that will show.
    //
    // -->

    public ListPingScriptEvent() {
        registerCouldMatcher("server list ping");
        this.<ListPingScriptEvent, ListTag>registerDetermination(null, ListTag.class, (evt, context, list) -> {
            if (ArgumentHelper.matchesInteger(list.get(0))) {
                evt.event.setMaxPlayers(Integer.parseInt(list.get(0)));
                if (list.size() == 2) {
                    evt.setMotd(list.get(1));
                }
            }
            else {
                evt.setMotd(list.get(0));
            }
        });
        this.<ListPingScriptEvent, ElementTag>registerOptionalDetermination("max_players", ElementTag.class, (evt, context, max) -> {
            if (max.isInt()) {
                evt.event.setMaxPlayers(max.asInt());
                return true;
            }
            return false;
        });
        this.<ListPingScriptEvent, ElementTag>registerDetermination("motd", ElementTag.class, (evt, context, motd) -> {
            evt.setMotd(motd.asString());
        });
        this.<ListPingScriptEvent, ElementTag>registerOptionalDetermination("icon", ElementTag.class, (evt, context, iconPath) -> {
            String iconFile = iconPath.toString();
            CachedServerIcon icon = iconCache.get(iconFile);
            if (icon != null) {
                evt.event.setServerIcon(icon);
                return true;
            }
            File file = new File(iconFile);
            if (!Utilities.canReadFile(file)) {
                Debug.echoError("Cannot read icon file '" + iconFile + "' due to security settings in Denizen/config.yml");
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
                evt.event.setServerIcon(icon);
                return true;
            }
            return false;
        });
    }


    public ServerListPingEvent event;

    // Despite the 'cached' class name, there's no actual internal cache.
    public static HashMap<String, CachedServerIcon> iconCache = new HashMap<>();

    public void setMotd(String text) {
        event.setMotd(text);
    }

    @Override
    public ObjectTag getContext(String name) {
        return switch (name) {
            case "motd" -> new ElementTag(event.getMotd());
            case "max_players" -> new ElementTag(event.getMaxPlayers());
            case "num_players" -> new ElementTag(event.getNumPlayers());
            case "address" -> new ElementTag(event.getAddress().toString());
            case "hostname" -> NMSHandler.getVersion().isAtLeast(NMSVersion.v1_19) ? new ElementTag(event.getHostname(), true) : null;
            default -> super.getContext(name);
        };
    }

    public void syncFire(ServerListPingEvent event) {
        this.event = event;
        if (!Bukkit.isPrimaryThread()) {
            BukkitScriptEvent altEvent = (BukkitScriptEvent) clone();
            Future future = Bukkit.getScheduler().callSyncMethod(Denizen.getInstance(), () -> {
                altEvent.fire(event);
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

    public static class ListPingScriptEventSpigotImpl extends ListPingScriptEvent {

        @EventHandler
        public void onListPing(ServerListPingEvent event) {
            syncFire(event);
        }
    }
}

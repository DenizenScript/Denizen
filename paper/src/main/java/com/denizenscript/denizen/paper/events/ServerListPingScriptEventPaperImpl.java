package com.denizenscript.denizen.paper.events;

import com.denizenscript.denizen.events.server.ListPingScriptEvent;
import com.denizenscript.denizencore.objects.ArgumentHelper;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.utilities.CoreUtilities;
import com.destroystokyo.paper.event.server.PaperServerListPingEvent;
import org.bukkit.event.EventHandler;

public class ServerListPingScriptEventPaperImpl extends ListPingScriptEvent {

    @Override
    public boolean applyDetermination(ScriptPath path, ObjectTag determinationObj) {
        if (!isDefaultDetermination(determinationObj)) {
            String determination = determinationObj.toString();
            String lower = CoreUtilities.toLowerCase(determination);
            if (lower.startsWith("protocol_version:") && ArgumentHelper.matchesInteger(determination.substring("protocol_version:".length()))) {
                ((PaperServerListPingEvent) event).setProtocolVersion(Integer.parseInt(determination.substring("protocol_version:".length())));
                return true;
            }
            else if (lower.startsWith("version_name:")) {
                ((PaperServerListPingEvent) event).setVersion(determination.substring("version_name:".length()));
                return true;
            }
        }
        return super.applyDetermination(path, determinationObj);
    }

    @Override
    public ObjectTag getContext(String name) {
        if (name.equals("protocol_version")) {
            return new ElementTag(((PaperServerListPingEvent) event).getProtocolVersion());
        }
        else if (name.equals("version_name")) {
            return new ElementTag(((PaperServerListPingEvent) event).getVersion());
        }
        return super.getContext(name);
    }

    @EventHandler
    public void onListPing(PaperServerListPingEvent event) {
        syncFire(event);
    }
}

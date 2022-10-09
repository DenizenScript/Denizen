package com.denizenscript.denizen.paper.events;

import com.denizenscript.denizen.events.player.PlayerCompletesAdvancementScriptEvent;
import com.denizenscript.denizen.paper.PaperModule;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.utilities.CoreUtilities;
import net.md_5.bungee.api.ChatColor;

public class PlayerCompletesAdvancementScriptEventPaperImpl extends PlayerCompletesAdvancementScriptEvent {

    @Override
    public ObjectTag getContext(String name) {
        switch (name) {
            case "message": return new ElementTag(PaperModule.stringifyComponent(event.message()));
        }
        return super.getContext(name);
    }

    @Override
    public boolean applyDetermination(ScriptPath path, ObjectTag determinationObj) {
        if (determinationObj instanceof ElementTag) {
            String determination = determinationObj.toString();
            String lower = CoreUtilities.toLowerCase(determination);
            if (lower.equals("no_message")) {
                event.message(null);
                return true;
            }
            event.message(PaperModule.parseFormattedText(determination, ChatColor.WHITE));
            return true;
        }
        else {
            return super.applyDetermination(path, determinationObj);
        }
    }
}

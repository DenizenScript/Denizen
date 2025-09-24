package com.denizenscript.denizen.paper.events;

import com.denizenscript.denizen.events.player.PlayerCompletesAdvancementScriptEvent;
import com.denizenscript.denizen.paper.utilities.FormattedTextHelper;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.utilities.CoreUtilities;
import net.kyori.adventure.text.format.NamedTextColor;

public class PlayerCompletesAdvancementScriptEventPaperImpl extends PlayerCompletesAdvancementScriptEvent {

    @Override
    public ObjectTag getContext(String name) {
        switch (name) {
            case "message": return new ElementTag(FormattedTextHelper.stringify(event.message()), true);
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
            event.message(FormattedTextHelper.parse(determination, NamedTextColor.WHITE));
            return true;
        }
        else {
            return super.applyDetermination(path, determinationObj);
        }
    }
}

package com.denizenscript.denizen.paper.events;

import com.denizenscript.denizen.events.player.PlayerQuitsScriptEvent;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.core.ElementTag;


public class PlayerQuitsScriptEventPaperImpl extends PlayerQuitsScriptEvent {

    public PlayerQuitsScriptEventPaperImpl() {
        registerSwitches("cause");
    }

    @Override
    public boolean matches(ScriptPath path) {
        if (!runGenericSwitchCheck(path, "cause", event.getReason().name())) {
            return false;
        }
        return super.matches(path);
    }

    @Override
    public ObjectTag getContext(String name) {
        return switch (name) {
            case "cause" -> new ElementTag(event.getReason());
            default -> super.getContext(name);
        };
    }
}

package com.denizenscript.denizen.paper.events;

import com.denizenscript.denizen.events.player.PlayerQuitsScriptEvent;
import com.denizenscript.denizen.paper.PaperModule;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import net.md_5.bungee.api.ChatColor;


public class PlayerQuitsScriptEventPaperImpl extends PlayerQuitsScriptEvent {

    public PlayerQuitsScriptEventPaperImpl() {
        registerSwitches("cause");
        this.<PlayerQuitsScriptEventPaperImpl>registerTextDetermination("none", (evt) -> {
            event.quitMessage(null);
        });
        this.<PlayerQuitsScriptEventPaperImpl, ElementTag>registerOptionalDetermination(null, ElementTag.class, (evt, context, determination) -> {
            event.quitMessage(PaperModule.parseFormattedText(determination.toString(), ChatColor.WHITE));
            return true;
        });
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
            case "message" -> new ElementTag(PaperModule.stringifyComponent(event.quitMessage()));
            case "cause" -> new ElementTag(event.getReason());
            default -> super.getContext(name);
        };
    }
}

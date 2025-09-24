package com.denizenscript.denizen.paper.events;

import com.denizenscript.denizen.events.player.PlayerQuitsScriptEvent;
import com.denizenscript.denizen.paper.utilities.FormattedTextHelper;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import net.kyori.adventure.text.format.NamedTextColor;


public class PlayerQuitsScriptEventPaperImpl extends PlayerQuitsScriptEvent {

    public PlayerQuitsScriptEventPaperImpl() {
        registerSwitches("cause");
        this.<PlayerQuitsScriptEventPaperImpl>registerTextDetermination("none", (evt) -> {
            event.quitMessage(null);
        });
        this.<PlayerQuitsScriptEventPaperImpl, ElementTag>registerDetermination(null, ElementTag.class, (evt, context, determination) -> {
            event.quitMessage(FormattedTextHelper.parse(determination.asString(), NamedTextColor.WHITE));
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
            case "message" -> new ElementTag(FormattedTextHelper.stringify(event.quitMessage()), true);
            case "cause" -> new ElementTag(event.getReason());
            default -> super.getContext(name);
        };
    }
}

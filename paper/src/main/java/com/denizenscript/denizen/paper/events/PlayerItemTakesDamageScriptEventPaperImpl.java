package com.denizenscript.denizen.paper.events;

import com.denizenscript.denizen.events.player.PlayerItemTakesDamageScriptEvent;
import com.denizenscript.denizen.nms.NMSHandler;
import com.denizenscript.denizen.nms.NMSVersion;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.core.ElementTag;

public class PlayerItemTakesDamageScriptEventPaperImpl extends PlayerItemTakesDamageScriptEvent {

    @Override
    public ObjectTag getContext(String name) {
        switch (name) {
            case "original_damage": return NMSHandler.getVersion().isAtLeast(NMSVersion.v1_18) ? new ElementTag(event.getOriginalDamage()) : null;
        }
        return super.getContext(name);
    }
}

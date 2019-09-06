package com.denizenscript.denizen.paper.events;

import com.denizenscript.denizen.events.player.PlayerJumpScriptEvent;
import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizen.objects.LocationTag;
import com.denizenscript.denizen.objects.PlayerTag;
import com.destroystokyo.paper.event.player.PlayerJumpEvent;
import org.bukkit.event.EventHandler;

public class PlayerJumpsPaperScriptEventImpl extends PlayerJumpScriptEvent {

    @EventHandler
    public void onPlayerJumps(PlayerJumpEvent event) {
        if (EntityTag.isNPC(event.getPlayer())) {
            return;
        }
        location = new LocationTag(event.getFrom());
        player = new PlayerTag(event.getPlayer());
        fire(event);
    }
}

package com.denizenscript.denizen.paper.events;

import com.denizenscript.denizen.events.player.AreaEnterExitScriptEvent;
import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import io.papermc.paper.event.entity.EntityMoveEvent;
import org.bukkit.event.EventHandler;

public class AreaEnterExitScriptEventPaperImpl extends AreaEnterExitScriptEvent {

    @Override
    public ObjectTag getContext(String name) {
        if (name.equals("cause") && currentEvent instanceof EntityMoveEvent) {
            return new ElementTag("WALK");
        }
        return super.getContext(name);
    }

    @Override
    public void registerCorrectClass() {
        if (onlyTrackPlayers) {
            initListener(new SpigotListeners());
        }
        else {
            initListener(new PaperListeners());
        }
    }

    public class PaperListeners extends SpigotListeners {

        @EventHandler
        public void onEntityMove(EntityMoveEvent event) {
            processNewPosition(new EntityTag(event.getEntity()), event.getTo(), event);
        }
    }
}

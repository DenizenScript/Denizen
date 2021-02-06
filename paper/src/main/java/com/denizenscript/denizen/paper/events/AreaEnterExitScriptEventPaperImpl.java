package com.denizenscript.denizen.paper.events;

import com.denizenscript.denizen.events.entity.AreaEnterExitScriptEvent;
import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizen.objects.LocationTag;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import io.papermc.paper.event.entity.EntityMoveEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDeathEvent;

public class AreaEnterExitScriptEventPaperImpl extends AreaEnterExitScriptEvent {

    @Override
    public ObjectTag getContext(String name) {
        if (name.equals("cause") && currentEvent instanceof EntityMoveEvent) {
            return new ElementTag("WALK");
        }
        else if (name.equals("from") && currentEvent instanceof EntityMoveEvent) {
            return new LocationTag(((EntityMoveEvent) currentEvent).getFrom());
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
            if (event.getEntity().isValid()) {
                processNewPosition(new EntityTag(event.getEntity()), event.getTo(), event);
            }
        }

        @EventHandler
        public void onEntityDeath(EntityDeathEvent event) {
            processNewPosition(new EntityTag(event.getEntity()), null, event);
        }
    }
}

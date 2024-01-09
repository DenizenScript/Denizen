package com.denizenscript.denizen.events.entity;

import com.denizenscript.denizen.events.BukkitScriptEvent;
import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.core.ListTag;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.AreaEffectCloudApplyEvent;

public class AreaEffectCloudApplyScriptEvent extends BukkitScriptEvent implements Listener {

    // <--[event]
    // @Events
    // area cloud applies effects
    //
    // @Group Entity
    //
    // @Cancellable true
    //
    // @Triggers when a lingering potion's area cloud applies its effect to an entity, and every five ticks after.
    //
    // @Context
    // <context.entity> returns the EntityTag of the area cloud.
    // <context.affected_entities> returns a ListTag(EntityTag) of entities affected by the lingering potion's area cloud.
    //
    // -->

    public AreaEffectCloudApplyScriptEvent() {
        registerCouldMatcher("area cloud applies effects");
    }

    public AreaEffectCloudApplyEvent event;
    public EntityTag entity;

    @Override
    public ObjectTag getContext(String name) {
        return switch (name) {
            case "entity" -> entity;
            case "affected_entities" -> {
                ListTag list = new ListTag();
                for (Entity entity : event.getAffectedEntities()) {
                    list.addObject(new EntityTag(entity));
                }
                yield list;
            }
            default -> super.getContext(name);
        };
    }

    @EventHandler
    public void onAreaEffectCloudApply(AreaEffectCloudApplyEvent event) {
        this.event = event;
        entity = new EntityTag(event.getEntity());
        fire(event);
    }
}

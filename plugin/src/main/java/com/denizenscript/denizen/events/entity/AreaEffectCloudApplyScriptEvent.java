package com.denizenscript.denizen.events.entity;

import com.denizenscript.denizen.events.BukkitScriptEvent;
import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.core.ListTag;
import com.denizenscript.denizencore.utilities.debugging.Debug;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.AreaEffectCloudApplyEvent;

public class AreaEffectCloudApplyScriptEvent extends BukkitScriptEvent implements Listener {

    // <--[event]
    // @Events
    // area effect cloud applies
    //
    // @Group Entity
    //
    // @Cancellable true
    //
    // @Triggers when an area_effect_cloud tries to apply its effect(s) to entities within range.
    //
    // @Warning
    // This runs every 5 ticks if there are any entities in the area effect cloud's bounding box. Prefer <@link event entity potion effects modified> for listening to normal potion effect changes.
    //
    // @Context
    // <context.entity> returns the EntityTag of the area effect cloud.
    // <context.affected_entities> returns a ListTag of EntityTags affected by the area effect cloud. Note that this can be empty, and only lists which entities are currently being refreshed.
    //
    // @Determine
    // "AFFECTED_ENTITIES:<ListTag(EntityTag)>" to determine the entities that will be affected by the area effect cloud. The list should not contain non-living entities.
    //
    // -->

    public AreaEffectCloudApplyScriptEvent() {
        registerCouldMatcher("area effect cloud applies");
        this.<AreaEffectCloudApplyScriptEvent, ListTag>registerDetermination("affected_entities", ListTag.class, (evt, context, list) -> {
            evt.event.getAffectedEntities().clear();
            for (EntityTag entity : list.filter(EntityTag.class, context)) {
                if (entity.isLivingEntity()) {
                    evt.event.getAffectedEntities().add(entity.getLivingEntity());
                }
                else {
                    Debug.echoError(entity + " is not a living entity!");
                }
            }
        });
    }

    public AreaEffectCloudApplyEvent event;
    public EntityTag entity;

    @Override
    public ObjectTag getContext(String name) {
        return switch (name) {
            case "entity" -> entity;
            case "affected_entities" -> new ListTag(event.getAffectedEntities(), EntityTag::new);
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

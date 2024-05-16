package com.denizenscript.denizen.events.entity;

import com.denizenscript.denizen.events.BukkitScriptEvent;
import com.denizenscript.denizen.nms.NMSHandler;
import com.denizenscript.denizen.nms.NMSVersion;
import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizen.utilities.implementation.BukkitScriptEntryData;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.scripts.ScriptEntryData;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityResurrectEvent;

public class EntityResurrectScriptEvent extends BukkitScriptEvent implements Listener {

    // <--[event]
    // @Events
    // <entity> resurrected
    //
    // @Group Entity
    //
    // @Location true
    //
    // @Cancellable true
    //
    // @Triggers when an entity dies and is resurrected by a totem.
    //
    // @Context
    // <context.entity> returns the EntityTag being resurrected.
    // <context.hand> returns which hand the totem was in during resurrection, if any. Can be either HAND or OFF_HAND. Available only on MC 1.19+.
    //
    // @Player when the entity being resurrected is a player.
    //
    // -->

    public EntityResurrectScriptEvent() {
        registerCouldMatcher("<entity> resurrected");
    }

    public EntityTag entity;
    public EntityResurrectEvent event;

    @Override
    public boolean matches(ScriptPath path) {
        if (!path.tryArgObject(0, entity)) {
            return false;
        }
        if (!runInCheck(path, entity.getLocation())) {
            return false;
        }
        return super.matches(path);
    }

    @Override
    public ScriptEntryData getScriptEntryData() {
        return new BukkitScriptEntryData(entity);
    }

    @Override
    public ObjectTag getContext(String name) {
        return switch (name) {
            case "entity" -> entity;
            case "hand" -> {
                if (NMSHandler.getVersion().isAtLeast(NMSVersion.v1_19)) {
                    yield event.getHand() != null ? new ElementTag(event.getHand()) : null;
                }
                yield null;
            }
            default -> super.getContext(name);
        };

    }

    @EventHandler
    public void onEntityResurrect(EntityResurrectEvent event) {
        EntityTag.rememberEntity(event.getEntity());
        entity = new EntityTag(event.getEntity());
        this.event = event;
        fire(event);
        EntityTag.forgetEntity(event.getEntity());
    }
}

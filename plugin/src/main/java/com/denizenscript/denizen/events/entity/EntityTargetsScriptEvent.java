package com.denizenscript.denizen.events.entity;

import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizen.objects.LocationTag;
import com.denizenscript.denizen.utilities.implementation.BukkitScriptEntryData;
import com.denizenscript.denizen.events.BukkitScriptEvent;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.scripts.ScriptEntryData;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityTargetEvent;

public class EntityTargetsScriptEvent extends BukkitScriptEvent implements Listener {

    // <--[event]
    // @Events
    // <entity> targets (<entity>) (because <'cause'>)
    //
    // @Group Entity
    //
    // @Location true
    //
    // @Cancellable true
    //
    // @Triggers when an entity targets a new entity - usually a hostile mob preparing to attack something.
    //
    // @Context
    // <context.entity> returns the targeting entity.
    // <context.reason> returns the reason the entity changed targets. Refer to <@link url https://hub.spigotmc.org/javadocs/spigot/org/bukkit/event/entity/EntityTargetEvent.TargetReason.html>.
    // <context.target> returns the targeted entity.
    //
    // @Determine
    // EntityTag to make the entity target a different entity instead.
    //
    // @Player when the entity being targetted is a player.
    //
    // -->

    public EntityTargetsScriptEvent() {
        registerCouldMatcher("<entity> targets (<entity>) (because <'cause'>)");
    }

    public EntityTag entity;
    public ElementTag reason;
    public EntityTag target;
    private LocationTag location;
    public EntityTargetEvent event;

    @Override
    public boolean matches(ScriptPath path) {
        if (!path.tryArgObject(0, entity)) {
            return false;
        }
        String victim = path.eventArgLowerAt(2);
        if (!victim.equals("in") && !victim.equals("because") && !victim.equals("") && (target == null || !target.tryAdvancedMatcher(victim))) {
            return false;
        }
        if (!runInCheck(path, location)) {
            return false;
        }
        int index = path.eventArgLowerAt(3).equals("because") ? 3 : (path.eventArgAt(2).equals("because") ? 2 : -1);
        if (index > 0 && !path.tryArgObject(index + 1, reason)) {
            return false;
        }
        return super.matches(path);
    }

    @Override
    public boolean applyDetermination(ScriptPath path, ObjectTag determinationObj) {
        String determination = determinationObj.toString();
        if (EntityTag.matches(determination)) {
            target = EntityTag.valueOf(determination, getTagContext(path));
            return true;
        }
        return super.applyDetermination(path, determinationObj);
    }

    @Override
    public ScriptEntryData getScriptEntryData() {
        return new BukkitScriptEntryData(target);
    }

    @Override
    public ObjectTag getContext(String name) {
        return switch (name) {
            case "entity" -> entity.getDenizenObject();
            case "reason" -> reason;
            case "target" -> target == null ? null : target.getDenizenObject();
            default -> super.getContext(name);
        };
    }

    @EventHandler
    public void onEntityTargets(EntityTargetEvent event) {
        entity = new EntityTag(event.getEntity());
        reason = new ElementTag(event.getReason().toString());
        target = event.getTarget() != null ? new EntityTag(event.getTarget()) : null;
        location = new LocationTag(event.getEntity().getLocation());
        this.event = event;
        fire(event);
    }
}

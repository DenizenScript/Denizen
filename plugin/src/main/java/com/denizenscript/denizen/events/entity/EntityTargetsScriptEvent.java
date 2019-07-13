package com.denizenscript.denizen.events.entity;

import com.denizenscript.denizen.objects.dCuboid;
import com.denizenscript.denizen.objects.dEntity;
import com.denizenscript.denizen.objects.dLocation;
import com.denizenscript.denizen.utilities.debugging.Debug;
import com.denizenscript.denizen.BukkitScriptEntryData;
import com.denizenscript.denizen.events.BukkitScriptEvent;
import com.denizenscript.denizencore.objects.ElementTag;
import com.denizenscript.denizencore.objects.ListTag;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.scripts.ScriptEntryData;
import com.denizenscript.denizencore.scripts.containers.ScriptContainer;
import com.denizenscript.denizencore.utilities.CoreUtilities;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityTargetEvent;

public class EntityTargetsScriptEvent extends BukkitScriptEvent implements Listener {

    // <--[event]
    // @Events
    // entity targets (<entity>) (because <cause>)
    // <entity> targets (<entity>) (because <cause>)
    //
    // @Regex ^on [^\s]+ targets( [^\s]+)?( because [^\s]+)?$
    // @Switch in <area>
    //
    // @Cancellable true
    //
    // @Triggers when an entity targets a new entity.
    //
    // @Context
    // <context.entity> returns the targeting entity.
    // <context.reason> returns the reason the entity changed targets.
    // <context.target> returns the targeted entity.
    //
    // @Determine
    // dEntity to make the entity target a different entity instead.
    //
    // @Player when the entity being targetted is a player.
    //
    // -->

    public EntityTargetsScriptEvent() {
        instance = this;
    }

    public static EntityTargetsScriptEvent instance;
    public dEntity entity;
    public ElementTag reason;
    public dEntity target;
    private dLocation location;
    public EntityTargetEvent event;

    @Override
    public boolean couldMatch(ScriptContainer scriptContainer, String s) {
        return CoreUtilities.getXthArg(1, CoreUtilities.toLowerCase(s)).equals("targets");
    }

    @Override
    public boolean matches(ScriptPath path) {

        if (!tryEntity(entity, path.eventArgLowerAt(0))) {
            return false;
        }

        String victim = path.eventArgLowerAt(2);
        if (!victim.equals("in") && !victim.equals("because") && !victim.equals("") && !tryEntity(target, victim)) {
            return false;
        }

        if (!runInCheck(path, location)) {
            return false;
        }

        int index = path.eventArgLowerAt(3).equals("because") ? 3 : (path.eventArgAt(2).equals("because") ? 2 : -1);
        if (index > 0 && !path.eventArgLowerAt(index + 1).equals(CoreUtilities.toLowerCase(reason.toString()))) {
            return false;
        }

        return true;
    }

    @Override
    public String getName() {
        return "EntityTargets";
    }

    @Override
    public boolean applyDetermination(ScriptContainer container, String determination) {
        if (dEntity.matches(determination)) {
            target = dEntity.valueOf(determination);
        }
        return super.applyDetermination(container, determination);
    }

    @Override
    public ScriptEntryData getScriptEntryData() {
        return new BukkitScriptEntryData(entity.isPlayer() ? dEntity.getPlayerFrom(event.getEntity()) : null,
                entity.isCitizensNPC() ? dEntity.getNPCFrom(event.getEntity()) : null);
    }

    @Override
    public ObjectTag getContext(String name) {
        if (name.equals("entity")) {
            return entity.getDenizenObject();
        }
        else if (name.equals("reason")) {
            return reason;
        }
        else if (name.equals("cuboids")) {
            Debug.echoError("context.cuboids tag is deprecated in " + getName() + " script event");
            ListTag cuboids = new ListTag();
            for (dCuboid cuboid : dCuboid.getNotableCuboidsContaining(location)) {
                cuboids.add(cuboid.identifySimple());
            }
            return cuboids;
        }
        else if (name.equals("target") && target != null) {
            return target.getDenizenObject();
        }
        return super.getContext(name);
    }

    @EventHandler
    public void onEntityTargets(EntityTargetEvent event) {
        entity = new dEntity(event.getEntity());
        reason = new ElementTag(event.getReason().toString());
        target = event.getTarget() != null ? new dEntity(event.getTarget()) : null;
        location = new dLocation(event.getEntity().getLocation());
        this.event = event;
        fire(event);
    }

}

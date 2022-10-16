package com.denizenscript.denizen.events.entity;

import com.denizenscript.denizen.events.BukkitScriptEvent;
import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizen.utilities.implementation.BukkitScriptEntryData;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.scripts.ScriptEntryData;
import org.bukkit.entity.Pose;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPoseChangeEvent;

public class EntityChangesPoseScriptEvent extends BukkitScriptEvent implements Listener {

    // <--[event]
    // @Events
    // <entity> changes pose
    //
    // @Group Entity
    //
    // @Location true
    //
    // @Switch old:<pose> to only process the event if the old pose matches the input.
    // @Switch new:<pose> to only process the event if the new pose matches the input.
    //
    // @Triggers when an entity changes its visual pose.
    //
    // @Context
    // <context.entity> returns the EntityTag that changed its pose.
    // <context.old_pose> returns the name of the old pose. See <@link url https://hub.spigotmc.org/javadocs/bukkit/org/bukkit/entity/Pose.html>
    // <context.new_pose> returns the name of the new pose.
    //
    // @Player when the entity that changed its pose is a player.
    //
    // -->

    public EntityChangesPoseScriptEvent() {
        registerCouldMatcher("<entity> changes pose");
        registerSwitches("old", "new");
    }

    public EntityTag entity;
    public Pose oldPose;
    public EntityPoseChangeEvent event;

    @Override
    public boolean matches(ScriptPath path) {
        if (!entity.tryAdvancedMatcher(path.eventArgLowerAt(0))) {
            return false;
        }
        if (!runGenericSwitchCheck(path, "old", oldPose.name())) {
            return false;
        }
        if (!runGenericSwitchCheck(path, "new", event.getPose().name())) {
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
        switch (name) {
            case "entity":
                return entity;
            case "old_pose":
                return new ElementTag(oldPose);
            case "new_pose":
                return new ElementTag(event.getPose());
        }
        return super.getContext(name);
    }

    @EventHandler
    public void onPoseChange(EntityPoseChangeEvent event) {
        entity = new EntityTag(event.getEntity());
        oldPose = entity.getBukkitEntity().getPose();
        this.event = event;
        fire(event);
    }
}

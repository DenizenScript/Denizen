package com.denizenscript.denizen.paper.events;

import com.denizenscript.denizen.events.BukkitScriptEvent;
import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizen.utilities.implementation.BukkitScriptEntryData;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.scripts.ScriptEntryData;
import com.destroystokyo.paper.event.entity.ExperienceOrbMergeEvent;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class ExperienceOrbMergeScriptEvent extends BukkitScriptEvent implements Listener {

    // <--[event]
    // @Events
    // experience orbs merge
    //
    // @Regex ^on experience orbs merge$
    //
    // @Switch in:<area> to only process the event if it occurred within a specified area.
    //
    // @Plugin Paper
    //
    // @Cancellable true
    //
    // @Triggers when two experience orbs are about to merge.
    //
    // @Context
    // <context.target> returns the EntityTag of the orb that will absorb the other experience orb.
    // <context.source> returns the EntityTag of the orb that will be removed and merged into the target.
    //
    // -->

    public ExperienceOrbMergeScriptEvent() {
        instance = this;
    }

    public static ExperienceOrbMergeScriptEvent instance;
    public ExperienceOrbMergeEvent event;

    @Override
    public boolean couldMatch(ScriptPath path) {
        return path.eventLower.startsWith("experience orbs merge");
    }

    @Override
    public boolean matches(ScriptPath path) {
        if (!runInCheck(path, event.getMergeTarget().getLocation())) {
            return false;
        }
        return super.matches(path);
    }

    @Override
    public String getName() {
        return "ExperienceOrbsMerge";
    }

    @Override
    public ScriptEntryData getScriptEntryData() {
        return new BukkitScriptEntryData(null, null);
    }

    @Override
    public ObjectTag getContext(String name) {
        if (name.equals("target")) {
            return new EntityTag(event.getMergeTarget());
        }
        else if (name.equals("source")) {
            return new EntityTag(event.getMergeSource());
        }
        return super.getContext(name);
    }

    @EventHandler
    public void experienceOrbsMerge(ExperienceOrbMergeEvent event) {
        Entity target = event.getMergeTarget();
        Entity source = event.getMergeSource();
        EntityTag.rememberEntity(target);
        EntityTag.rememberEntity(source);
        this.event = event;
        fire(event);
        EntityTag.forgetEntity(target);
        EntityTag.forgetEntity(source);
    }
}

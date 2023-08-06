package com.denizenscript.denizen.events.entity;

import com.denizenscript.denizen.events.BukkitScriptEvent;
import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizencore.objects.Argument;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ExpBottleEvent;

public class ExperienceBottleBreaksScriptEvent extends BukkitScriptEvent implements Listener {

    // <--[event]
    // @Events
    // experience bottle breaks
    //
    // @Regex ^on experience bottle breaks$
    //
    // @Group Entity
    //
    // @Location true
    //
    // @Cancellable true
    //
    // @Triggers when a thrown experience bottle breaks.
    //
    // @Context
    // <context.entity> returns the EntityTag of the thrown experience bottle.
    // <context.experience> returns the amount of experience to be spawned.
    // <context.show_effect> returns whether the effect should be shown.
    //
    // @Determine
    // "EXPERIENCE:<ElementTag(Number)>" to specify the amount of experience to be created.
    // "EFFECT:<ElementTag(Boolean)>" to specify if the particle effects will be shown.
    //
    // -->

    public ExperienceBottleBreaksScriptEvent() {
    }

    public ExpBottleEvent event;

    @Override
    public boolean couldMatch(ScriptPath path) {
        if (!path.eventLower.startsWith("experience bottle breaks")) {
            return false;
        }
        return true;
    }

    @Override
    public boolean matches(ScriptPath path) {
        if (!runInCheck(path, event.getEntity().getLocation())) {
            return false;
        }
        return super.matches(path);
    }

    @Override
    public boolean applyDetermination(ScriptPath path, ObjectTag determinationObj) {
        String lower = determinationObj.toString().toLowerCase();
        if (lower.startsWith("experience:")) {
            int experience = Argument.valueOf(lower.substring(11)).asElement().asInt();
            event.setExperience(experience);
        }
        else if (lower.startsWith("effect:")) {
            boolean effect = Argument.valueOf(lower.substring(7)).asElement().asBoolean();
            event.setShowEffect(effect);
        }
        else {
            return super.applyDetermination(path, determinationObj);
        }
        return true;
    }

    @Override
    public ObjectTag getContext(String name) {
        switch (name) {
            case "entity":
                return new EntityTag(event.getEntity()).getDenizenObject();
            case "experience":
                return new ElementTag(event.getExperience());
            case "show_effect":
                return new ElementTag(event.getShowEffect());
        }
        return super.getContext(name);
    }

    @EventHandler
    public void onExperienceBottleBreaks(ExpBottleEvent event) {
        this.event = event;
        fire(event);
    }
}

package com.denizenscript.denizen.events.entity;

import com.denizenscript.denizen.events.BukkitScriptEvent;
import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.scripts.containers.ScriptContainer;
import com.denizenscript.denizencore.utilities.CoreUtilities;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ExpBottleEvent;

public class ExpBottleBreaksScriptEvent extends BukkitScriptEvent implements Listener {

    // <--[event]
    // @Events
    // exp bottle breaks
    //
    // @Regex ^on exp bottle breaks$
    // @Switch in <area>
    //
    // @Cancellable true
    //
    // @Triggers when a thrown exp bottle breaks.
    //
    // @Context
    // <context.bottle> returns the EntityTag of the thrown exp bottle.
    // <context.experience> returns the amount of experience to be spawned.
    // <context.show_effect> returns whether the effect should be shown.
    //
    // -->

    public ExpBottleBreaksScriptEvent() {
        instance = this;
    }

    public static ExpBottleBreaksScriptEvent instance;
    private EntityTag bottle;
    private ElementTag experience;
    private ElementTag showEffect;
    private ExpBottleEvent event;

    @Override
    public boolean couldMatch(ScriptContainer scriptContainer, String s) {
        String lower = CoreUtilities.toLowerCase(s);
        return (lower.equals("exp bottle breaks"));
    }

    @Override
    public boolean matches(ScriptPath path) {
        return runInCheck(path, bottle.getLocation());
    }

    @Override
    public String getName() {
        return "ExpBottleBreaks";
    }

    @Override
    public ObjectTag getContext(String name) {
        if (name.equals("entity")) {
            return bottle;
        }
        else if (name.equals("experience")) {
            return experience;
        }
        else if (name.equals("show_effect")) {
            return showEffect;
        }
        return super.getContext(name);
    }

    @EventHandler
    public void onExpBottleBreaks(ExpBottleEvent event) {
        bottle = new EntityTag(event.getEntity());
        experience = new ElementTag(event.getExperience());
        showEffect = new ElementTag(event.getShowEffect());
        this.event = event;
        fire(event);
    }
}

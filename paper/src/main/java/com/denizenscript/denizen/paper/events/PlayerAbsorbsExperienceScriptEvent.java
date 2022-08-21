package com.denizenscript.denizen.paper.events;

import com.denizenscript.denizen.events.BukkitScriptEvent;
import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizen.utilities.implementation.BukkitScriptEntryData;
import com.denizenscript.denizencore.events.ScriptEvent;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.scripts.ScriptEntryData;
import com.destroystokyo.paper.event.player.PlayerPickupExperienceEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class PlayerAbsorbsExperienceScriptEvent extends BukkitScriptEvent implements Listener {

    // <--[event]
    // @Events
    // player absorbs experience
    //
    // @Location true
    //
    // @Plugin Paper
    //
    // @Group Paper
    //
    // @Cancellable true
    //
    // @Triggers when a player is absorbing an experience orb.
    //
    // @Context
    // <context.entity> returns the EntityTag of the experience orb.
    //
    // @Player Always.
    //
    // -->

    public PlayerAbsorbsExperienceScriptEvent() {
        registerCouldMatcher("player absorbs experience");
    }

    public PlayerPickupExperienceEvent event;

    @Override
    public boolean matches(ScriptEvent.ScriptPath path) {
        if (!runInCheck(path, event.getPlayer().getLocation())) {
            return false;
        }
        return super.matches(path);
    }

    @Override
    public ScriptEntryData getScriptEntryData() {
        return new BukkitScriptEntryData(event.getPlayer());
    }

    @Override
    public ObjectTag getContext(String name) {
        if (name.equals("entity")) {
            return new EntityTag(event.getExperienceOrb());
        }
        return super.getContext(name);
    }

    @EventHandler
    public void absorbsExperience(PlayerPickupExperienceEvent event) {
        this.event = event;
        fire(event);
    }
}

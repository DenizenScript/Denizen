package com.denizenscript.denizen.events.player;

import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizen.objects.LocationTag;
import com.denizenscript.denizen.objects.PlayerTag;
import com.denizenscript.denizen.BukkitScriptEntryData;
import com.denizenscript.denizen.events.BukkitScriptEvent;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.scripts.ScriptEntryData;
import com.denizenscript.denizencore.scripts.containers.ScriptContainer;
import com.denizenscript.denizencore.utilities.CoreUtilities;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerAnimationEvent;

public class PlayerAnimatesScriptEvent extends BukkitScriptEvent implements Listener {

    // <--[event]
    // @Events
    // player animates (<animation>)
    //
    // @Regex ^on player animates [^\s]+$
    // @Switch in <area>
    //
    // @Cancellable true
    //
    // @Triggers when a player performs an animation.
    //
    // @Context
    // <context.animation> returns the name of the animation.
    //
    // -->

    public PlayerAnimatesScriptEvent() {
        instance = this;
    }

    public static PlayerAnimatesScriptEvent instance;
    public String animation;
    private LocationTag location;
    public PlayerAnimationEvent event;

    @Override
    public boolean couldMatch(ScriptContainer scriptContainer, String s) {
        return CoreUtilities.toLowerCase(s).startsWith("player animates");
    }

    @Override
    public boolean matches(ScriptPath path) {
        if (EntityTag.isNPC(event.getPlayer())) {
            return false;
        }

        String ani = path.eventArgLowerAt(2);
        if (ani.length() > 0 && !ani.equals("in") && !ani.equalsIgnoreCase(animation)) {
            return false;
        }

        return runInCheck(path, location);
    }

    @Override
    public String getName() {
        return "PlayerAnimates";
    }

    @Override
    public ScriptEntryData getScriptEntryData() {
        return new BukkitScriptEntryData(new PlayerTag(event.getPlayer()), null);
    }

    @Override
    public ObjectTag getContext(String name) {
        if (name.equals("animation")) {
            return new ElementTag(animation);
        }
        return super.getContext(name);
    }

    @EventHandler
    public void onPlayerAnimates(PlayerAnimationEvent event) {
        if (EntityTag.isNPC(event.getPlayer())) {
            return;
        }
        location = new LocationTag(event.getPlayer().getLocation());
        animation = event.getAnimationType().name();
        this.event = event;
        fire(event);
    }
}

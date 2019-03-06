package net.aufdemrand.denizen.events.player;

import net.aufdemrand.denizen.BukkitScriptEntryData;
import net.aufdemrand.denizen.events.BukkitScriptEvent;
import net.aufdemrand.denizen.objects.dEntity;
import net.aufdemrand.denizen.objects.dLocation;
import net.aufdemrand.denizen.objects.dPlayer;
import net.aufdemrand.denizen.utilities.DenizenAPI;
import net.aufdemrand.denizencore.objects.Element;
import net.aufdemrand.denizencore.objects.dObject;
import net.aufdemrand.denizencore.scripts.ScriptEntryData;
import net.aufdemrand.denizencore.scripts.containers.ScriptContainer;
import net.aufdemrand.denizencore.utilities.CoreUtilities;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerAnimationEvent;

public class PlayerAnimatesScriptEvent extends BukkitScriptEvent implements Listener {

    // <--[event]
    // @Events
    // player animates (<animation>) (in <area>)
    //
    // @Regex ^on player animates [^\s]+( in ((notable (cuboid|ellipsoid))|([^\s]+)))?$
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
    private dLocation location;
    public PlayerAnimationEvent event;

    @Override
    public boolean couldMatch(ScriptContainer scriptContainer, String s) {
        return CoreUtilities.toLowerCase(s).startsWith("player animates");
    }

    @Override
    public boolean matches(ScriptPath path) {
        String s = path.event;
        String lower = path.eventLower;
        if (dEntity.isNPC(event.getPlayer())) {
            return false;
        }

        String ani = CoreUtilities.getXthArg(2, lower);
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
    public void destroy() {
        PlayerAnimationEvent.getHandlerList().unregister(this);
    }

    @Override
    public boolean applyDetermination(ScriptContainer container, String determination) {
        return super.applyDetermination(container, determination);
    }

    @Override
    public ScriptEntryData getScriptEntryData() {
        return new BukkitScriptEntryData(new dPlayer(event.getPlayer()), null);
    }

    @Override
    public dObject getContext(String name) {
        if (name.equals("animation")) {
            return new Element(animation);
        }
        return super.getContext(name);
    }

    @EventHandler
    public void onPlayerAnimates(PlayerAnimationEvent event) {
        if (dEntity.isNPC(event.getPlayer())) {
            return;
        }
        location = new dLocation(event.getPlayer().getLocation());
        animation = event.getAnimationType().name();
        cancelled = event.isCancelled();
        this.event = event;
        fire();
        event.setCancelled(cancelled);
    }
}

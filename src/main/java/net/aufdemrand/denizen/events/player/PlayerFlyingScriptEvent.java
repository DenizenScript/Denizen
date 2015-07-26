package net.aufdemrand.denizen.events.player;

import net.aufdemrand.denizen.BukkitScriptEntryData;
import net.aufdemrand.denizen.events.BukkitScriptEvent;
import net.aufdemrand.denizen.objects.dEntity;
import net.aufdemrand.denizen.utilities.DenizenAPI;
import net.aufdemrand.denizencore.objects.Element;
import net.aufdemrand.denizencore.objects.dObject;
import net.aufdemrand.denizencore.scripts.ScriptEntryData;
import net.aufdemrand.denizencore.scripts.containers.ScriptContainer;
import net.aufdemrand.denizencore.utilities.CoreUtilities;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerToggleFlightEvent;

import java.util.HashMap;

public class PlayerFlyingScriptEvent extends BukkitScriptEvent implements Listener {

    // <--[event]
    // @Events
    // player toggles flying (in <area>)
    // player starts flying (in <area>)
    // player stops flying (in <area>)
    //
    // @Regex ^on player (toggles|starts|stops) flying( in ((notable (cuboid|ellipsoid))|([^\s]+)))?$
    //
    // @Cancellable true
    //
    // @Triggers when a player starts or stops flying.
    //
    // @Context
    // <context.state> returns an Element(Boolean) with a value of "true" if the player is now flying and "false" otherwise.
    //
    // -->

    public PlayerFlyingScriptEvent() {
        instance = this;
    }

    public static PlayerFlyingScriptEvent instance;
    public Boolean state;
    public PlayerToggleFlightEvent event;

    @Override
    public boolean couldMatch(ScriptContainer scriptContainer, String s) {
        return CoreUtilities.getXthArg(2, CoreUtilities.toLowerCase(s)).equals("flying")
                || CoreUtilities.getXthArg(2, CoreUtilities.toLowerCase(s)).equals("flight");
    }

    @Override
    public boolean matches(ScriptContainer scriptContainer, String s) {
        String lower = CoreUtilities.toLowerCase(s);
        String cmd = CoreUtilities.getXthArg(1, lower);
        if (cmd.equals("starts") && !state) {
            return false;
        }
        if (cmd.equals("stops") && state) {
            return false;
        }

        return runInCheck(scriptContainer, s, lower, event.getPlayer().getLocation());
    }

    @Override
    public String getName() {
        return "PlayerSpring";
    }

    @Override
    public void init() {
        Bukkit.getServer().getPluginManager().registerEvents(this, DenizenAPI.getCurrentInstance());
    }

    @Override
    public void destroy() {
        PlayerToggleFlightEvent.getHandlerList().unregister(this);
    }

    @Override
    public boolean applyDetermination(ScriptContainer container, String determination) {
        return super.applyDetermination(container, determination);
    }

    @Override
    public ScriptEntryData getScriptEntryData() {
        return new BukkitScriptEntryData(dEntity.isPlayer(event.getPlayer()) ? dEntity.getPlayerFrom(event.getPlayer()) : null, null);
    }

    @Override
    public HashMap<String, dObject> getContext() {
        HashMap<String, dObject> context = super.getContext();
        context.put("state", new Element(state));
        return context;
    }

    @EventHandler
    public void onPlayerFlying(PlayerToggleFlightEvent event) {
        if (dEntity.isNPC(event.getPlayer())) {
            return;
        }
        state = event.isFlying();
        cancelled = event.isCancelled();
        this.event = event;
        fire();
        event.setCancelled(cancelled);
    }
}

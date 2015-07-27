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
import org.bukkit.event.player.PlayerToggleSprintEvent;

import java.util.HashMap;

public class PlayerSprintScriptEvent extends BukkitScriptEvent implements Listener {

    // <--[event]
    // @Events
    // player toggles sprinting (in <area>)
    // player starts sprinting (in <area>)
    // player stops sprinting (in <area>)
    //
    // @Regex ^on player (toggles|starts\stops) sprinting( in ((notable (cuboid|ellipsoid))|([^\s]+)))?$
    //
    // @Cancellable true
    //
    // @Triggers when a player starts or stops sprinting.
    //
    // @Context
    // <context.state> returns an Element(Boolean) with a value of "true" if the player is now sprinting and "false" otherwise.
    //
    // -->

    public PlayerSprintScriptEvent() {
        instance = this;
    }

    public static PlayerSprintScriptEvent instance;
    public Boolean state;
    public PlayerToggleSprintEvent event;

    @Override
    public boolean couldMatch(ScriptContainer scriptContainer, String s) {
        return CoreUtilities.getXthArg(2, CoreUtilities.toLowerCase(s)).startsWith("sprint");
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
        PlayerToggleSprintEvent.getHandlerList().unregister(this);
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

    @EventHandler(ignoreCancelled = true)
    public void onPlayerSprint(PlayerToggleSprintEvent event) {
        if (dEntity.isNPC(event.getPlayer())) {
            return;
        }
        state = event.isSprinting();
        cancelled = event.isCancelled();
        this.event = event;
        fire();
        event.setCancelled(cancelled);
    }
}

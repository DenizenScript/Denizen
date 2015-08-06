package net.aufdemrand.denizen.events.player;

import net.aufdemrand.denizen.BukkitScriptEntryData;
import net.aufdemrand.denizen.events.BukkitScriptEvent;
import net.aufdemrand.denizen.objects.dEntity;
import net.aufdemrand.denizen.objects.dLocation;
import net.aufdemrand.denizen.utilities.DenizenAPI;
import net.aufdemrand.denizencore.objects.dObject;
import net.aufdemrand.denizencore.scripts.ScriptEntryData;
import net.aufdemrand.denizencore.scripts.containers.ScriptContainer;
import net.aufdemrand.denizencore.utilities.CoreUtilities;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerRespawnEvent;

public class PlayerRespawnsScriptEvent extends BukkitScriptEvent implements Listener {

    // <--[event]
    // @Events
    // player respawns (at bed/elsewhere)
    //
    // @Regex ^on player respawns( at (bed|elsewhere))?$
    //
    // @Cancellable true
    //
    // @Triggers when a player respawns.
    //
    // @Context
    // <context.location> returns a dLocation of the respawn location.
    //
    // @Determine
    // dLocation to change the respawn location.
    //
    // -->

    public PlayerRespawnsScriptEvent() {
        instance = this;
    }

    public static PlayerRespawnsScriptEvent instance;
    public dLocation location;
    public PlayerRespawnEvent event;

    @Override
    public boolean couldMatch(ScriptContainer scriptContainer, String s) {
        return CoreUtilities.toLowerCase(s).startsWith("player respawns");
    }

    @Override
    public boolean matches(ScriptContainer scriptContainer, String s) {
        String lower = CoreUtilities.toLowerCase(s);
        String loc = CoreUtilities.getXthArg(2, lower);
        if (loc.equals("at") && !event.isBedSpawn()) {
            return false;
        }
        if (loc.equals("elsewhere") && event.isBedSpawn()) {
            return false;
        }
        return true;
    }

    @Override
    public String getName() {
        return "PlayerRespawns";
    }

    @Override
    public void init() {
        Bukkit.getServer().getPluginManager().registerEvents(this, DenizenAPI.getCurrentInstance());
    }

    @Override
    public void destroy() {
        PlayerRespawnEvent.getHandlerList().unregister(this);
    }

    @Override
    public boolean applyDetermination(ScriptContainer container, String determination) {
        if (!CoreUtilities.toLowerCase(determination).equals("none")) {
            dLocation loc = dLocation.valueOf(determination);
            if (loc != null) {
                location = loc;
                return true;
            }
        }
        return super.applyDetermination(container, determination);
    }

    @Override
    public ScriptEntryData getScriptEntryData() {
        return new BukkitScriptEntryData(dEntity.isPlayer(event.getPlayer()) ? dEntity.getPlayerFrom(event.getPlayer()) : null, null);
    }

    @Override
    public dObject getContext(String name) {
        if (name.equals("location")) {
            return location;
        }
        return super.getContext(name);
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerRespawns(PlayerRespawnEvent event) {
        if (dEntity.isNPC(event.getPlayer())) {
            return;
        }
        location = new dLocation(event.getRespawnLocation());
        this.event = event;
        fire();
        event.setRespawnLocation(location);
    }
}

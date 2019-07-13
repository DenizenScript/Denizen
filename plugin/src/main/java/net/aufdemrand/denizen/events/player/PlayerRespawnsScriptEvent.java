package net.aufdemrand.denizen.events.player;

import net.aufdemrand.denizen.BukkitScriptEntryData;
import net.aufdemrand.denizen.events.BukkitScriptEvent;
import net.aufdemrand.denizen.objects.dEntity;
import net.aufdemrand.denizen.objects.dLocation;
import net.aufdemrand.denizen.objects.dPlayer;
import com.denizenscript.denizencore.objects.dObject;
import com.denizenscript.denizencore.scripts.ScriptEntryData;
import com.denizenscript.denizencore.scripts.containers.ScriptContainer;
import com.denizenscript.denizencore.utilities.CoreUtilities;
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
    public boolean matches(ScriptPath path) {
        String loc = path.eventArgLowerAt(2);
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
        return new BukkitScriptEntryData(new dPlayer(event.getPlayer()), null);
    }

    @Override
    public dObject getContext(String name) {
        if (name.equals("location")) {
            return location;
        }
        return super.getContext(name);
    }

    @EventHandler
    public void onPlayerRespawns(PlayerRespawnEvent event) {
        if (dEntity.isNPC(event.getPlayer())) {
            return;
        }
        location = new dLocation(event.getRespawnLocation());
        this.event = event;
        fire(event);
        event.setRespawnLocation(location);
    }
}

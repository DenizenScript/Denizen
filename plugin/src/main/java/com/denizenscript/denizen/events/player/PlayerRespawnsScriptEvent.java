package com.denizenscript.denizen.events.player;

import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizen.objects.LocationTag;
import com.denizenscript.denizen.objects.PlayerTag;
import com.denizenscript.denizen.BukkitScriptEntryData;
import com.denizenscript.denizen.events.BukkitScriptEvent;
import com.denizenscript.denizencore.objects.ObjectTag;
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
    // <context.location> returns a LocationTag of the respawn location.
    //
    // @Determine
    // LocationTag to change the respawn location.
    //
    // -->

    public PlayerRespawnsScriptEvent() {
        instance = this;
    }

    public static PlayerRespawnsScriptEvent instance;
    public LocationTag location;
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
            LocationTag loc = LocationTag.valueOf(determination);
            if (loc != null) {
                location = loc;
                return true;
            }
        }
        return super.applyDetermination(container, determination);
    }

    @Override
    public ScriptEntryData getScriptEntryData() {
        return new BukkitScriptEntryData(new PlayerTag(event.getPlayer()), null);
    }

    @Override
    public ObjectTag getContext(String name) {
        if (name.equals("location")) {
            return location;
        }
        return super.getContext(name);
    }

    @EventHandler
    public void onPlayerRespawns(PlayerRespawnEvent event) {
        if (EntityTag.isNPC(event.getPlayer())) {
            return;
        }
        location = new LocationTag(event.getRespawnLocation());
        this.event = event;
        fire(event);
        event.setRespawnLocation(location);
    }
}

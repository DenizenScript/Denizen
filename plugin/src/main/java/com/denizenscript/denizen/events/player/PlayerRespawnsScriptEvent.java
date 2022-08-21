package com.denizenscript.denizen.events.player;

import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizen.objects.LocationTag;
import com.denizenscript.denizen.utilities.implementation.BukkitScriptEntryData;
import com.denizenscript.denizen.events.BukkitScriptEvent;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.scripts.ScriptEntryData;
import com.denizenscript.denizencore.utilities.CoreUtilities;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerRespawnEvent;

public class PlayerRespawnsScriptEvent extends BukkitScriptEvent implements Listener {

    // <--[event]
    // @Events
    // player respawns (at bed)
    // player respawns elsewhere
    //
    // @Group Player
    //
    // @Triggers when a player respawns.
    //
    // @Context
    // <context.location> returns a LocationTag of the respawn location.
    // <context.is_bed_spawn> returns a boolean indicating whether the player is about to respawn at their bed.
    //
    // @Determine
    // LocationTag to change the respawn location.
    //
    // @Player Always.
    //
    // -->

    public PlayerRespawnsScriptEvent() {
        registerCouldMatcher("player respawns (at bed)");
        registerCouldMatcher("player respawns elsewhere");
    }

    public PlayerRespawnEvent event;

    @Override
    public boolean matches(ScriptPath path) {
        String loc = path.eventArgLowerAt(2);
        if (loc.equals("at") && !event.isBedSpawn()) {
            return false;
        }
        if (loc.equals("elsewhere") && event.isBedSpawn()) {
            return false;
        }
        return super.matches(path);
    }

    @Override
    public boolean applyDetermination(ScriptPath path, ObjectTag determinationObj) {
        String determination = determinationObj.toString();
        if (!CoreUtilities.equalsIgnoreCase(determination, "none")) {
            LocationTag loc = LocationTag.valueOf(determination, getTagContext(path));
            if (loc != null) {
                event.setRespawnLocation(loc);
                return true;
            }
        }
        return super.applyDetermination(path, determinationObj);
    }

    @Override
    public ScriptEntryData getScriptEntryData() {
        return new BukkitScriptEntryData(event.getPlayer());
    }

    @Override
    public ObjectTag getContext(String name) {
        if (name.equals("location")) {
            return new LocationTag(event.getRespawnLocation());
        }
        else if (name.equals("is_bed_spawn")) {
            return new ElementTag(event.isBedSpawn());
        }
        return super.getContext(name);
    }

    @EventHandler
    public void onPlayerRespawns(PlayerRespawnEvent event) {
        if (EntityTag.isNPC(event.getPlayer())) {
            return;
        }
        this.event = event;
        fire(event);
    }
}

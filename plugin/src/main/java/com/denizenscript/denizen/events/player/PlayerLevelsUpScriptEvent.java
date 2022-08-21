package com.denizenscript.denizen.events.player;

import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizen.objects.PlayerTag;
import com.denizenscript.denizen.utilities.implementation.BukkitScriptEntryData;
import com.denizenscript.denizen.events.BukkitScriptEvent;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.scripts.ScriptEntryData;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLevelChangeEvent;

public class PlayerLevelsUpScriptEvent extends BukkitScriptEvent implements Listener {

    // <--[event]
    // @Events
    // player levels up (from <'level'>) (to <'level'>)
    //
    // @Group Player
    //
    // @Location true
    //
    // @Triggers when a player levels up.
    //
    // @Context
    // <context.new_level> returns an ElementTag of the player's new level.
    // <context.old_level> returns an ElementTag of the player's old level.
    //
    // @Player Always.
    //
    // -->

    public PlayerLevelsUpScriptEvent() {
        registerCouldMatcher("player levels up (from <'level'>) (to <'level'>)");
    }

    public int new_level;
    public int old_level;
    public PlayerTag player;
    public PlayerLevelChangeEvent event;

    @Override
    public boolean matches(ScriptPath path) {
        String[] data = path.eventArgsLower;
        for (int index = 3; index < data.length; index++) {
            if (data[index].equals("from")) {
                if (Integer.parseInt(data[index + 1]) != old_level) {
                    return false;
                }
            }
            if (data[index].equals("to")) {
                if (Integer.parseInt(data[index + 1]) != new_level) {
                    return false;
                }
            }
        }

        if (!runInCheck(path, player.getLocation())) {
            return false;
        }

        return super.matches(path);
    }

    @Override
    public ScriptEntryData getScriptEntryData() {
        return new BukkitScriptEntryData(player, null);
    }

    @Override
    public ObjectTag getContext(String name) {
        if (name.equals("level") || name.equals("new_level")) {
            return new ElementTag(new_level);
        }
        else if (name.equals("old_level")) {
            return new ElementTag(old_level);
        }
        return super.getContext(name);
    }

    @EventHandler
    public void onPlayerLevels(PlayerLevelChangeEvent event) {
        if (EntityTag.isNPC(event.getPlayer())) {
            return;
        }
        player = PlayerTag.mirrorBukkitPlayer(event.getPlayer());
        old_level = event.getOldLevel();
        new_level = event.getNewLevel();
        this.event = event;
        fire(event);
    }
}

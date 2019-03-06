package net.aufdemrand.denizen.events.player;

import net.aufdemrand.denizen.BukkitScriptEntryData;
import net.aufdemrand.denizen.events.BukkitScriptEvent;
import net.aufdemrand.denizen.objects.dEntity;
import net.aufdemrand.denizen.objects.dPlayer;
import net.aufdemrand.denizen.utilities.DenizenAPI;
import net.aufdemrand.denizencore.objects.Element;
import net.aufdemrand.denizencore.objects.aH;
import net.aufdemrand.denizencore.objects.dObject;
import net.aufdemrand.denizencore.scripts.ScriptEntryData;
import net.aufdemrand.denizencore.scripts.containers.ScriptContainer;
import net.aufdemrand.denizencore.utilities.CoreUtilities;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLevelChangeEvent;

import java.util.List;

public class PlayerLevelsUpScriptEvent extends BukkitScriptEvent implements Listener {

    // <--[event]
    // @Events
    // player levels up (from <level>) (to <level>) (in <area>)
    //
    // @Regex ^on player levels up( from [^\s]+)?( to [^\s]+)?( in ((notable (cuboid|ellipsoid))|([^\s]+)))?$
    //
    // @Triggers when a player levels up.
    //
    // @Context
    // <context.new_level> returns an Element of the player's new level.
    // <context.old_level> returns an Element of the player's old level.
    //
    // -->

    public PlayerLevelsUpScriptEvent() {
        instance = this;
    }

    public static PlayerLevelsUpScriptEvent instance;
    public int new_level;
    public int old_level;
    public dPlayer player;
    public PlayerLevelChangeEvent event;

    @Override
    public boolean couldMatch(ScriptContainer scriptContainer, String s) {
        return CoreUtilities.toLowerCase(s).startsWith("player levels up");
    }

    @Override
    public boolean matches(ScriptPath path) {
        String s = path.event;
        String lower = path.eventLower;
        List<String> data = CoreUtilities.split(lower, ' ');
        for (int index = 0; index < data.size(); index++) {
            if (data.get(index).equals("from")) {
                if (aH.getIntegerFrom(data.get(index + 1)) != old_level) {
                    return false;
                }
            }
            if (data.get(index).equals("to")) {
                if (aH.getIntegerFrom(data.get(index + 1)) != new_level) {
                    return false;
                }
            }
        }

        if (!runInCheck(path, player.getLocation())) {
            return false;
        }

        return true;
    }

    @Override
    public String getName() {
        return "PlayerLevelsUp";
    }

    @Override
    public void destroy() {
        PlayerLevelChangeEvent.getHandlerList().unregister(this);
    }

    @Override
    public boolean applyDetermination(ScriptContainer container, String determination) {
        return super.applyDetermination(container, determination);
    }

    @Override
    public ScriptEntryData getScriptEntryData() {
        return new BukkitScriptEntryData(player, null);
    }

    @Override
    public dObject getContext(String name) {
        if (name.equals("level") || name.equals("new_level")) {
            return new Element(new_level);
        }
        else if (name.equals("old_level")) {
            return new Element(old_level);
        }
        return super.getContext(name);
    }

    @EventHandler
    public void onPlayerLevels(PlayerLevelChangeEvent event) {
        if (dEntity.isNPC(event.getPlayer())) {
            return;
        }
        player = dPlayer.mirrorBukkitPlayer(event.getPlayer());
        old_level = event.getOldLevel();
        new_level = event.getNewLevel();
        this.event = event;
        fire();
    }
}

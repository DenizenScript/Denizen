package net.aufdemrand.denizen.events.player;

import net.aufdemrand.denizen.BukkitScriptEntryData;
import net.aufdemrand.denizen.events.BukkitScriptEvent;
import net.aufdemrand.denizen.objects.dEntity;
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

public class PlayerLevelsScriptEvent extends BukkitScriptEvent implements Listener {

    // <--[event]
    // @Events
    // player levels up (from <level>) (to <level>)
    //
    // @Regex ^on player levels up( from [^\s]+)?( to [^\s]+)?$
    //
    // @Cancellable false
    //
    // @Triggers when a player levels an entity.
    //
    // @Context
    // <context.level> returns an Element of the player's new level.
    //
    // -->

    public PlayerLevelsScriptEvent() {
        instance = this;
    }

    public static PlayerLevelsScriptEvent instance;
    public int level;
    public PlayerLevelChangeEvent event;

    @Override
    public boolean couldMatch(ScriptContainer scriptContainer, String s) {
        return CoreUtilities.toLowerCase(s).startsWith("player levels up");
    }

    @Override
    public boolean matches(ScriptContainer scriptContainer, String s) {
        String lower = CoreUtilities.toLowerCase(s);
        List<String> data = CoreUtilities.split(lower, ' ');
        for (int index = 0; index < data.size(); index++) {
            if (data.get(index).equals("from")) {
                if (aH.getIntegerFrom(data.get(index + 1)) != event.getOldLevel()) {
                    return false;
                }
            }
            if (data.get(index).equals("to")) {
                if (aH.getIntegerFrom(data.get(index + 1)) != event.getNewLevel()) {
                    return false;
                }
            }
        }

        return true;
    }

    @Override
    public String getName() {
        return "PlayerLevels";
    }

    @Override
    public void init() {
        Bukkit.getServer().getPluginManager().registerEvents(this, DenizenAPI.getCurrentInstance());
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
        dEntity player = new dEntity(event.getPlayer());
        return new BukkitScriptEntryData(player.isPlayer() ? player.getDenizenPlayer() : null, null);
    }

    @Override
    public dObject getContext(String name) {
        if (name.equals("level")) {
            return new Element(level);
        }
        return super.getContext(name);
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerLevels(PlayerLevelChangeEvent event) {
        if (dEntity.isNPC(event.getPlayer())) {
            return;
        }
        level = event.getNewLevel();
        this.event = event;
        fire();
    }
}

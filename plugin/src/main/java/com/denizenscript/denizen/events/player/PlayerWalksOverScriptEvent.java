package com.denizenscript.denizen.events.player;

import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizen.objects.LocationTag;
import com.denizenscript.denizen.objects.PlayerTag;
import com.denizenscript.denizen.objects.notable.NotableManager;
import com.denizenscript.denizen.utilities.implementation.BukkitScriptEntryData;
import com.denizenscript.denizen.events.BukkitScriptEvent;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.scripts.ScriptEntryData;
import com.denizenscript.denizencore.utilities.CoreUtilities;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

public class PlayerWalksOverScriptEvent extends BukkitScriptEvent implements Listener {

    // <--[event]
    // @Events
    // player walks over notable
    // player walks over <location>
    //
    // @Regex ^on player walks over [^\s]+$
    //
    // @Switch in:<area> to only process the event if it occurred within a specified area.
    //
    // @Cancellable true
    //
    // @Triggers when a player walks over a notable location.
    //
    // @Context
    // <context.notable> returns an ElementTag of the notable location's name.
    //
    // @Player Always.
    //
    // -->

    public PlayerWalksOverScriptEvent() {
        instance = this;
    }

    public static PlayerWalksOverScriptEvent instance;
    public String notable;
    public PlayerMoveEvent event;

    @Override
    public boolean couldMatch(ScriptPath path) {
        return path.eventLower.startsWith("player walks over");
    }

    @Override
    public boolean matches(ScriptPath path) {
        String loc = path.eventArgLowerAt(3);
        return loc.equals(CoreUtilities.toLowerCase(notable)) || tryLocation(new LocationTag(event.getPlayer().getLocation()), loc);
    }

    @Override
    public String getName() {
        return "PlayerWalksOver";
    }

    @Override
    public ScriptEntryData getScriptEntryData() {
        return new BukkitScriptEntryData(new PlayerTag(event.getPlayer()), null);
    }

    @Override
    public ObjectTag getContext(String name) {
        if (name.equals("notable")) {
            return new ElementTag(notable);
        }
        return super.getContext(name);
    }

    @EventHandler
    public void onPlayerWalksOver(PlayerMoveEvent event) {
        if (event.getFrom().getBlock().equals(event.getTo().getBlock())) {
            return;
        }
        if (EntityTag.isNPC(event.getPlayer())) {
            return;
        }
        notable = NotableManager.getSavedId(new LocationTag(event.getTo().getBlock().getLocation()));
        if (notable == null) {
            return;
        }
        this.event = event;
        fire(event);
    }
}

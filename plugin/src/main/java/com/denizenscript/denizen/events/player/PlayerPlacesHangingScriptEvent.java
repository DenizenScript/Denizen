package com.denizenscript.denizen.events.player;

import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizen.objects.LocationTag;
import com.denizenscript.denizen.utilities.implementation.BukkitScriptEntryData;
import com.denizenscript.denizen.events.BukkitScriptEvent;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.scripts.ScriptEntryData;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.hanging.HangingPlaceEvent;

public class PlayerPlacesHangingScriptEvent extends BukkitScriptEvent implements Listener {

    // <--[event]
    // @Events
    // player places hanging
    // player places <hanging>
    //
    // @Regex ^on player places [^\s]+$
    //
    // @Group Player
    //
    // @Location true
    //
    // @Cancellable true
    //
    // @Triggers when a hanging entity (painting or itemframe) is placed.
    //
    // @Context
    // <context.hanging> returns the EntityTag of the hanging.
    // <context.location> returns the LocationTag of the block the hanging was placed on.
    //
    // @Player Always.
    //
    // -->

    public PlayerPlacesHangingScriptEvent() {
        instance = this;
    }

    public static PlayerPlacesHangingScriptEvent instance;
    public EntityTag hanging;
    public LocationTag location;
    public HangingPlaceEvent event;

    @Override
    public boolean couldMatch(ScriptPath path) {
        if (!path.eventLower.startsWith("player places")) {
            return false;
        }
        if (!couldMatchEntity(path.eventArgLowerAt(2))) {
            return false;
        }
        return true;
    }

    @Override
    public boolean matches(ScriptPath path) {
        String hangCheck = path.eventArgLowerAt(2);
        if (!hanging.tryAdvancedMatcher(hangCheck)) {
            return false;
        }
        if (!runInCheck(path, location)) {
            return false;
        }
        return super.matches(path);
    }

    @Override
    public String getName() {
        return "PlayerPlacesHanging";
    }

    @Override
    public ScriptEntryData getScriptEntryData() {
        return new BukkitScriptEntryData(event.getPlayer());
    }

    @Override
    public ObjectTag getContext(String name) {
        if (name.equals("hanging")) {
            return hanging;
        }
        else if (name.equals("location")) {
            return location;
        }
        return super.getContext(name);
    }

    @EventHandler
    public void pnPlayerPlacesHanging(HangingPlaceEvent event) {
        if (EntityTag.isNPC(event.getPlayer())) {
            return;
        }
        Entity hangingEntity = event.getEntity();
        EntityTag.rememberEntity(hangingEntity);
        hanging = new EntityTag(hangingEntity);
        location = new LocationTag(event.getBlock().getLocation());
        this.event = event;
        fire(event);
        EntityTag.forgetEntity(hangingEntity);
    }
}

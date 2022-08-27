package com.denizenscript.denizen.events.player;

import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizen.objects.LocationTag;
import com.denizenscript.denizen.utilities.implementation.BukkitScriptEntryData;
import com.denizenscript.denizen.events.BukkitScriptEvent;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.notable.NoteManager;
import com.denizenscript.denizencore.scripts.ScriptEntryData;
import com.denizenscript.denizencore.utilities.CoreUtilities;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

public class PlayerWalksOverScriptEvent extends BukkitScriptEvent implements Listener {

    // <--[event]
    // @Events
    // player walks over notable
    // player walks over <'location'>
    //
    // @Group Player
    //
    // @Location true
    //
    // @Cancellable true
    //
    // @Triggers when a player walks over a noted location. In most cases, it is preferable to use <@link event player enters area> with a small cuboid.
    //
    // @Context
    // <context.notable> returns an ElementTag of the notable location's name.
    //
    // @Player Always.
    //
    // -->

    public PlayerWalksOverScriptEvent() {
        registerCouldMatcher("player walks over notable");
        registerCouldMatcher("player walks over <'location'>");
    }

    public String notable;
    public PlayerMoveEvent event;

    @Override
    public boolean matches(ScriptPath path) {
        String loc = path.eventArgLowerAt(3);
        MatchHelper matcher = createMatcher(loc);
        if (!loc.equals("notable") && !matcher.doesMatch(notable)) {
            return false;
        }
        return super.matches(path);
    }

    @Override
    public ScriptEntryData getScriptEntryData() {
        return new BukkitScriptEntryData(event.getPlayer());
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
        if (LocationTag.isSameBlock(event.getFrom(), event.getTo())) {
            return;
        }
        if (EntityTag.isNPC(event.getPlayer())) {
            return;
        }
        notable = NoteManager.getSavedId(new LocationTag(event.getTo()).getBlockLocation());
        if (notable == null) {
            return;
        }
        notable = CoreUtilities.toLowerCase(notable);
        this.event = event;
        fire(event);
    }
}

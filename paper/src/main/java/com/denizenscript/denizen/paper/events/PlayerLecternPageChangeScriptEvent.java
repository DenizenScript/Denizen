package com.denizenscript.denizen.paper.events;

import com.denizenscript.denizen.events.BukkitScriptEvent;
import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizen.objects.ItemTag;
import com.denizenscript.denizen.objects.LocationTag;
import com.denizenscript.denizen.utilities.implementation.BukkitScriptEntryData;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.scripts.ScriptEntryData;
import io.papermc.paper.event.player.PlayerLecternPageChangeEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class PlayerLecternPageChangeScriptEvent extends BukkitScriptEvent implements Listener {

    // <--[event]
    // @Events
    // player flips lectern page
    //
    // @Plugin Paper
    //
    // @Group Paper
    //
    // @Location true
    //
    // @Cancellable true
    //
    // @Triggers when the player flips to a page in a lectern.
    //
    // @Context
    // <context.book> returns an ItemTag of the book in the lectern.
    // <context.lectern> returns a LocationTag of the lectern.
    // <context.old_page> returns the last page the player was on. Page numbers follow zero-based numbering, starting at 0.
    // <context.new_page> returns the new page that the player flipped to.
    // <context.page_direction> returns the direction in which the player flips the lectern book page.
    //
    // @Player when the attacked entity is a player.
    //
    // -->

    public PlayerLecternPageChangeScriptEvent() {
        registerCouldMatcher("player flips lectern page");
    }

    public PlayerLecternPageChangeEvent event;

    @Override
    public boolean matches(ScriptPath path) {
        return super.matches(path);
    }

    @Override
    public ScriptEntryData getScriptEntryData() {
        return new BukkitScriptEntryData(event.getPlayer());
    }

    @Override
    public ObjectTag getContext(String name) {
        return switch (name) {
            case "book" -> new ItemTag(event.getBook());
            case "lectern" -> new LocationTag(event.getLectern().getLocation());
            case "old_page" -> new ElementTag(event.getOldPage());
            case "new_page" -> new ElementTag(event.getNewPage());
            case "page_direction" -> new ElementTag(event.getPageChangeDirection());
            default -> super.getContext(name);
        };
    }

    @EventHandler
    public void onPlayerFlipsLecternPage(PlayerLecternPageChangeEvent event) {
        this.event = event;
        fire(event);
    }
}

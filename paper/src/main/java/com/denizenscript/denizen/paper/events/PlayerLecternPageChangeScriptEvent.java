package com.denizenscript.denizen.paper.events;

import com.denizenscript.denizen.events.BukkitScriptEvent;
import com.denizenscript.denizen.objects.ItemTag;
import com.denizenscript.denizen.objects.LocationTag;
import com.denizenscript.denizen.utilities.implementation.BukkitScriptEntryData;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.scripts.ScriptEntryData;
import com.denizenscript.denizencore.utilities.CoreUtilities;
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
    // @Player Always.
    //
    // @Triggers when the player flips to a page in a lectern.
    //
    // @Switch book:<item> to only process the event if the book on the lectern matches the given item.
    //
    // @Context
    // <context.book> returns an ItemTag of the book in the lectern.
    // <context.lectern> returns a LocationTag of the lectern.
    // <context.old_page> returns an ElementTag(Number) of the last page the player was on.
    // <context.new_page> returns an ElementTag(Number) of the new page that the player flipped to.
    // <context.flip_direction> returns the direction in which the player flips the lectern book page, can be either LEFT or RIGHT.
    //
    // @Determine
    // "PAGE:<ElementTag(Number)>" to set the page that the player will flip to.
    //
    // @Example
    // # Announce the page the player flipped to.
    // on player flips lectern page:
    // - announce "<player.name> flipped to page #<context.new_page>!"
    //
    // @Example
    // # Flips the player to page 5 if they are flagged with "pancakes".
    // on player flips lectern page flagged:pancakes:
    // - determine page:5
    //
    // -->

    public PlayerLecternPageChangeScriptEvent() {
        registerCouldMatcher("player flips lectern page");
        registerSwitches("book");
    }

    public PlayerLecternPageChangeEvent event;
    
    @Override
    public boolean matches(ScriptPath path) {
        if (!runInCheck(path, event.getLectern().getLocation())) {
            return false;
        }
        if (!path.tryObjectSwitch("book", new ItemTag(event.getBook()))) {
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
        return switch (name) {
            case "book" -> new ItemTag(event.getBook());
            case "lectern" -> new LocationTag(event.getLectern().getLocation());
            case "old_page" -> new ElementTag(event.getOldPage() + 1);
            case "new_page" -> new ElementTag(event.getNewPage() + 1);
            case "flip_direction" -> new ElementTag(event.getPageChangeDirection());
            default -> super.getContext(name);
        };
    }

    @Override
    public boolean applyDetermination(ScriptPath path, ObjectTag determinationObj) {
        if (determinationObj instanceof ElementTag) {
            String lower = CoreUtilities.toLowerCase(determinationObj.toString());
            if (lower.startsWith("page:")) {
                ElementTag value = new ElementTag(lower.substring("page:".length()));
                if (value.isInt()) {
                    event.setNewPage(value.asInt() - 1);
                    return true;
                }
            }
        }
        return super.applyDetermination(path, determinationObj);
    }
    
    @EventHandler
    public void onPlayerFlipsLecternPage(PlayerLecternPageChangeEvent event) {
        this.event = event;
        fire(event);
    }
}

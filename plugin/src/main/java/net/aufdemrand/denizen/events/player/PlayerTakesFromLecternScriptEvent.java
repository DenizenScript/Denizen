package net.aufdemrand.denizen.events.player;

import net.aufdemrand.denizen.BukkitScriptEntryData;
import net.aufdemrand.denizen.events.BukkitScriptEvent;
import net.aufdemrand.denizen.objects.dEntity;
import net.aufdemrand.denizen.objects.dItem;
import net.aufdemrand.denizen.objects.dLocation;
import net.aufdemrand.denizen.objects.dPlayer;
import net.aufdemrand.denizencore.events.ScriptEvent;
import net.aufdemrand.denizencore.objects.dObject;
import net.aufdemrand.denizencore.scripts.ScriptEntryData;
import net.aufdemrand.denizencore.scripts.containers.ScriptContainer;
import net.aufdemrand.denizencore.utilities.CoreUtilities;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerTakeLecternBookEvent;

public class PlayerTakesFromLecternScriptEvent extends BukkitScriptEvent implements Listener {

    // <--[event]
    // @Events
    // player takes item from lectern
    // player takes <item> from lectern
    //
    // @Regex ^on player takes [^\s]+ from lectern$
    // @Switch in <area>
    //
    // @Cancellable true
    //
    // @Triggers when a player takes a book from a lectern.
    //
    // @Context
    // <context.location> returns the dLocation of the lectern.
    // <context.item> returns the book dItem taken out of the lectern.
    //
    // -->

    public PlayerTakesFromLecternScriptEvent() {
        instance = this;
    }

    public static PlayerTakesFromLecternScriptEvent instance;
    public dLocation location;
    public dItem item;
    public PlayerTakeLecternBookEvent event;

    @Override
    public boolean couldMatch(ScriptContainer scriptContainer, String s) {
        String lower = CoreUtilities.toLowerCase(s);
        return lower.startsWith("player takes")
                && (CoreUtilities.getXthArg(4, lower).equals("lectern"));
    }

    @Override
    public boolean matches(ScriptEvent.ScriptPath path) {
        String itemTest = path.eventArgLowerAt(2);
        return tryItem(item, itemTest) && runInCheck(path, location);
    }

    @Override
    public String getName() {
        return "PlayerTakesFromLectern";
    }

    @Override
    public ScriptEntryData getScriptEntryData() {
        return new BukkitScriptEntryData(dPlayer.mirrorBukkitPlayer(event.getPlayer()), null);
    }

    @Override
    public dObject getContext(String name) {
        if (name.equals("location")) {
            return location;
        }
        else if (name.equals("item")) {
            return item;
        }
        return super.getContext(name);
    }

    @EventHandler
    public void onPlayerTakesFromLectern(PlayerTakeLecternBookEvent event) {
        if (dEntity.isNPC(event.getPlayer())) {
            return;
        }
        item = new dItem(event.getBook());
        location = new dLocation(event.getLectern().getLocation());
        this.event = event;
        fire(event);
    }
}

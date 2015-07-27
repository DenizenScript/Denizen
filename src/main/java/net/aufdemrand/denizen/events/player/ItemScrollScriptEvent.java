package net.aufdemrand.denizen.events.player;

import net.aufdemrand.denizen.BukkitScriptEntryData;
import net.aufdemrand.denizen.objects.dPlayer;
import net.aufdemrand.denizen.utilities.DenizenAPI;
import net.aufdemrand.denizencore.events.ScriptEvent;
import net.aufdemrand.denizencore.objects.Element;
import net.aufdemrand.denizencore.objects.dObject;
import net.aufdemrand.denizencore.scripts.ScriptEntryData;
import net.aufdemrand.denizencore.scripts.containers.ScriptContainer;
import net.aufdemrand.denizencore.utilities.CoreUtilities;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemHeldEvent;

import java.util.HashMap;

public class ItemScrollScriptEvent extends ScriptEvent implements Listener {

    // TODO: in area
    // TODO: item:x switch
    // <--[event]
    // @Events
    // player scrolls their hotbar
    // player holds item
    //
    // @Regex ^on player (scrolls their hotbar|holds item)$
    //
    // @Cancellable true
    //
    // @Triggers when a player scrolls through their hotbar.
    //
    // @Context
    // <context.new_slot> returns the number of the new inventory slot.
    // <context.previous_slot> returns the number of the old inventory slot.
    //
    // -->

    public ItemScrollScriptEvent() {
        instance = this;
    }

    public static ItemScrollScriptEvent instance;

    public Element new_slot;
    public Element previous_slot;
    public PlayerItemHeldEvent event;

    @Override
    public boolean couldMatch(ScriptContainer scriptContainer, String s) {
        String lower = CoreUtilities.toLowerCase(s);
        return lower.equals("player holds item")
                || lower.equals("player scrolls their hotbar");
    }

    @Override
    public boolean matches(ScriptContainer scriptContainer, String s) {
        return true;
    }

    @Override
    public String getName() {
        return "PlayerScrollsItem";
    }

    @Override
    public void init() {
        Bukkit.getServer().getPluginManager().registerEvents(this, DenizenAPI.getCurrentInstance());
    }

    @Override
    public void destroy() {
        PlayerItemHeldEvent.getHandlerList().unregister(this);
    }

    @Override
    public boolean applyDetermination(ScriptContainer container, String determination) {
        return super.applyDetermination(container, determination);
    }

    @Override
    public ScriptEntryData getScriptEntryData() {
        return new BukkitScriptEntryData(new dPlayer(event.getPlayer()), null);
    }

    @Override
    public dObject getContext(String name) {
        if (name.equals("new_slot")) {
            return new_slot;
        }
        else if (name.equals("previous_slot")) {
            return previous_slot;
        }
        return super.getContext(name);
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerScrollsHotbar(PlayerItemHeldEvent event) {
        new_slot = new Element(event.getNewSlot() + 1);
        previous_slot = new Element(event.getPreviousSlot() + 1);
        cancelled = event.isCancelled();
        this.event = event;
        fire();
        event.setCancelled(cancelled);
    }
}

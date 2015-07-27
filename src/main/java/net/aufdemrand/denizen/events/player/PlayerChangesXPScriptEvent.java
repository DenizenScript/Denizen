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
import org.bukkit.event.player.PlayerExpChangeEvent;

import java.util.HashMap;

public class PlayerChangesXPScriptEvent extends BukkitScriptEvent implements Listener {

    // TODO: in area
    // <--[event]
    // @Events
    // player changes xp
    //
    // @Regex ^on player changes xp$
    //
    // @Cancellable true
    //
    // @Triggers when a player's experience amount changes.
    //
    // @Context
    // <context.amount> returns the amount of changed experience.
    //
    // @Determine
    // Element(Number) to set the amount of changed experience.
    //
    // -->

    public PlayerChangesXPScriptEvent() {
        instance = this;
    }

    public static PlayerChangesXPScriptEvent instance;
    public int amount;
    public PlayerExpChangeEvent event;

    @Override
    public boolean couldMatch(ScriptContainer scriptContainer, String s) {
        return CoreUtilities.toLowerCase(s).startsWith("player changes xp");
    }

    @Override
    public boolean matches(ScriptContainer scriptContainer, String s) {
        return true;
    }

    @Override
    public String getName() {
        return "PlayerChangesXP";
    }

    @Override
    public void init() {
        Bukkit.getServer().getPluginManager().registerEvents(this, DenizenAPI.getCurrentInstance());
    }

    @Override
    public void destroy() {
        PlayerExpChangeEvent.getHandlerList().unregister(this);
    }

    @Override
    public boolean applyDetermination(ScriptContainer container, String determination) {
        String lower = CoreUtilities.toLowerCase(determination);
        if (aH.matchesInteger(determination)) {
            amount = aH.getIntegerFrom(determination);
            return true;
        }
        if (lower.equals("cancelled")) {
            amount = 0;
            return true;
        }
        return super.applyDetermination(container, determination);
    }

    @Override
    public ScriptEntryData getScriptEntryData() {
        return new BukkitScriptEntryData(dEntity.isPlayer(event.getPlayer()) ? dEntity.getPlayerFrom(event.getPlayer()) : null, null);
    }

    @Override
    public HashMap<String, dObject> getContext() {
        HashMap<String, dObject> context = super.getContext();
        context.put("amount", new Element(amount));
        return context;
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerChangesXP(PlayerExpChangeEvent event) {
        if (dEntity.isNPC(event.getPlayer())) {
            return;
        }
        amount = event.getAmount();
        this.event = event;
        fire();
        event.setAmount(amount);
    }
}

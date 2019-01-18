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
import org.bukkit.event.player.PlayerExpChangeEvent;

public class PlayerChangesXPScriptEvent extends BukkitScriptEvent implements Listener {

    // TODO: in area
    // <--[event]
    // @Events
    // player changes xp (in <area>)
    //
    // @Regex ^on player changes xp( in ((notable (cuboid|ellipsoid))|([^\s]+)))?$
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
    public PlayerExpChangeEvent event;
    public int amount;
    public dPlayer player;

    @Override
    public boolean couldMatch(ScriptContainer scriptContainer, String s) {
        return CoreUtilities.toLowerCase(s).startsWith("player changes xp");
    }

    @Override
    public boolean matches(ScriptPath path) {
        String s = path.event;
        String lower = path.eventLower;
        if (!runInCheck(scriptContainer, s, lower, player.getLocation())) {
            return false;
        }

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
        return super.applyDetermination(container, determination);
    }

    @Override
    public ScriptEntryData getScriptEntryData() {
        return new BukkitScriptEntryData(player, null);
    }

    @Override
    public dObject getContext(String name) {
        if (name.equals("amount")) {
            return new Element(amount);
        }
        return super.getContext(name);
    }

    @EventHandler
    public void onPlayerChangesXP(PlayerExpChangeEvent event) {
        if (dEntity.isNPC(event.getPlayer())) {
            return;
        }
        amount = event.getAmount();
        player = dPlayer.mirrorBukkitPlayer(event.getPlayer());
        this.event = event;
        cancelled = false;
        fire();
        if (cancelled) {
            amount = 0;
        }
        event.setAmount(amount);
    }
}

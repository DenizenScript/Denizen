package net.aufdemrand.denizen.events.player;

import net.aufdemrand.denizen.BukkitScriptEntryData;
import net.aufdemrand.denizen.events.BukkitScriptEvent;
import net.aufdemrand.denizen.objects.dEntity;
import net.aufdemrand.denizen.objects.dLocation;
import net.aufdemrand.denizen.objects.dPlayer;
import net.aufdemrand.denizen.objects.notable.NotableManager;
import net.aufdemrand.denizen.utilities.DenizenAPI;
import net.aufdemrand.denizencore.objects.Element;
import net.aufdemrand.denizencore.objects.dObject;
import net.aufdemrand.denizencore.scripts.ScriptEntryData;
import net.aufdemrand.denizencore.scripts.containers.ScriptContainer;
import net.aufdemrand.denizencore.utilities.CoreUtilities;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

public class PlayerWalksOverScriptEvent extends BukkitScriptEvent implements Listener {

    // <--[event]
    // @Events
    // player walks over notable
    // player walks over <location>
    //
    // @Regex ^on player walks over [^\s]+( in ((notable (cuboid|ellipsoid))|([^\s]+)))?$
    //
    // @Cancellable true
    //
    // @Triggers when a player walks over a notable location.
    //
    // @Context
    // <context.notable> returns an Element of the notable location's name.
    //
    // -->

    public PlayerWalksOverScriptEvent() {
        instance = this;
    }

    public static PlayerWalksOverScriptEvent instance;
    public String notable;
    public PlayerMoveEvent event;

    @Override
    public boolean couldMatch(ScriptContainer scriptContainer, String s) {
        return CoreUtilities.toLowerCase(s).startsWith("player walks over");
    }

    @Override
    public boolean matches(ScriptContainer scriptContainer, ScriptPath path) {
        String s = path.event;
        String lower = path.eventLower;
        String loc = CoreUtilities.getXthArg(3, lower);
        return loc.equals(CoreUtilities.toLowerCase(notable)) || tryLocation(new dLocation(event.getPlayer().getLocation()), loc);
    }

    @Override
    public String getName() {
        return "PlayerWalksOver";
    }

    @Override
    public void init() {
        Bukkit.getServer().getPluginManager().registerEvents(this, DenizenAPI.getCurrentInstance());
    }

    @Override
    public void destroy() {
        PlayerMoveEvent.getHandlerList().unregister(this);
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
        if (name.equals("notable")) {
            return new Element(notable);
        }
        return super.getContext(name);
    }

    @EventHandler
    public void onPlayerWalksOver(PlayerMoveEvent event) {
        if (event.getFrom().getBlock().equals(event.getTo().getBlock())) {
            return;
        }
        if (dEntity.isNPC(event.getPlayer())) {
            return;
        }
        notable = NotableManager.getSavedId(new dLocation(event.getTo().getBlock().getLocation()));
        if (notable == null) {
            return;
        }
        cancelled = event.isCancelled();
        this.event = event;
        fire();
        event.setCancelled(cancelled);
    }
}

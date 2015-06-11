package net.aufdemrand.denizen.events.scriptevents;


import net.aufdemrand.denizen.objects.dLocation;
import net.aufdemrand.denizen.utilities.DenizenAPI;
import net.aufdemrand.denizencore.events.ScriptEvent;
import net.aufdemrand.denizencore.objects.Element;
import net.aufdemrand.denizencore.objects.dObject;
import net.aufdemrand.denizencore.scripts.containers.ScriptContainer;
import net.aufdemrand.denizencore.utilities.CoreUtilities;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

import java.util.HashMap;

public class BiomeEnterExitScriptEvent extends ScriptEvent implements Listener {

    // <--[event]
    // @Events
    // player enters <biome>
    // player exits <biome>
    // player enters biome
    // player exits biome
    //
    // @Warning Cancelling this event will fire a similar event immediately after.
    //
    // @Cancellable true
    //
    // @Triggers when a player enters or exits a biome.
    //
    // @Context
    // <context.from> returns the block location moved from.
    // <context.to> returns the block location moved to.
    // <context.old_biome> returns an element of the biome being left.
    // <context.new_biome> returns an element of the biome being entered.
    //
    // -->

    public BiomeEnterExitScriptEvent() {
        instance = this;
    }
    public static BiomeEnterExitScriptEvent instance;
    public dLocation from;
    public dLocation to;
    public Element old_biome;
    public Element new_biome;
    public PlayerMoveEvent event;

    @Override
    public boolean couldMatch(ScriptContainer scriptContainer, String s) {
        String lower = CoreUtilities.toLowerCase(s);
        return lower.startsWith("player enters")
                || lower.startsWith("player exits");
    }

    @Override
    public boolean matches(ScriptContainer scriptContainer, String s) {
        String lower = CoreUtilities.toLowerCase(s);
        String biome_test = lower.substring(lower.lastIndexOf(" ")+1);
        String direction = lower.substring(lower.indexOf(" ")+1, lower.lastIndexOf(" ")-1);

        return !(old_biome.toString().toLowerCase().equals(new_biome.toString().toLowerCase()))
                && (biome_test.equals("biome")
                    || (direction.equals("enters") && biome_test.equals(new_biome.toString().toLowerCase()))
                    || (direction.equals("exits") && biome_test.equals(old_biome.toString().toLowerCase())));
    }

    @Override
    public String getName() {
        return "BiomeEnterExit";
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
    public HashMap<String, dObject> getContext() {
        HashMap<String, dObject> context = super.getContext();
        context.put("to", to);
        context.put("from", from);
        context.put("old_biome", old_biome);
        context.put("new_biome", new_biome);
        return context;
    }

    @EventHandler
    public void onPlayerEntersExitsBiome(PlayerMoveEvent event) {
        from = new dLocation(event.getFrom());
        to = new dLocation(event.getTo());
        old_biome = new Element(from.getBlock().getBiome().name());
        new_biome = new Element(to.getBlock().getBiome().name());
        cancelled = event.isCancelled();
        this.event = event;
        fire();
        event.setCancelled(cancelled);
    }
}

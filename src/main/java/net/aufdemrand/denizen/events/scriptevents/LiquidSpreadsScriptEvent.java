package net.aufdemrand.denizen.events.scriptevents;

import net.aufdemrand.denizen.objects.dLocation;
import net.aufdemrand.denizen.objects.dMaterial;
import net.aufdemrand.denizen.utilities.DenizenAPI;
import net.aufdemrand.denizencore.events.ScriptEvent;
import net.aufdemrand.denizencore.objects.dObject;
import net.aufdemrand.denizencore.scripts.containers.ScriptContainer;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockFromToEvent;

import java.util.HashMap;

public class LiquidSpreadsScriptEvent extends ScriptEvent implements Listener  {

    // <--[event]
    // @Events
    // liquid spreads
    // <liquid block> spreads
    //
    // @Triggers when a liquid block spreads or dragon egg moves.
    // @Context
    // <context.destination> returns the dLocation the block spread to.
    // <context.location> returns the dLocation the block spread from.
    // <context.material> returns the dMaterial of the block that spread.
    //
    // @Determine
    // "CANCELLED" to stop the block from spreading.
    //
    // -->


    public LiquidSpreadsScriptEvent() {
        instance = this;
    }
    public static LiquidSpreadsScriptEvent instance;
    public dMaterial material;
    public dLocation from;
    public dLocation to;
    public BlockFromToEvent event;

    @Override
    public boolean couldMatch(ScriptContainer scriptContainer, String s) {
        String lower = s.toLowerCase();
        return (lower.matches("[^\\s]+ spreads"));
    }

    @Override
    public boolean matches(ScriptContainer scriptContainer, String s) {
        String lower = s.toLowerCase();
        String mname = event.getBlock().getType().getData().getName().toLowerCase();
        String mname2 = material.identifySimple().substring(2);
        return lower.startsWith("liquid spreads")
                || lower.startsWith(mname + " spreads")
                || lower.startsWith(mname2 + " spreads");
    }

    @Override
    public String getName() {
        return "LiquidSpreads";
    }

    @Override
    public void init() {
        Bukkit.getServer().getPluginManager().registerEvents(this, DenizenAPI.getCurrentInstance());
    }

    @Override
    public void destroy() {
        BlockFromToEvent.getHandlerList().unregister(this);
    }

    @Override
    public boolean applyDetermination(ScriptContainer container, String determination) {
        return super.applyDetermination(container, determination);
    }

    @Override
    public HashMap<String, dObject> getContext() {
        HashMap<String, dObject> context = super.getContext();
        context.put("location", from);
        context.put("destination", to);
        context.put("material", material);
        return context;
    }

    @EventHandler
    public void blockFromTo(BlockFromToEvent event) {
        to = new dLocation(event.getToBlock().getLocation());
        from = new dLocation(event.getBlock().getLocation());
        material = dMaterial.getMaterialFrom(event.getBlock().getType(), event.getBlock().getData());
        cancelled = event.isCancelled();
        this.event = event;
        fire();
        event.setCancelled(cancelled);
    }
}

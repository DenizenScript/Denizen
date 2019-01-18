package net.aufdemrand.denizen.events.world;

import net.aufdemrand.denizen.objects.dChunk;
import net.aufdemrand.denizen.objects.dWorld;
import net.aufdemrand.denizen.utilities.DenizenAPI;
import net.aufdemrand.denizencore.events.ScriptEvent;
import net.aufdemrand.denizencore.objects.dObject;
import net.aufdemrand.denizencore.scripts.containers.ScriptContainer;
import net.aufdemrand.denizencore.utilities.CoreUtilities;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkUnloadEvent;

public class ChunkUnloadScriptEvent extends ScriptEvent implements Listener {

    // TODO: replace in <world> with in <area>
    // <--[event]
    // @Events
    // chunk unloads (in <world>)
    //
    // @Regex ^on chunk unloads( in [^\s]+)?$
    //
    // @Warning This event will fire *extremely* rapidly and often!
    //
    // @Cancellable true
    //
    // @Triggers when a chunk is unloaded
    //
    // @Context
    // <context.chunk> returns the loading chunk.
    //
    // -->

    public ChunkUnloadScriptEvent() {
        instance = this;
    }

    public static ChunkUnloadScriptEvent instance;

    public dChunk chunk;
    public dWorld world;
    public ChunkUnloadEvent event;

    @Override
    public boolean couldMatch(ScriptContainer scriptContainer, String s) {
        String lower = CoreUtilities.toLowerCase(s);
        return lower.startsWith("chunk unloads");
    }

    @Override
    public boolean matches(ScriptContainer scriptContainer, ScriptPath path) {
        String s = path.event;
        String lower = path.eventLower;
        return (lower.startsWith("chunk unloads") && !lower.contains(" in "))
                || lower.startsWith("chunk unloads in " + CoreUtilities.toLowerCase(world.getName()));
    }

    @Override
    public String getName() {
        return "ChunkUnloads";
    }

    @Override
    public void init() {
        Bukkit.getServer().getPluginManager().registerEvents(this, DenizenAPI.getCurrentInstance());
    }

    @Override
    public void destroy() {
        ChunkUnloadEvent.getHandlerList().unregister(this);
    }

    @Override
    public boolean applyDetermination(ScriptContainer container, String determination) {
        return super.applyDetermination(container, determination);
    }

    @Override
    public dObject getContext(String name) {
        if (name.equals("chunk")) {
            return chunk;
        }
        else if (name.equals("world")) { // NOTE: Deprecated in favor of context.chunk.world
            return world;
        }
        return super.getContext(name);
    }

    @EventHandler
    public void onChunkUnload(ChunkUnloadEvent event) {
        chunk = new dChunk(event.getChunk());
        world = new dWorld(event.getWorld());
        cancelled = event.isCancelled();
        this.event = event;
        fire();
        event.setCancelled(cancelled);
    }
}

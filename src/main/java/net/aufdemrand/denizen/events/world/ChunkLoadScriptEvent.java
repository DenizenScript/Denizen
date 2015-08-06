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
import org.bukkit.event.world.ChunkLoadEvent;

public class ChunkLoadScriptEvent extends ScriptEvent implements Listener {

    // TODO: Replace in <world> with in <area>
    // <--[event]
    // @Events
    // chunk loads for the first time (in <world>)
    //
    // @Regex ^on chunk loads for the first time( in [^\s]+)?$
    //
    // @Warning This event will fire *extremely* rapidly and often!
    //
    // @Triggers when a new chunk is loaded
    //
    // @Context
    // <context.chunk> returns the loading chunk.
    //
    // -->

    public ChunkLoadScriptEvent() {
        instance = this;
    }

    public static ChunkLoadScriptEvent instance;

    public dChunk chunk;
    public dWorld world;
    public ChunkLoadEvent event;

    @Override
    public boolean couldMatch(ScriptContainer scriptContainer, String s) {
        String lower = CoreUtilities.toLowerCase(s);
        return lower.startsWith("chunk loads");
    }

    @Override
    public boolean matches(ScriptContainer scriptContainer, String s) {
        String lower = CoreUtilities.toLowerCase(s);
        return lower.equals("chunk loads for the first time")
                || lower.equals("chunk loads for the first time in " +
                CoreUtilities.toLowerCase(world.getName()));
    }

    @Override
    public String getName() {
        return "ChunkLoads";
    }

    @Override
    public void init() {
        Bukkit.getServer().getPluginManager().registerEvents(this, DenizenAPI.getCurrentInstance());
    }

    @Override
    public void destroy() {
        ChunkLoadEvent.getHandlerList().unregister(this);
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
        else if (name.equals("world")) { // Deprecated in favor of context.chunk.world
            return world;
        }
        return super.getContext(name);
    }

    @EventHandler(ignoreCancelled = true)
    public void onChunkLoad(ChunkLoadEvent event) {
        chunk = new dChunk(event.getChunk());
        world = new dWorld(event.getWorld());
        this.event = event;
        fire();
    }
}

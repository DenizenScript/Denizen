package com.denizenscript.denizen.events.world;

import com.denizenscript.denizen.objects.dChunk;
import com.denizenscript.denizen.objects.dWorld;
import com.denizenscript.denizen.events.BukkitScriptEvent;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.scripts.containers.ScriptContainer;
import com.denizenscript.denizencore.utilities.CoreUtilities;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkUnloadEvent;

public class ChunkUnloadScriptEvent extends BukkitScriptEvent implements Listener {

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
    public boolean matches(ScriptPath path) {
        return (path.eventLower.startsWith("chunk unloads") && !path.eventLower.contains(" in "))
                || path.eventLower.startsWith("chunk unloads in " + CoreUtilities.toLowerCase(world.getName()));
    }

    @Override
    public String getName() {
        return "ChunkUnloads";
    }

    @Override
    public boolean applyDetermination(ScriptContainer container, String determination) {
        return super.applyDetermination(container, determination);
    }

    @Override
    public ObjectTag getContext(String name) {
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
        this.event = event;
        fire(event);
    }
}

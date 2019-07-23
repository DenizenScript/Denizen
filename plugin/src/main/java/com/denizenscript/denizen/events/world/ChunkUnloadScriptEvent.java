package com.denizenscript.denizen.events.world;

import com.denizenscript.denizen.objects.ChunkTag;
import com.denizenscript.denizen.objects.WorldTag;
import com.denizenscript.denizen.events.BukkitScriptEvent;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.scripts.containers.ScriptContainer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkUnloadEvent;

public class ChunkUnloadScriptEvent extends BukkitScriptEvent implements Listener {

    // <--[event]
    // @Events
    // chunk unloads
    //
    // @Regex ^on chunk unloads$
    //
    // @Group World
    //
    // @Switch in <area>
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

    public ChunkTag chunk;
    public ChunkUnloadEvent event;

    @Override
    public boolean couldMatch(ScriptPath path) {
        return path.eventLower.startsWith("chunk unloads");
    }

    @Override
    public boolean matches(ScriptPath path) {
        if (!runInCheck(path, chunk.getCenter())) {
            return false;
        }
        return true;
    }

    @Override
    public String getName() {
        return "ChunkUnloads";
    }

    @Override
    public ObjectTag getContext(String name) {
        if (name.equals("chunk")) {
            return chunk;
        }
        else if (name.equals("world")) { // NOTE: Deprecated in favor of context.chunk.world
            return new WorldTag(event.getWorld());
        }
        return super.getContext(name);
    }

    @EventHandler
    public void onChunkUnload(ChunkUnloadEvent event) {
        chunk = new ChunkTag(event.getChunk());
        this.event = event;
        fire(event);
    }
}

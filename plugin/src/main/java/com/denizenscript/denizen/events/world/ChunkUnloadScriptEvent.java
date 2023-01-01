package com.denizenscript.denizen.events.world;

import com.denizenscript.denizen.objects.ChunkTag;
import com.denizenscript.denizen.events.BukkitScriptEvent;
import com.denizenscript.denizencore.objects.ObjectTag;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkUnloadEvent;

public class ChunkUnloadScriptEvent extends BukkitScriptEvent implements Listener {

    // <--[event]
    // @Events
    // chunk unloads
    //
    // @Group World
    //
    // @Location true
    //
    // @Warning This event will fire *extremely* rapidly and often!
    //
    // @Cancellable true
    //
    // @Triggers when a chunk is unloaded
    //
    // @Context
    // <context.chunk> returns the unloading chunk.
    //
    // -->

    public ChunkUnloadScriptEvent() {
        registerCouldMatcher("chunk unloads");
    }


    public ChunkTag chunk;
    public ChunkUnloadEvent event;

    @Override
    public boolean matches(ScriptPath path) {
        if (!runInCheck(path, chunk.getCenter())) {
            return false;
        }
        return super.matches(path);
    }

    @Override
    public ObjectTag getContext(String name) {
        if (name.equals("chunk")) {
            return chunk;
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

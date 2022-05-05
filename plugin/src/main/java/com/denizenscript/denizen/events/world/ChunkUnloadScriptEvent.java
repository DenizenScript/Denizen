package com.denizenscript.denizen.events.world;

import com.denizenscript.denizen.objects.ChunkTag;
import com.denizenscript.denizen.objects.WorldTag;
import com.denizenscript.denizen.events.BukkitScriptEvent;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizen.utilities.BukkitImplDeprecations;
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
        instance = this;
        registerCouldMatcher("chunk unloads");
    }

    public static ChunkUnloadScriptEvent instance;

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
    public String getName() {
        return "ChunkUnloads";
    }

    @Override
    public ObjectTag getContext(String name) {
        if (name.equals("chunk")) {
            return chunk;
        }
        else if (name.equals("world")) {
            BukkitImplDeprecations.worldContext.warn();
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

package com.denizenscript.denizen.events.world;

import com.denizenscript.denizen.objects.ChunkTag;
import com.denizenscript.denizen.objects.WorldTag;
import com.denizenscript.denizen.events.BukkitScriptEvent;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.utilities.Deprecations;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkLoadEvent;

public class ChunkLoadScriptEvent extends BukkitScriptEvent implements Listener {

    // <--[event]
    // @Events
    // chunk loads for the first time
    //
    // @Regex ^on chunk loads for the first time$
    //
    // @Group World
    //
    // @Switch in <area>
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

    public ChunkTag chunk;
    public ChunkLoadEvent event;

    @Override
    public boolean couldMatch(ScriptPath path) {
        return path.eventLower.startsWith("chunk loads for the first time");
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
        return "ChunkLoads";
    }

    @Override
    public ObjectTag getContext(String name) {
        if (name.equals("chunk")) {
            return chunk;
        }
        else if (name.equals("world")) {
            Deprecations.worldContext.warn();
            return new WorldTag(event.getWorld());
        }
        return super.getContext(name);
    }

    @EventHandler
    public void onChunkLoad(ChunkLoadEvent event) {
        if (!event.isNewChunk()) {
            return;
        }
        chunk = new ChunkTag(event.getChunk());
        this.event = event;
        fire(event);
    }
}

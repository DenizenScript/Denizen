package net.aufdemrand.denizen.events.core;

import net.aufdemrand.denizen.events.EventManager;
import net.aufdemrand.denizen.events.SmartEvent;
import net.aufdemrand.denizen.objects.*;
import net.aufdemrand.denizen.utilities.DenizenAPI;
import net.aufdemrand.denizen.utilities.debugging.dB;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkLoadEvent;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ChunkLoadSmartEvent implements SmartEvent, Listener {


    ///////////////////
    // SMARTEVENT METHODS
    ///////////////


    @Override
    public boolean shouldInitialize(Set<String> events) {

        // Loop through event names from loaded world script events
        for (String event : events) {

            // Use a regex pattern to narrow down matches
            Matcher m = Pattern.compile("on chunk loads for the first time", Pattern.CASE_INSENSITIVE)
                    .matcher(event);

            if (m.matches()) {
                // Event names are simple enough to just go ahead and pass on any match.
                return true;
            }
        }
        // No matches at all, just fail.
        return false;
    }


    @Override
    public void _initialize() {
        DenizenAPI.getCurrentInstance().getServer().getPluginManager()
                .registerEvents(this, DenizenAPI.getCurrentInstance());
        dB.log("Loaded Chunk Load SmartEvent.");
    }


    @Override
    public void breakDown() {
        ChunkLoadEvent.getHandlerList().unregister(this);
    }

    //////////////
    //  MECHANICS
    ///////////

    // <--[event]
    // @Events
    // chunk loads for the first time (in <world>)
    //
    // @Warning This event will fire *extremely* rapidly and often!
    //
    // @Triggers when a new chunk is loaded
    // @Context
    // <context.chunk> returns the loading chunk.
    // <context.world> returns the world the chunk is loading in.
    //
    // -->
    @EventHandler
    public void onChunkLoad(ChunkLoadEvent event) {
        if (!event.isNewChunk()) return;
        Map<String, dObject> context = new HashMap<String, dObject>();
        dWorld world = new dWorld(event.getWorld());
        context.put("chunk", new dChunk(event.getChunk()));
        context.put("world", world);
        String determination = EventManager.doEvents(Arrays.asList("chunk loads for the first time",
                "chunk loads for the first time in " + world.identify()), null, null, context, true);
        // TODO: Find way to cancel this?... may not be reasonably possible.
    }
}

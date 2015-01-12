package net.aufdemrand.denizen.events.core;

import net.aufdemrand.denizen.events.EventManager;
import net.aufdemrand.denizen.events.SmartEvent;
import net.aufdemrand.denizen.objects.dChunk;
import net.aufdemrand.denizen.objects.dObject;
import net.aufdemrand.denizen.objects.dWorld;
import net.aufdemrand.denizen.utilities.DenizenAPI;
import net.aufdemrand.denizen.utilities.debugging.dB;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkUnloadEvent;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ChunkUnloadSmartEvent implements SmartEvent, Listener {


    ///////////////////
    // SMARTEVENT METHODS
    ///////////////


    @Override
    public boolean shouldInitialize(Set<String> events) {

        // Loop through event names from loaded world script events
        for (String event : events) {

            // Use a regex pattern to narrow down matches
            Matcher m = Pattern.compile("on chunk unloads( in (w@)?\\w+)?", Pattern.CASE_INSENSITIVE)
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
        dB.log("Loaded Chunk Unload SmartEvent.");
    }


    @Override
    public void breakDown() {
        ChunkUnloadEvent.getHandlerList().unregister(this);
    }

    //////////////
    //  MECHANICS
    ///////////

    // <--[event]
    // @Events
    // chunk unloads (in <world>)
    //
    // @Warning This event will fire *extremely* rapidly and often!
    //
    // @Triggers when a chunk is unloaded
    // @Context
    // <context.chunk> returns the loading chunk.
    //
    // @Determine
    // "CANCELLED" to prevent the chunk from being unloaded.
    //
    // -->
    @EventHandler
    public void onChunkLoad(ChunkUnloadEvent event) {
        Map<String, dObject> context = new HashMap<String, dObject>();
        dWorld world = new dWorld(event.getWorld());
        context.put("chunk", new dChunk(event.getChunk()));
        String determination = EventManager.doEvents(Arrays.asList("chunk unloads",
                "chunk unloads " + world.identify()), null, null, context, true);
        if (determination.equalsIgnoreCase("cancelled"))
            event.setCancelled(true);
    }
}

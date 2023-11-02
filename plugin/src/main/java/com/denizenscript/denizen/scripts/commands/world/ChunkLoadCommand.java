package com.denizenscript.denizen.scripts.commands.world;

import com.denizenscript.denizen.utilities.blocks.ChunkCoordinate;
import com.denizenscript.denizencore.utilities.debugging.Debug;
import com.denizenscript.denizen.utilities.depends.Depends;
import com.denizenscript.denizen.Denizen;
import com.denizenscript.denizen.objects.ChunkTag;
import com.denizenscript.denizen.objects.LocationTag;
import com.denizenscript.denizencore.exceptions.InvalidArgumentsException;
import com.denizenscript.denizencore.objects.Argument;
import com.denizenscript.denizencore.objects.core.DurationTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.core.ListTag;
import com.denizenscript.denizencore.scripts.ScriptEntry;
import com.denizenscript.denizencore.scripts.commands.AbstractCommand;
import com.denizenscript.denizencore.utilities.CoreUtilities;
import net.citizensnpcs.api.event.DespawnReason;
import net.citizensnpcs.api.event.NPCDespawnEvent;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class ChunkLoadCommand extends AbstractCommand implements Listener {

    public ChunkLoadCommand() {
        setName("chunkload");
        setSyntax("chunkload ({add}/remove/removeall) [<chunk>|...] (duration:<value>)");
        setRequiredArguments(1, 3);
        Bukkit.getPluginManager().registerEvents(this, Denizen.getInstance());
        if (Depends.citizens != null) {
            Bukkit.getPluginManager().registerEvents(new ChunkLoadCommandNPCEvents(), Denizen.getInstance());
        }
        isProcedural = false;
    }

    // <--[command]
    // @Name ChunkLoad
    // @Syntax chunkload ({add}/remove/removeall) [<chunk>|...] (duration:<value>)
    // @Required 1
    // @Maximum 3
    // @Short Keeps a chunk actively loaded and allowing activity.
    // @Group world
    //
    // @Description
    // Forces a chunk to load and stay loaded in the world for the duration specified or until removed.
    // This will not persist over server restarts.
    // If no duration is specified it defaults to 0 (forever).
    // While a chunk is loaded all normal activity such as crop growth and npc activity continues,
    // other than activity that requires a nearby player.
    //
    // @Tags
    // <WorldTag.loaded_chunks>
    // <ChunkTag.is_loaded>
    // <ChunkTag.force_loaded>
    //
    // @Usage
    // Use to load a chunk.
    // - chunkload <[some_chunk]>
    //
    // @Usage
    // Use to temporarily load a chunk.
    // - chunkload <player.location.add[5000,0,0].chunk> duration:5m
    //
    // @Usage
    // Use to stop loading a chunk.
    // - chunkload remove <[some_chunk]>
    //
    // @Usage
    // Use to stop loading all chunks.
    // - chunkload removeall
    // -->

    /*
     * Keeps a chunk loaded
     */

    private enum Action {ADD, REMOVE, REMOVEALL}

    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {
        for (Argument arg : scriptEntry) {
            if (arg.matchesEnum(Action.class)
                    && !scriptEntry.hasObject("action")) {
                scriptEntry.addObject("action", new ElementTag(arg.getValue().toUpperCase()));
                if (arg.getValue().equalsIgnoreCase("removeall")) {
                    scriptEntry.addObject("location", new ListTag(Collections.singletonList(new LocationTag(Bukkit.getWorlds().get(0), 0, 0, 0))));
                }
            }
            else if (arg.matchesArgumentList(ChunkTag.class)
                    && !scriptEntry.hasObject("location")) {
                scriptEntry.addObject("location", arg.asType(ListTag.class));
            }
            else if (arg.matchesArgumentList(LocationTag.class)
                    && !scriptEntry.hasObject("location")) {
                scriptEntry.addObject("location", arg.asType(ListTag.class));
            }
            else if (arg.matchesArgumentType(DurationTag.class)
                    && !scriptEntry.hasObject("duration")) {
                scriptEntry.addObject("duration", arg.asType(DurationTag.class));
            }
            else {
                arg.reportUnhandled();
            }
        }
        if (!scriptEntry.hasObject("location")) {
            throw new InvalidArgumentsException("Missing location argument!");
        }
        if (!scriptEntry.hasObject("action")) {
            scriptEntry.addObject("action", new ElementTag("ADD"));
        }
        if (!scriptEntry.hasObject("duration")) {
            scriptEntry.addObject("duration", new DurationTag(0));
        }
    }

    @Override
    public void execute(ScriptEntry scriptEntry) {
        ElementTag action = scriptEntry.getElement("action");
        ListTag chunklocs = scriptEntry.getObjectTag("location");
        DurationTag length = scriptEntry.getObjectTag("duration");
        if (scriptEntry.dbCallShouldDebug()) {
            Debug.report(scriptEntry, getName(), action, chunklocs, length);
        }
        for (String chunkText : chunklocs) {
            Chunk chunk;
            if (ChunkTag.matches(chunkText)) {
                chunk = ChunkTag.valueOf(chunkText, scriptEntry.context).getChunk();
            }
            else if (LocationTag.matches(chunkText)) {
                chunk = LocationTag.valueOf(chunkText, scriptEntry.context).getChunk();
            }
            else {
                Debug.echoError("Chunk input '" + chunkText + "' is invalid.");
                return;
            }
            ChunkCoordinate coord = new ChunkCoordinate(chunk);
            switch (Action.valueOf(action.asString())) {
                case ADD:
                    if (length.getSeconds() != 0) {
                        chunkDelays.put(coord, CoreUtilities.monotonicMillis() + length.getMillis());
                    }
                    else {
                        chunkDelays.put(coord, (long) 0);
                    }
                    Debug.echoDebug(scriptEntry, "...added chunk " + chunk.getX() + ", " + chunk.getZ() + " with a delay of " + length.getSeconds() + " seconds.");
                    if (!chunk.isLoaded()) {
                        chunk.load();
                    }
                    chunk.addPluginChunkTicket(Denizen.getInstance());
                    if (length.getSeconds() > 0) {
                        Bukkit.getScheduler().scheduleSyncDelayedTask(Denizen.getInstance(), () -> {
                            if (chunkDelays.containsKey(coord) && chunkDelays.get(coord) <= CoreUtilities.monotonicMillis()) {
                                chunk.removePluginChunkTicket(Denizen.getInstance());
                                chunkDelays.remove(coord);
                            }
                        }, length.getTicks() + 20);
                    }
                    break;
                case REMOVE:
                    if (chunkDelays.containsKey(coord)) {
                        chunkDelays.remove(coord);
                        chunk.removePluginChunkTicket(Denizen.getInstance());
                        Debug.echoDebug(scriptEntry, "...allowing unloading of chunk " + chunk.getX() + ", " + chunk.getZ());
                    }
                    else {
                        Debug.echoDebug(scriptEntry, "Chunk '" + coord + "' was not on the load list, ignoring.");
                    }
                    break;
                case REMOVEALL:
                    Debug.echoDebug(scriptEntry, "...allowing unloading of all stored chunks");
                    for (ChunkCoordinate loopCoord : chunkDelays.keySet()) {
                        loopCoord.getChunk().getChunk().removePluginChunkTicket(Denizen.getInstance());
                    }
                    chunkDelays.clear();
                    break;
            }
        }
    }

    // Map of chunks with delays
    Map<ChunkCoordinate, Long> chunkDelays = new HashMap<>();

    public class ChunkLoadCommandNPCEvents implements Listener {
        @EventHandler
        public void stopDespawn(NPCDespawnEvent e) {
            if (e.getNPC() == null || !e.getNPC().isSpawned() || e.getReason() != DespawnReason.CHUNK_UNLOAD) {
                return;
            }
            Chunk chnk = e.getNPC().getEntity().getLocation().getChunk();
            ChunkCoordinate coord = new ChunkCoordinate(chnk);
            if (chunkDelays.containsKey(coord)) {
                if (chunkDelays.get(coord) == 0) {
                    e.setCancelled(true);
                }
                else if (CoreUtilities.monotonicMillis() < chunkDelays.get(coord)) {
                    e.setCancelled(true);
                }
                else {
                    chunkDelays.remove(coord);
                }
            }
        }
    }
}

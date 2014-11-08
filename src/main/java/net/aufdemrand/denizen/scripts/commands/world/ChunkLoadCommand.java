package net.aufdemrand.denizen.scripts.commands.world;

import java.util.HashMap;
import java.util.Map;

import net.aufdemrand.denizen.objects.*;
import net.aufdemrand.denizen.utilities.DenizenAPI;
import net.aufdemrand.denizencore.exceptions.CommandExecutionException;
import net.aufdemrand.denizencore.exceptions.InvalidArgumentsException;
import net.aufdemrand.denizen.scripts.ScriptEntry;
import net.aufdemrand.denizen.scripts.commands.AbstractCommand;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.citizensnpcs.api.event.NPCDespawnEvent;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkUnloadEvent;

public class ChunkLoadCommand extends AbstractCommand implements Listener {

    @Override
    public void onEnable() {
        DenizenAPI.getCurrentInstance().getServer().getPluginManager().registerEvents(this, DenizenAPI.getCurrentInstance());
    }

    /*
     * Keeps a chunk loaded
     */

    private enum Action { ADD, REMOVE, REMOVEALL }

    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {

        for (aH.Argument arg : aH.interpret(scriptEntry.getArguments())) {
            if (arg.matchesEnum(Action.values())
                    && !scriptEntry.hasObject("action")) {
                scriptEntry.addObject("action", new Element(arg.getValue().toUpperCase()));
                if (arg.getValue().equalsIgnoreCase("removeall"))
                    scriptEntry.addObject("location", new dLocation(Bukkit.getWorlds().get(0), 0, 0, 0));
            }

            else if (arg.matchesArgumentType(dChunk.class)
                    && !scriptEntry.hasObject("location"))
                scriptEntry.addObject("location", arg.asType(dChunk.class).getCenter());

            else if (arg.matchesArgumentType(dLocation.class)
                    && !scriptEntry.hasObject("location"))
                scriptEntry.addObject("location", arg.asType(dLocation.class));

            else if (arg.matchesArgumentType(Duration.class)
                && !scriptEntry.hasObject("duration"))
                scriptEntry.addObject("duration", arg.asType(Duration.class));

            else
                arg.reportUnhandled();
        }

        if (!scriptEntry.hasObject("location"))
            throw new InvalidArgumentsException("Missing location argument!");

        if (!scriptEntry.hasObject("action"))
            scriptEntry.addObject("action", new Element("ADD"));

        if (!scriptEntry.hasObject("duration"))
            scriptEntry.addObject("duration", new Duration(0));
    }

    @Override
    public void execute(ScriptEntry scriptEntry) throws CommandExecutionException {
        // Get objects
        Element action = scriptEntry.getElement("action");
        dLocation chunkloc = (dLocation) scriptEntry.getObject("location");
        Duration length = (Duration) scriptEntry.getObject("duration");

        dB.report(scriptEntry, getName(),
                action.debug()
                + chunkloc.debug()
                + length.debug());

        Chunk chunk = chunkloc.getChunk();
        String chunkString = chunk.getX()+", "+chunk.getZ();

        switch (Action.valueOf(action.asString())) {
        case ADD:
            if(length.getSeconds() != 0)
                chunkDelays.put(chunkString, System.currentTimeMillis() + length.getMillis());
            else
                chunkDelays.put(chunkString, (long) 0);
            dB.echoDebug(scriptEntry, "...added chunk "+chunk.getX() + ", "+ chunk.getZ() + " with a delay of " + length.getSeconds() + " seconds.");
            if(!chunk.isLoaded())
                chunk.load();
            break;
        case REMOVE:
            if(chunkDelays.containsKey(chunkString)) {
                chunkDelays.remove(chunkString);
                dB.echoDebug(scriptEntry, "...allowing unloading of chunk "+chunk.getX() + ", "+ chunk.getZ());
            }
            else
                dB.echoError("Chunk was not on the load list!");
            break;
        case REMOVEALL:
            dB.echoDebug(scriptEntry, "...allowing unloading of all stored chunks");
            chunkDelays.clear();
            break;
        }

    }

    // Map of chunks with delays
    Map<String, Long> chunkDelays = new HashMap<String, Long>();

    @EventHandler
    public void stopUnload(ChunkUnloadEvent e) {
        String chunkString = e.getChunk().getX()+", "+ e.getChunk().getZ();
        if(chunkDelays.containsKey(chunkString)) {
            if(chunkDelays.get(chunkString) == 0)
                e.setCancelled(true);

            else if(System.currentTimeMillis() < chunkDelays.get(chunkString))
                e.setCancelled(true);

            else
                chunkDelays.remove(chunkString);
        }
    }

    @EventHandler
    public void stopDespawn(NPCDespawnEvent e) {
        if (e.getNPC() == null || !e.getNPC().isSpawned())
            return;
        Chunk chnk = e.getNPC().getEntity().getLocation().getChunk();
        String chunkString = chnk.getX()+", "+ chnk.getZ();
        if(chunkDelays.containsKey(chunkString)) {
            if(chunkDelays.get(chunkString) == 0)
                e.setCancelled(true);

            else if(System.currentTimeMillis() < chunkDelays.get(chunkString))
                e.setCancelled(true);

            else
                chunkDelays.remove(chunkString);
        }
    }
}

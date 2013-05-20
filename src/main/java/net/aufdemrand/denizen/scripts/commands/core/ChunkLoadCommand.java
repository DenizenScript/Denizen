package net.aufdemrand.denizen.scripts.commands.core;

import java.util.HashMap;
import java.util.Map;

import net.aufdemrand.denizen.exceptions.CommandExecutionException;
import net.aufdemrand.denizen.exceptions.InvalidArgumentsException;
import net.aufdemrand.denizen.scripts.ScriptEntry;
import net.aufdemrand.denizen.scripts.commands.AbstractCommand;
import net.aufdemrand.denizen.arguments.Duration;
import net.aufdemrand.denizen.arguments.aH;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.aufdemrand.denizen.utilities.debugging.dB.Messages;
import net.citizensnpcs.api.event.NPCDespawnEvent;

import org.bukkit.Chunk;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkUnloadEvent;

public class ChunkLoadCommand extends AbstractCommand implements Listener {

    @Override
    public void onEnable() {
        denizen.getServer().getPluginManager().registerEvents(this, denizen);
    }

    /* 
     * Keeps a chunk loaded
     */
    
    private enum Action { ADD, REMOVE, REMOVEALL }

    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {
        Chunk chunk = null;
        Duration length = new Duration(0);
        Action action = Action.ADD;
        
        for (String arg : scriptEntry.getArguments()) {
            if(aH.matchesArg("ADD, REMOVE, REMOVEALL", arg)) {
                action = Action.valueOf(aH.getStringFrom(arg).toUpperCase());
            } else if (aH.matchesLocation(arg)) {
                chunk = aH.getLocationFrom(arg).getChunk();

            } else if(aH.matchesInteger(arg) || aH.matchesDouble(arg) || aH.matchesDuration(arg)) {
                length = Duration.valueOf(arg);
            } else throw new InvalidArgumentsException(Messages.ERROR_UNKNOWN_ARGUMENT, arg);
        }
        
        if (chunk == null) throw new InvalidArgumentsException(Messages.DEBUG_SET_LOCATION);

        scriptEntry.addObject("action", action)
            .addObject("chunk", chunk)
            .addObject("length", length);
    }

    @Override
    public void execute(ScriptEntry scriptEntry) throws CommandExecutionException {
        // Get objects
        Action action = (Action) scriptEntry.getObject("action");
        Chunk chunk = (Chunk) scriptEntry.getObject("chunk");
        Duration length = (Duration) scriptEntry.getObject("length");
        
        
        switch (action) {
        case ADD:
            if(length.getSeconds() != 0)
                chunkDelays.put(chunk.getX()+", "+chunk.getZ(), System.currentTimeMillis() + length.getMillis());
            else
                chunkDelays.put(chunk.getX()+", "+chunk.getZ(), (long) 0);
            dB.echoDebug("...added chunk "+chunk.getX() + ", "+ chunk.getZ() + " with a " + length.getSeconds() + "delay");
            if(!chunk.isLoaded())
                chunk.load();
            break;
        case REMOVE:
            if(chunkDelays.containsKey(chunk)) {
                chunkDelays.remove(chunk);
                dB.echoDebug("...allowing unloading of chunk "+chunk.getX() + ", "+ chunk.getZ());
            }
            break;
        case REMOVEALL:
            dB.echoDebug("...allowing unloading of all stored chunks");
            chunkDelays.clear();
            break;
        }
        
        // Report to dB
        dB.report(getName(), aH.debugObj("Action", action.toString())
                        + aH.debugObj("Chunk", chunk.toString())
                        + aH.debugObj("Length", length.toString()));

    }
    
    // Map of chunks with delays
    Map<String, Long> chunkDelays = new HashMap<String, Long>();
    
    @EventHandler
    public void stopUnload(ChunkUnloadEvent e) {
        if(chunkDelays.containsKey(e.getChunk().getX()+ ", "+e.getChunk().getZ())) {
            if(chunkDelays.get(e.getChunk().getX()+ ", "+e.getChunk().getZ()) == 0) {
                e.setCancelled(true);
                return;
            } else if(System.currentTimeMillis() < chunkDelays.get(e.getChunk().getX()+ ", "+e.getChunk().getZ())) {
                e.setCancelled(true);
                return;
            } else chunkDelays.remove(e.getChunk().getX()+ ", "+e.getChunk().getZ());
        }
        return;
    }
    
    @EventHandler
    public void stopDespawn(NPCDespawnEvent e) {
        Chunk chnk = e.getNPC().getBukkitEntity().getLocation().getChunk();
        if(chunkDelays.containsKey(chnk.getX() + ", "+chnk.getZ())) {
            if(chunkDelays.get(chnk.getX() + ", "+chnk.getZ()) == 0) {
                e.setCancelled(true);
                return;
            } else if(System.currentTimeMillis() < chunkDelays.get(chnk.getX() + ", "+chnk.getZ())) {
                e.setCancelled(true);
                return;
            } else chunkDelays.remove(chnk.getX() + ", "+chnk.getZ());
        }
        return;
    }

}

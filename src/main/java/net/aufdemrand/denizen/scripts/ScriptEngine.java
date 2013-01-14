package net.aufdemrand.denizen.scripts;

import net.aufdemrand.denizen.Denizen;
import net.aufdemrand.denizen.npc.DenizenNPC;
import net.aufdemrand.denizen.scripts.commands.CommandExecuter;
import net.aufdemrand.denizen.scripts.requirements.RequirementChecker;
import net.aufdemrand.denizen.utilities.debugging.dB;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Contains methods used to manipulate and execute ScriptEntries in Queues.
 * 
 * @author Jeremy Schroeder
 * 
 */

@SuppressWarnings("incomplete-switch")
public class ScriptEngine {

    final private Denizen denizen;
    
    final private ScriptHelper scriptHelper;
    final private ScriptBuilder scriptBuilder;
    final private RequirementChecker requirementChecker;
    final private CommandExecuter commandExecuter;

    public ScriptEngine(Denizen denizenPlugin) {
        denizen  = denizenPlugin;

        scriptHelper  = new ScriptHelper(denizen);
        scriptBuilder = new ScriptBuilder(denizen);
        commandExecuter = new CommandExecuter(denizen);
        requirementChecker = new RequirementChecker(denizen);
    }	

    public enum QueueType {
        PLAYER, PLAYER_TASK, NPC
    }

    private Map<Player, List<ScriptEntry>>      playerQueue = new ConcurrentHashMap<Player, List<ScriptEntry>>();
    private Map<Player, List<ScriptEntry>>  playerTaskQueue = new ConcurrentHashMap<Player, List<ScriptEntry>>();
    private Map<DenizenNPC, List<ScriptEntry>>     npcQueue = new ConcurrentHashMap<DenizenNPC, List<ScriptEntry>>();


    /**
     * Gets the currently loaded instance of the ScriptHelper
     * 
     * @return ScriptHelper
     * 
     */
    public ScriptHelper getScriptHelper() {
        return scriptHelper;
    }

    
    /**
     * Gets the currently loaded instance of the RequirementChecker
     * 
     * @return ScriptHelper
     * 
     */
    public RequirementChecker getRequirementChecker() {
        return requirementChecker;
    }
    

    /**
     * Gets the currently loaded instance of the ScriptBuilder
     * 
     * @return ScriptBuilder
     * 
     */
    public ScriptBuilder getScriptBuilder() {
        return scriptBuilder;
    }

    
    /**
     * Gets the currently loaded instance of the ScriptExecuter
     * 
     * @return Executer
     * 
     */
    public CommandExecuter getScriptExecuter() {
        return commandExecuter;
    }
    

    /**
     * Processes commands from the Queues. This is called automatically by a repeating Bukkit task.
     *  
     */
    public void run() {
        // First the playerQue, primary script queue for Players
        if (!playerQueue.isEmpty()) 	
            // Attempt to run a command for each player. The attempted command (and attached info) is in scriptEntry.
            for (Entry<Player, List<ScriptEntry>> theEntry : playerQueue.entrySet()) 
                if (!theEntry.getValue().isEmpty()) 
                    // Check the time of the command to see if it has been delayed with a WAIT command. Only 
                    // proceed for the player if the time on the command is less than the current time. 
                    // If it's more, then this entry will be skipped and saved for next time.
                    if (theEntry.getValue().get(0).getAllowedRunTime() < System.currentTimeMillis()) {
                        // Feeds the executer ScriptCommands as long as they are instant commands ("^"), otherwise
                        // runs one command, removes it from the queue, and moves on to the next player.
                        boolean instantly;
                        do { 
                            instantly = false;
                            ScriptEntry scriptEntry = theEntry.getValue().get(0);
                            scriptEntry.setSendingQueue(QueueType.PLAYER);
                            // Instant command check
                            if (theEntry.getValue().size() > 1 && theEntry.getValue().get(0).isInstant()) instantly = true; 
                            // Remove from queue, the command is still cached in scriptEntry until it is finished
                            theEntry.getValue().remove(0);
                            // Updates the triggerQue map
                            playerQueue.put(theEntry.getKey(), theEntry.getValue());
                            // Catching errors here will keep the queue from locking up.
                            try { getScriptExecuter().execute(scriptEntry); }
                            catch (Throwable e) {
                                dB.echoError("Woah! An exception has been called with this command!");
                                if (!dB.showStackTraces)
                                    dB.echoError("Enable '/denizen stacktrace' for the nitty-gritty.");
                                else e.printStackTrace(); }
                        } while (instantly);
                    }

        if (!playerTaskQueue.isEmpty()) 	
            for (Entry<Player, List<ScriptEntry>> theEntry : playerTaskQueue.entrySet()) 
                if (!theEntry.getValue().isEmpty()) 
                    if (theEntry.getValue().get(0).getAllowedRunTime() < System.currentTimeMillis()) {
                        boolean instantly;
                        do { 
                            instantly = false;
                            ScriptEntry scriptEntry = theEntry.getValue().get(0);
                            scriptEntry.setSendingQueue(QueueType.PLAYER_TASK);
                            if (theEntry.getValue().size() > 1 && theEntry.getValue().get(0).isInstant()) instantly = true; 
                            theEntry.getValue().remove(0);
                            playerTaskQueue.put(theEntry.getKey(), theEntry.getValue());
                            try { getScriptExecuter().execute(scriptEntry); }
                            catch (Throwable e) {
                                dB.echoError("Woah! An exception has been called with this command!");
                                if (!dB.showStackTraces)
                                    dB.echoError("Enable '/denizen stacktrace' for the nitty-gritty.");
                                else e.printStackTrace(); }
                        } while (instantly);
                    }

        if (!npcQueue.isEmpty()) 	
            for (Entry<DenizenNPC, List<ScriptEntry>> theEntry : npcQueue.entrySet()) 
                if (!theEntry.getValue().isEmpty()) 
                    if (theEntry.getValue().get(0).getAllowedRunTime() < System.currentTimeMillis()) {
                        boolean instantly;
                        do { 
                            instantly = false;
                            ScriptEntry scriptEntry = theEntry.getValue().get(0);
                            scriptEntry.setSendingQueue(QueueType.NPC);
                            if (theEntry.getValue().size() > 1 && theEntry.getValue().get(0).isInstant()) instantly = true; 
                            theEntry.getValue().remove(0);
                            npcQueue.put(theEntry.getKey(), theEntry.getValue());
                            try { getScriptExecuter().execute(scriptEntry); }
                            catch (Throwable e) {
                                dB.echoError("Woah! An exception has been called with this command!");
                                if (!dB.showStackTraces)
                                    dB.echoError("Enable '/denizen stacktrace' for the nitty-gritty.");
                                else e.printStackTrace(); }
                        } while (instantly);
                    }
    }


    /**
     * Adds scriptEntries to a Player Queue. (QueueType.PLAYER or QueueType.PLAYER_TASK)
     * 
     * @param player
     *      The player's queue to add the entries to.
     * @param scriptEntries
     *      List of scriptEntries to add.
     * @param queueType
     *      The queue type to add the entries to.
     * 
     */
	public void addToQue(Player player, List<ScriptEntry> scriptEntries, QueueType queueType) {
        List<ScriptEntry> scriptCommandList;
        switch (queueType) {
        case PLAYER:
            scriptCommandList = playerQueue.get(player);
            if (scriptCommandList == null)
                scriptCommandList = new ArrayList<ScriptEntry>();
            playerQueue.remove(player);
            scriptCommandList.addAll(scriptEntries);
            playerQueue.put(player, scriptCommandList);
            break;
    
        case PLAYER_TASK:
            scriptCommandList = playerTaskQueue.get(player);
            if (scriptCommandList == null)
                scriptCommandList = new ArrayList<ScriptEntry>();
            playerTaskQueue.remove(player); 
            scriptCommandList.addAll(scriptEntries);
            playerTaskQueue.put(player, scriptCommandList);
            break;
        }
    
        return;
    }


    /**
     * Adds scriptEntries to a Player Queue. (QueueType.PLAYER or QueueType.PLAYER_TASK)
     * 
     * @param npc
     *      The NPC's queue to add the entries to.
     * @param scriptEntries
     *      List of scriptEntries to add.
     * @param queueType
     *      The queue type to add the entries to.
     * 
     */    
    public void addToQue(DenizenNPC npc, List<ScriptEntry> scriptEntries, QueueType queueType) {
        List<ScriptEntry> scriptCommandList;
        switch (queueType) {
        case NPC:
            scriptCommandList = npcQueue.get(npc);
            if (scriptCommandList == null)
                scriptCommandList = new ArrayList<ScriptEntry>();
            npcQueue.remove(npc); 
            scriptCommandList.addAll(scriptEntries);
            npcQueue.put(npc, scriptCommandList);
            break;
        }
    
        return;
    }


    /**
     * Retrieves a current Player QueueType. (QueueType.PLAYER or QueueType.PLAYER_TASK)
     *
     * @param queueType 
     *      The queue type to retrieve.
     *      
     * @return the specified queue.
     * 
     */
    public Map<Player, List<ScriptEntry>> getQueue(QueueType queueType) {
        switch (queueType) {
        case PLAYER:
            return playerQueue;
    
        case PLAYER_TASK:
            return playerTaskQueue;
        }
        return null;
    }


    /**
     * Retrieves a current NPC QueueType. (QueueType.NPC)
     *
     * @param queueType 
     *      The queue type to retrieve.
     *      
     * @return the specified queue.
     * 
     */
    public Map<DenizenNPC, List<ScriptEntry>> getDQueue(QueueType queueType) {
        switch (queueType) {
        case NPC:
            return npcQueue;
        }
    
        return null;
    }


    /**
     * Retrieves a specific Player's Queue of ScriptEntries.
     * 
     * @param player
     *      The Player's queue that should be retrieved.
     * @param queueType
     *      The queue type to retrieve.
     * 
     * @return the Player's Queue of ScriptEntries.
     * 
     */
    public List<ScriptEntry> getPlayerQueue(Player player, QueueType queueType) {
        return getQueue(queueType).get(player);
    }


    /**
     * Retrieves a specific NPC's Queue of ScriptEntries.
     * 
     * @param npc
     *      The NPC's queue that should be retrieved.
     * @param queueType
     *      The queue type to retrieve.
     * 
     * @return the NPCs's Queue of ScriptEntries.
     * 
     */
    public List<ScriptEntry> getDenizenQueue(DenizenNPC npc, QueueType queueType) {
        return getDQueue(queueType).get(npc);
    }


    /**
     * Injects a list of scriptEntries into a Queue. The scriptEntry with an index of 0 is the next entry
     * to execute. If the position specified is larger than the current amount of ScriptEntries currently
     * in the Queue, they are added to the end.
     * 
     * @param player
     *      The player's queue to inject the entries to.
     * @param scriptEntries
     *      The list of ScriptEntries to inject.
     * @param queueType
     *      The type of queue to add the entries to.
     * @param position
     *      The position in the queue to inject the entries.
     * 
     * @return false if the specified queueType doesn't exist.
     * 
     */
    public boolean injectToQueue(Player player, List<ScriptEntry> scriptEntries, QueueType queueType, int position) {
        List<ScriptEntry> scriptCommandList = new ArrayList<ScriptEntry>();
        switch (queueType) {
        case PLAYER:
            scriptCommandList = playerQueue.get(player);
            playerQueue.remove(player); 
            if (position > scriptCommandList.size() || position < 0) position = 1;
            if (scriptCommandList.size() == 0) position = 0;
            scriptCommandList.addAll(position, scriptEntries);
            playerQueue.put(player, scriptCommandList);
            return true;

        case PLAYER_TASK:
            scriptCommandList = playerTaskQueue.get(player);
            playerTaskQueue.remove(player); 
            if (position > scriptCommandList.size() || position < 0) position = 1;
            if (scriptCommandList.size() == 0) position = 0;
            scriptCommandList.addAll(position, scriptEntries);
            playerTaskQueue.put(player, scriptCommandList);
            return true;
        }

        return false;
    }


    /**
     * Injects a scriptEntry into a Queue. The ScriptEntry with an index of 0 is the next entry
     * to execute. If the position specified is larger than the current amount of scriptEntries currently
     * in the Queue, it is added to the end.
     * 
     * @param player
     *      The player's queue to inject the entries to.
     * @param scriptEntry
     *      The ScriptEntry to inject.
     * @param queueType
     *      The type of queue to add the entries to.
     * @param position
     *      The position in the queue to inject the entries.
     * 
     * @return false if the specified queueType doesn't exist.
     * 
     */
    public boolean injectToQueue(Player player, ScriptEntry scriptEntry, QueueType queueType, int position) {
        List<ScriptEntry> scriptEntries = new ArrayList<ScriptEntry>();
        scriptEntries.add(scriptEntry);

        return injectToQueue(player, scriptEntries, queueType, position);
    }


    /**
     * Injects a list of scriptEntries into a Queue. The scriptEntry with an index of 0 is the next entry
     * to execute. If the position specified is larger than the current amount of scriptEntries currently
     * in the Queue, they are added to the end.
     * 
     * @param npc
     *      The NPC's queue to inject the entries to.
     * @param scriptEntries
     *      The list of ScriptEntries to inject.
     * @param queueType
     *      The type of queue to add the entries to.
     * @param position
     *      The position in the queue to inject the entries.
     * 
     * @return false if the specified queueType doesn't exist.
     * 
     */
    public boolean injectToQueue(DenizenNPC npc, List<ScriptEntry> scriptEntries, QueueType queueType, int position) {
        List<ScriptEntry> scriptCommandList = new ArrayList<ScriptEntry>();

        switch (queueType) {
        case NPC:
            scriptCommandList = npcQueue.get(npc);
            playerQueue.remove(npc); 
            if (position > scriptCommandList.size() || position < 0) position = 1;
            if (scriptCommandList.size() == 0) position = 0;
            scriptCommandList.addAll(position, scriptEntries);
            npcQueue.put(npc, scriptCommandList);
            return true;
        }

        return false;
    }


    /**
     * Injects a scriptEntry into a Queue. The ScriptEntry with an index of 0 is the next entry
     * to execute. If the position specified is larger than the current amount of scriptEntries currently
     * in the Queue, it is added to the end.
     * 
     * @param npc
     *      The NPC's queue to inject the entries to.
     * @param scriptEntry
     *      The list of ScriptEntries to inject.
     * @param queueType
     *      The queue type to add the entries to.
     * @param position
     *      The position in the queue to inject the entries.
     * 
     * @return false if the specified queueType doesn't exist.
     * 
     */
    public boolean injectToQue(DenizenNPC npc, ScriptEntry scriptEntry, QueueType queueType, int position) {
        List<ScriptEntry> scriptEntries = new ArrayList<ScriptEntry>();
        scriptEntries.add(scriptEntry);

        return injectToQueue(npc, scriptEntries, queueType, position);
    }


    /**
     * Replace a Player's Queue. Use with care! This could be confusing to the player if not properly used!
     * 
     * @param player
     *      The Player's queue to replace
     * @param scriptEntries
     *      The list of scriptEntries to replace with
     * @param queueType
     *      The queue type to replace
     *      
     */
    public void replaceQueue(Player player, List<ScriptEntry> scriptEntries, QueueType queueType) {
        switch (queueType) {
        case PLAYER:
            playerQueue.remove(player); 
            playerQueue.put(player, scriptEntries);
            break;

        case PLAYER_TASK:
            playerTaskQueue.remove(player); 
            playerTaskQueue.put(player, scriptEntries);
            break;
        
        default:
        	break;
        }

        return;
    }

    
    /**
     * Replace a NPC's Queue. Use with care! This could be confusing to the player if not properly used!
     * 
     * @param npc
     *      The NPC's queue to replace
     * @param scriptEntries
     *      The list of scriptEntries to replace with
     * @param queueType
     *      The queue type to replace
     *      
     */
    public void replaceQueue(DenizenNPC npc, List<ScriptEntry> scriptEntries, QueueType queueType) {
        switch (queueType) {
        case NPC:
            npcQueue.remove(npc); 
            npcQueue.put(npc, scriptEntries);
            break;
        }

        return;
    }

}
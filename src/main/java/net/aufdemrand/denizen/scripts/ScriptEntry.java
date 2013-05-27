package net.aufdemrand.denizen.scripts;

import net.aufdemrand.denizen.exceptions.ScriptEntryCreationException;
import net.aufdemrand.denizen.objects.dNPC;
import net.aufdemrand.denizen.objects.dScript;
import net.aufdemrand.denizen.scripts.containers.ScriptContainer;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.*;


/**
 * ScriptEntry contain information about a single entry from a dScript.
 *  
 * @author Jeremy Schroeder
 *
 */
public class ScriptEntry {

    // The name of the command that will be executed
	private String command;

    // The queuetime and allowed-run-time can dictate whether it's okay
    // for this command to run in the queue.
    private long creationTime;
    private long queueTime;
    private long runTime;
    private long holdTime;

	private boolean instant = false;
    private boolean waitfor = false;
    private boolean done = false;

    private Player player = null;
    private OfflinePlayer offlinePlayer = null;
	private dNPC npc = null;

    private dScript script = null;

	private ScriptQueue queue = null;
	private List<String> args = null;

	private Map<String, Object> objects = new HashMap<String, Object>();

    public ScriptEntry(String command, String[] arguments, ScriptContainer script) throws ScriptEntryCreationException {

        // Must never be null
        if (command == null)
            throw new ScriptEntryCreationException("CommandType cannot be null!");

        this.command = command.toUpperCase();
        this.script = script.getAsScriptArg();

        // Internal, never null. runTime/holdTime can be adjusted mid-execution
		this.creationTime = System.currentTimeMillis();
        this.queueTime = creationTime;
		this.runTime = creationTime;
        this.holdTime = creationTime;

        // Check if this is an 'instant' or 'waitfor' command.
		if (command.startsWith("^")) {
			instant = true;
			this.command = command.substring(1);
		} else if (command.startsWith("~")) {
            waitfor = true;
            this.command = command.substring(1);
        }

		this.args = new ArrayList<String>();
		if (arguments != null)
            this.args = Arrays.asList(arguments);

        // Check for replaceable tags.
        for (String arg : args) {
            if (arg.indexOf("<") >= 0
                    && arg.indexOf(">") >= 0) {
                has_tags = true;
                break;
            }
        }

	}

    public boolean has_tags = false;

	public ScriptEntry addObject(String key, Object object) {
        if (object == null) return this;
		objects.put(key.toUpperCase(), object);
		return this;
	}

	public long getRunTime() {
		return runTime;
	}

    public long getHoldTime() {
        return holdTime;
    }

	public List<String> getArguments() {
		return args;
	}

	public String getCommandName() {
		return command;
	}

	public dNPC getNPC() {
		return npc;
	}

    public OfflinePlayer getOfflinePlayer() {
        return offlinePlayer;
    }

    public void setFinished(boolean finished) {
        done = finished;
    }

    public ScriptEntry setOfflinePlayer(OfflinePlayer offlinePlayer) {
        this.offlinePlayer = offlinePlayer;
        return this;
    }

    public Player getPlayer() {
        return player;
    }

    public Map<String, Object> getObjects() {
		return objects;
	}
	
	public Object getObject(String key) {
		try {
			return objects.get(key.toUpperCase());
		} catch (Exception e) { return null; }
	}

    public boolean hasObject(String key) {
        return objects.containsKey(key.toUpperCase());
    }

	public dScript getScript() {
        return script;
	}

	public ScriptQueue getResidingQueue() {
		return queue;
	}

	public Long getQueueTime() {
		return queueTime;
	}

	public boolean isInstant() {
		return instant;
	}

    public boolean shouldWaitFor() {
        return waitfor;
    }

    public boolean isDone() {
        return done;
    }

	public ScriptEntry setRunTime(Long newTime) {
		runTime = newTime;
		return this;
	}
	
	public ScriptEntry setArguments(List<String> arguments) {
		args = arguments;
		return this;
	}

	public ScriptEntry setInstant(boolean instant) {
		this.instant = instant;
		return this;
	}

	public ScriptEntry setPlayer(Player player) {
		this.player = player;
		return this;
	}
	
	public ScriptEntry setNPC(dNPC dNPC) {
		this.npc = dNPC;
		return this;
	}
	
	public ScriptEntry setScript(String scriptName) {
		this.script = dScript.valueOf(scriptName);
		return this;
	}

	public void setSendingQueue(ScriptQueue scriptQueue) {
		queue = scriptQueue;
	}
	
}

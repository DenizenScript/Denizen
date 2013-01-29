package net.aufdemrand.denizen.scripts;

import net.aufdemrand.denizen.exceptions.ScriptEntryCreationException;
import net.aufdemrand.denizen.npc.dNPC;
import net.aufdemrand.denizen.scripts.ScriptEngine.QueueType;
import net.aufdemrand.denizen.utilities.arguments.Script;
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


	final private String command;
    final private long queueTime;

	private long allowedRunTime;
	private boolean instant = false;
	private Player player = null;
	private OfflinePlayer offlinePlayer = null;
	private Script script = null;
	private String step = null;
	private dNPC npc = null;
	private QueueType queueType = null;
	private List<String> args = null;


	private Map<String, Object> objects = new HashMap<String, Object>();

	
	public ScriptEntry(String command, String[] arguments) throws ScriptEntryCreationException {
		this(command, arguments, null, null, null, null);
	}

	public ScriptEntry(String command, String[] arguments, Player player) throws ScriptEntryCreationException {
		this(command, arguments, player, null, null, null);
	}

	public ScriptEntry(String command, String[] arguments, Player player, String script) throws ScriptEntryCreationException {
		this(command, arguments, player, null, script, null);
	}

	public ScriptEntry(String command, String[] arguments, dNPC denizen, String script) throws ScriptEntryCreationException {
		this(command, arguments, null, denizen, script, null);
	}

	public ScriptEntry(String command, String[] arguments, Player player, dNPC npc, String script, String step) throws ScriptEntryCreationException {

		if (command == null)
            throw new ScriptEntryCreationException("CommandType cannot be null!");

        // Internal, never null. allowedRunTime can be modified between queue and execution.
		this.queueTime = System.currentTimeMillis();
		this.allowedRunTime = queueTime;

		if (command.startsWith("^")) {
			instant = true;
			command = command.substring(1);
		}

		// Must never be null
		this.command = command.toUpperCase();

		// May be null
		this.player = player;
		this.npc = npc;
		this.args = new ArrayList<String>();
		if (arguments != null) this.args = Arrays.asList(arguments);
		if (script != null) this.script = new Script(script);
		this.step = step;

	}

	/* 
	 * ScriptEntry methods
	 */

	public ScriptEntry addObject(String key, Object object) {
        if (object == null) return this;
		objects.put(key.toUpperCase(), object);
		return this;
	}

	public Long getAllowedRunTime() {
		return allowedRunTime;
	}

	public List<String> getArguments() {
		return args;
	}

	public String getCommand() {
		return command;
	}

	public dNPC getNPC() {
		return npc;
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

	public OfflinePlayer getOfflinePlayer() {
		return offlinePlayer;
	}
	
	public Player getPlayer() {
		return player;
	}

	public Script getScript() {
        return script;
	}

	public QueueType getSendingQueue() {
		return queueType;
	}

	public String getStep() {
        if (step == null) return null;
		else return step.toUpperCase();
	}

	public Long getQueueTime() {
		return queueTime;
	}

	public boolean isInstant() {
		return instant;
	}

	public ScriptEntry setAllowedRunTime(Long newTime) {
		allowedRunTime = newTime;
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

	public ScriptEntry setOfflinePlayer(OfflinePlayer player) {
		this.offlinePlayer = player;
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
		this.script = new Script(scriptName);
		return this;
	}
	
	public ScriptEntry setStep(String step) {
		this.step = step;
		return this;
	}
	
	public void setSendingQueue(QueueType queue) {
		queueType = queue;
	}
	
}

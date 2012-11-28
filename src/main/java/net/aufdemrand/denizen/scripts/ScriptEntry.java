package net.aufdemrand.denizen.scripts;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.script.ScriptException;

import net.aufdemrand.denizen.Denizen;
import net.aufdemrand.denizen.npc.DenizenNPC;
import net.aufdemrand.denizen.scripts.ScriptEngine.QueueType;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;


/**
 * ScriptEntry contain information about a single entry from a DenizenScript. 
 *  
 * @author Jeremy Schroeder
 *
 */

public class ScriptEntry {

	/* 
	 * ScriptEntry variables
	 */

	final private String command; 
	final private Long queueTime;
	private Long allowedRunTime;
	private boolean isInstant = false;

	private Player player = null;
	private OfflinePlayer offlinePlayer = null;
	private String script = null;
	private Integer step = null;
	private DenizenNPC npc = null;
	private QueueType queueType = null;
	private List<String> args = null;
	private Denizen denizen = null;

	private Map<String, Object> objects = new HashMap<String, Object>();

	/* 
	 * ScriptEntry constructors
	 */
	
	public ScriptEntry(String command, String[] arguments) throws ScriptException {
		this(command, arguments, (Player) null, (DenizenNPC) null, (String) null, (Integer) null, (String) null, (String) null);
	}

	public ScriptEntry(String command, String[] arguments, Player player) throws ScriptException {
		this(command, arguments, player, (DenizenNPC) null, (String) null, (Integer) null, (String) null, (String) null);
	}

	public ScriptEntry(String command, String[] arguments, Player player, String script) throws ScriptException {
		this(command, arguments, player, (DenizenNPC) null, script, (Integer) null, (String) null, (String) null);
	}

	public ScriptEntry(String command, String[] arguments, DenizenNPC denizen, String script) throws ScriptException {
		this(command, arguments, (Player) null, denizen, script, (Integer) null, (String) null, (String) null);
	}

	public ScriptEntry(String command, String[] arguments, Player player, DenizenNPC denizen, String script, Integer step) throws ScriptException {
		this(command, arguments, player, denizen, script, step, (String) null, (String) null);
	}

	public ScriptEntry(String command, String[] arguments, Player player, DenizenNPC npc, String script, Integer step, String messageRaw, String messageFormatted) throws ScriptException {

		if (command == null) throw new ScriptException("CommandType cannot be null!");

		if (denizen == null) denizen = (Denizen) Bukkit.getPluginManager().getPlugin("Denizen");

		// Internal, never null. allowedRunTime can be modified between queue and execution.
		this.queueTime = System.currentTimeMillis();
		this.allowedRunTime = queueTime;

		if (command.startsWith("^")) {
			isInstant = true;
			command = command.substring(1);
		}

		// Must never be null
		this.command = command.toUpperCase();

		// May be null
		this.player = player;
		this.npc = npc;
		this.args = new ArrayList<String>();
		if (args != null) this.args = Arrays.asList(arguments);
		this.script = script;
		this.step = step;

	}

	/* 
	 * ScriptEntry methods
	 */

	public ScriptEntry addObject(String key, Object object) {
		objects.put(key.toUpperCase(), object);
		return this;
	}

	public Long getAllowedRunTime() {
		return allowedRunTime;
	}

	// getArguments auto-fills tags.

	public void fillArguments() {
		List<String> filledArgs = new ArrayList<String>();
		if (args != null) {
			for (String argument : args) {
				filledArgs.add(denizen.tagManager().tag(this, argument));
			} 
		}
		args = filledArgs;
	}

	public List<String> getArguments() {
		return args;
	}

	public String getCommand() {
		return command;
	}

	public DenizenNPC getDenizen() {
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

	public OfflinePlayer getOfflinePlayer() {
		return offlinePlayer;
	}
	
	public Player getPlayer() {
		return player;
	}

	public String getScript() {
		return script;
	}

	public QueueType getSendingQueue() {
		return queueType;
	}

	public Integer getStep() {
		return step;
	}

	public Long getQueueTime() {
		return queueTime;
	}

	public boolean isInstant() {
		return isInstant;
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
		isInstant = instant;
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
	
	public ScriptEntry setDenizen(DenizenNPC denizenNPC) {
		this.npc = denizenNPC;
		return this;
	}
	
	public ScriptEntry setScriptName(String scriptName) {
		this.script = scriptName;
		return this;
	}
	
	public ScriptEntry setStep(int Step) {
		this.step = Step;
		return this;
	}
	
	public void setSendingQueue(QueueType queue) {
		queueType = queue;
	}
	
}

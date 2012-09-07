package net.aufdemrand.denizen.scripts;

import javax.script.ScriptException;

import net.aufdemrand.denizen.npc.DenizenNPC;
import net.aufdemrand.denizen.scripts.ScriptEngine.QueueType;

import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;


/**
 * ScriptEntry contain information about a script line read from a script.
 *  
 * @author Jeremy Schroeder
 *
 */

public class ScriptEntry {

	/* Possible scriptEntry data */
	private String theCommand; 
	private Player thePlayer;
	private String theScript = null;
	private Integer theStep = null;
	private DenizenNPC theDenizen = null;

	private QueueType queueType = null;
	private boolean isInstant = false; 
	private LivingEntity theEntity = null;
	private Long[] commandTimes = new Long[2];
	private String[] playerText = new String[2];
	private String[] theArguments = new String[0];
	
	
	/* String value of the type of command */
	public String getCommand() {
		return theCommand;
	}
	
	/* Boolean representing whether the command is an instant command ("^") */
	public boolean isInstant() {
		return isInstant;
	}

	public void setInstant() {
		isInstant = true;
	}
	
	/* Player or LivingEntity object that represents the triggering entity */
	public Player getPlayer() {
		return thePlayer;
	}
	
	public LivingEntity getEntity() {
		if (theEntity != null) return theEntity;
		else return (LivingEntity) thePlayer;
	}
	
	/* the NPC Object of the Denizen associated with the command */
	public DenizenNPC getDenizen() {
		return theDenizen;
	}

	/* String[] of the trailing arguments after the CommandType from the script entry */
	public String[] arguments() {
		return theArguments;
	}

	/* The Script name that triggered the script. */
	public String getScript() {
		return theScript;
	}
	
	/* String[] of the trigger text. 
	 * [0] is the raw text the player typed to initiate the trigger
	 * [1] is the 'friendly' matched text as provided by the trigger: */
	public String[] getTexts() {
		return playerText;
	}

	/* Long[] currentSystemTimeMillis of the times associated with the script
	 * [0] contains time added to the queue
	 * [1] contains the time allowed to trigger */
	public Long getDelayedTime() {
		return commandTimes[1];
	}
	
	public void setDelay(Long newTime) {
		commandTimes[1] = newTime;
	}
	
	public Long getInitiatedTime() {
		return commandTimes[0];
	}
	
	/* Integer of theStep */ 
	public Integer getStep() {
		return theStep;
	}
	
	/* QueueType */ 
	public QueueType sendingQueue() {
		return queueType;
	}
	
	public void setSendingQueue(QueueType queue) {
		queueType = queue;
	}
	
	
	
	/**
	 * Creates a ScriptEntry (For use with a CHAT-like Trigger)
	 */

	public ScriptEntry(String commandType, String[] arguments, Player player, DenizenNPC denizen, String script, Integer step, String theMessage, String formattedText) throws ScriptException {

		if (commandType == null || player == null) throw new ScriptException("CommandType and Player cannot be null!");


		if (commandType.startsWith("^")) {
			isInstant = true;
			commandType = commandType.substring(1);
		}
		
		theStep = step;
		theCommand = commandType.toUpperCase();
		theArguments = arguments;
		thePlayer = player;
		theDenizen = denizen;
		theScript = script;

		commandTimes[0] = System.currentTimeMillis();
		commandTimes[1] = commandTimes[0];

		playerText[0] = theMessage;
		playerText[1] = formattedText;
	}
	

	
	/**
	 * Creates a ScriptEntry (for use with a CLICK-like Trigger)
	 */

	public ScriptEntry(String commandType, String[] arguments, Player player, DenizenNPC denizen, String script, Integer step) throws ScriptException {

		if (commandType == null || player == null) throw new ScriptException("CommandType and Player cannot be null!");

		if (commandType.startsWith("^")) {
			isInstant = true;
			commandType = commandType.substring(1);
		}
		
		theStep = step;
		theCommand = commandType;
		theArguments = arguments;
		thePlayer = player;
		theDenizen = denizen;
		theScript = script;

		commandTimes[0] = System.currentTimeMillis();
		commandTimes[1] = commandTimes[0];
	}


	
	/**
	 * Creates a ScriptEntry (for use with a TASK-like Trigger)
	 */

	public ScriptEntry(String commandType, String[] arguments, Player player, String script) throws ScriptException {

		if (commandType == null || player == null) throw new ScriptException("CommandType and Player cannot be null!");

		if (commandType.startsWith("^")) {
			isInstant = true;
			commandType = commandType.substring(1);
		}
		
		theCommand = commandType;
		theArguments = arguments;
		thePlayer = player;
		theScript = script;

		commandTimes[0] = System.currentTimeMillis();
		commandTimes[1] = commandTimes[0];
	}
	
	
	
	/**
	 * Creates a ScriptEntry with the bare minimum (Player, commandType, and arguments)
	 */

	public ScriptEntry(String commandType, String[] arguments, Player player) throws ScriptException {

		if (commandType == null || player == null) throw new ScriptException("CommandType and Player cannot be null!");

		if (commandType.startsWith("^")) {
			isInstant = true;
			commandType = commandType.substring(1);
		}
		
		theCommand = commandType;
		theArguments = arguments;
		thePlayer = player;

		commandTimes[0] = System.currentTimeMillis();
		commandTimes[1] = commandTimes[0];
	}

	
	/**
	 * Creates a ScriptEntry with the bare minimum (Denizen, commandType, and arguments)
	 */

	public ScriptEntry(String commandType, String[] arguments, DenizenNPC theDenizen, String theScript) throws ScriptException {

		if (commandType == null || theDenizen == null) throw new ScriptException("CommandType and Denizen cannot be null!");

		if (commandType.startsWith("^")) {
			isInstant = true;
			commandType = commandType.substring(1);
		}
		
		this.theScript = theScript;
		theCommand = commandType;
		theArguments = arguments;
		this.theDenizen = theDenizen;

		commandTimes[0] = System.currentTimeMillis();
		commandTimes[1] = commandTimes[0];
	}


}

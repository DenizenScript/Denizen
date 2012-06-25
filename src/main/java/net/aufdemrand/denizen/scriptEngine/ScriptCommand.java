package net.aufdemrand.denizen.scriptEngine;

import net.aufdemrand.denizen.scriptEngine.ScriptEngine.TriggerType;
import net.citizensnpcs.api.npc.NPC;

import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;


/**
 * ScriptCommands contain information about a command read from a script.
 * They reside in the player/denizenQueues.
 *  
 * @author Jeremy Schroeder
 *
 */

public class ScriptCommand {

	/* theCommand
	 *   String value of the type of command */
	private String theCommand; 
	
	public String getCommandType() {
		return theCommand.toUpperCase();
	}
	
	
	
	/* instant
	 *   Boolean representing whether the command is an instant command ("^") */
	private boolean instant; 
	
	public boolean instant() {
		return instant;
	}
	
	
	
	/* thePlayer/theEntity
	 *   Player or LivingEntity object that represents the triggering entity */
	private Player thePlayer;
	private LivingEntity theEntity;

	public Player getPlayer() {
		return thePlayer;
	}
	
	public LivingEntity getEntity() {
		if (theEntity != null) return theEntity;
		else return (LivingEntity) thePlayer;
	}
	
	
	
	/* theDenizen
	 *   the NPC Object of the Denizen associated with the command */
	private NPC theDenizen;
	
	public NPC getDenizen() {
		return theDenizen;
	}

	
	
	/* theArguments
	 *   String[] of the trailing arguments after the CommandType from the script entry */
	private String[] theArguments;
	
	public String[] arguments() {
		return theArguments;
	}

	
	
	/* theScript
	 *   The Script name that triggered the script. */
	private String theScript;

	public String getScriptName() {
		return theScript;
	}
	
	public String getScript() {
		return theScript;
	}
	
	
	
	/* theBookmark
	 *   The Location of the Bookmark associated */
	private Location theBookmark;  

	public Location getBookmarkLocation() {
		return theBookmark;
	}
	
	public void setBookmarkLocation(Location bookmarkLocation) {
		theBookmark = bookmarkLocation;
	}
	
	
	
	/* playerText
	 *   String[] of the trigger text. [0] is the raw text the player typed to initiate the trigger
	 *   [1] is the 'friendly' matched text as provided by the trigger: */
	private String[] playerText = new String[2];
	
	public String[] getTexts() {
		return playerText;
	}

	
	
	/* commandTime
	 *   Long[] currentSystemTimeMillis of the times associated with the script
	 *   [0] contains time added to the queue
	 *   [1] contains the time allowed to trigger */
	private Long[] commandTimes = new Long[2];

	public Long getDelayedTime() {
		return commandTimes[1];
	}
	
	
	
	/*  triggerType
	 *    TriggerType ENUM value of the type of trigger that was used to add the entry */ 
	private TriggerType triggerType;
	
	public TriggerType getTriggerType() {
		return triggerType;
	}

	
	
	/**
	 * Creates a ScriptCommand (For use with a CHAT Trigger)
	 */

	public ScriptCommand(String commandType, String[] arguments, Player player, NPC denizen, String script, Integer step, String theMessage, String formattedText) throws Exception {

		if (player == null || denizen == null || script == null) throw new Exception("Values cannot be null!");

		if (commandType.startsWith("^")) {
			instant = true;
			commandType = commandType.substring(1);
		}
		
		theCommand = commandType;
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
	 * Creates a ScriptCommand (for use with a CLICK/DAMAGE Trigger)
	 */

	public ScriptCommand(String commandType, String[] arguments, Player player, NPC denizen, String script, Integer step) throws Exception {

		if (player == null || denizen == null || script == null) throw new Exception("Values cannot be null!");

		if (commandType.startsWith("^")) {
			instant = true;
			commandType = commandType.substring(1);
		}
		
		theCommand = commandType;
		theArguments = arguments;
		thePlayer = player;
		theDenizen = denizen;
		theScript = script;

		commandTimes[0] = System.currentTimeMillis();
		commandTimes[1] = commandTimes[0];
	}


	
	/**
	 * Creates a ScriptCommand (for use with a TASK Trigger)
	 */

	public ScriptCommand(String commandType, String[] arguments, Player player, String script, Integer step) throws Exception {

		if (player == null || script == null) throw new Exception("Values cannot be null!");

		if (commandType.startsWith("^")) {
			instant = true;
			commandType = commandType.substring(1);
		}
		
		theCommand = commandType;
		theArguments = arguments;
		thePlayer = player;
		theScript = script;

		commandTimes[0] = System.currentTimeMillis();
		commandTimes[1] = commandTimes[0];
	}



}

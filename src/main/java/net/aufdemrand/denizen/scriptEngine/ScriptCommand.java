package net.aufdemrand.denizen.scriptEngine;

import java.util.Arrays;

import javax.script.ScriptException;

import net.aufdemrand.denizen.commands.Executer.CommandType;
import net.aufdemrand.denizen.scriptEngine.ScriptEngine.ScriptType;
import net.aufdemrand.denizen.scriptEngine.ScriptEngine.TriggerType;
import net.citizensnpcs.api.npc.NPC;

import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

public class ScriptCommand {

	/* ScriptCommands contain information about a command that is read from a script.
	 * 
	 * commandType
	 *   CommandType ENUM value of the type of command */
	private CommandType commandType; 
	
	public CommandType getCommandType() {
		return commandType;
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

	
	/*  scriptType
	 *    ScriptType ENUM value of the type of script this originated from */
	private ScriptType scriptType;

	public ScriptType getScriptType() {
		return scriptType;
	}

	
	
	/**
	 * Creates a ScriptCommand for a Trigger Script
	 * @return
	 */

	public ScriptCommand(Player player, NPC denizen, String script, Integer step, TriggerType trigger) throws Exception {

		if (player == null || denizen == null || script == null || trigger == null) throw new Exception("Values cannot be null!");

		thePlayer = player;
		theDenizen = denizen;
		theScript = script;
		triggerType = trigger;

		scriptType = ScriptType.TRIGGER;
		commandTimes[0] = System.currentTimeMillis();
		commandTimes[1] = commandTimes[0];
	}
	
	public ScriptCommand(Player player) {
				
	}

	public void setTexts(String theText, String formattedText) {
		playerText[0] = theText;
		playerText[1] = formattedText;
	}
	

	
	
	/**
	 * Creates a ScriptCommand for a Trigger Script
	 * @return
	 */

	public void create(Player player, String script, Integer step, TriggerType trigger) throws Exception {

		if (player == null || denizen == null || script == null || trigger == null) throw new Exception("Values cannot be null!");

		thePlayer = player;
		theDenizen = denizen;
		theScript = script;
		triggerType = trigger;

		scriptType = ScriptType.TRIGGER;
		commandTimes[0] = System.currentTimeMillis();
		commandTimes[1] = commandTimes[0];
	}




	



	/**
	 * 
	 * @return returns the script name
	 */

	public String getScript() {
		return theScript;
	}








}

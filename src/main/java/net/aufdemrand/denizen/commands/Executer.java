package net.aufdemrand.denizen.commands;

import net.aufdemrand.denizen.Denizen;
import net.aufdemrand.denizen.scriptEngine.ScriptCommand;

import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

public class Executer {
	
	
	public static enum CommandType {
		WAIT, ZAP, SPAWN, CHANGE, WEATHER, EFFECT, GIVE, TAKE, HEAL,
		TELEPORT, STRIKE, WALK, REMEMBER, RESPAWN, PERMISS, EXECUTE, SHOUT,
		WHISPER, CHAT, ANNOUNCE, GRANT, HINT, RETURN, LOOK, WALKTO, FINISH, 
		FOLLOW, CAST, NARRATE, ENGAGE, DISENGAGE, SWITCH, PRESS, HURT, 
		REFUSE, WAITING, RESET, FAIL, SPAWNMOB, EMOTE, ATTACK, PLAYERTASK, 
		RUNTASK, DROP
	} 

	private Denizen plugin;
	
	public Executer(Denizen denizen) {
		plugin = denizen;
	}
	
	
	/*
	 * Executes a command defined in theCommand 
	 */

	public boolean execute(ScriptCommand theCommand) {
		
		return false;
	}
	
	
}
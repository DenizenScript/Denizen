package net.aufdemrand.denizen.commands;

import net.aufdemrand.denizen.Denizen;

import org.bukkit.entity.LivingEntity;

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
	 * Executes a command defined in theStep (not to be confused with currentStep ;)
	 * 
	 * I am attempting to keep down the size of this method by branching out large
	 * sections of code into their own methods.
	 *
	 * These commands normally come from the playerQue or denizenQue, but don't have
	 * to necessarily, as long as the proper format is sent in theStep.
	 * 
	 * Syntax of theStep -- elements are divided by semi-colons.
	 * 0 Denizen ID; 1 Script Name; 2 Step Number; 3 Time added to Queue; 4 Command
	 */

	public boolean execute(LivingEntity theEntity, String theCommand, String[] theArgs ) {

		
		
		return false;
	}
	
	
}
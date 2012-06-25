package net.aufdemrand.denizen.commands;

import java.lang.Exception;
import java.rmi.activation.ActivationException;

import net.citizensnpcs.api.npc.NPC;

import org.bukkit.entity.Player;

public abstract class Command {

	
	
	public void activateAs(String commandName) throws ActivationException {
		
		//if (registerCommand(commandName, this)) return;
		//else 
			//throw new ActivationException("Error activating Command with CommandRegistry!");
	}


	/*
	 * Execute for Trigger Script
	 */

	public abstract boolean execute(Player thePlayer, NPC theDenizen, String[] arguments, String theText);


	/*
	 * Execute for Task Script
	 */

	public abstract boolean execute(Player thePlayer, String[] arguments);


	/*
	 * Execute for Activity Script
	 */

	public abstract boolean execute(NPC theDenizen, String[] arguments);



}

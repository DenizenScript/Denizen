package net.aufdemrand.denizen.commands;

import net.citizensnpcs.api.npc.NPC;

import org.bukkit.entity.Player;

public interface Command {
	
	
	/*
	 * Parse for Trigger Script
	 */
	
	public abstract boolean execute(Player thePlayer, NPC theDenizen, String[] arguments, String theText);
	
	
	/*
	 * Parse for Task Script
	 */
	
	public abstract boolean execute(Player thePlayer, String[] arguments);
	
	
	/*
	 * Execute for Activity Script
	 */
	
	public abstract boolean execute(NPC theDenizen, String[] arguments);
	


}

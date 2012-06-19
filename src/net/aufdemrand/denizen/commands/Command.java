package net.aufdemrand.denizen.commands;

import net.citizensnpcs.api.npc.NPC;

import org.bukkit.entity.Player;

public interface Command {

	public abstract boolean execute(Player thePlayer, NPC theDenizen, String[] arguments);
	
}

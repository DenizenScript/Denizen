package net.aufdemrand.denizen;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;

import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChatEvent;
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;

public class DenizenListener implements Listener {

	Denizen plugin;
	public DenizenListener(Denizen instance) { plugin = instance; }

	@EventHandler
	public void PlayerChatListener(PlayerChatEvent event) {

		List<net.citizensnpcs.api.npc.NPC> DenizenList = GetDenizensWithinRange(event.getPlayer().getLocation(), event.getPlayer().getWorld(), Denizen.PlayerChatRangeInBlocks);
		if (DenizenList == null) { return; }

		for (net.citizensnpcs.api.npc.NPC thisDenizen : DenizenList) {

			String theScript = GetScript(thisDenizen, event.getPlayer());
			if (theScript != null) { 



			}

		}
	}



	// GET SCRIPTS
	public String GetScript(net.citizensnpcs.api.npc.NPC thisDenizen, Player thisPlayer) {
		String theScript = null;
		List<String> ScriptList = plugin.getConfig().getStringList("Denizens." + thisDenizen.getId() + ".Scripts");
		if (ScriptList.isEmpty()) { return null; }

		List<String> ScriptsThatMeetRequirements = null;

		for (String thisScript : ScriptList) {
			if (CheckRequirements(thisScript, thisPlayer) == true) { ScriptsThatMeetRequirements.add(thisScript); }
		}

		
		// Now which  one to execute?

		return theScript;
	}



	// CHECK REQUIREMENTS

	public boolean CheckRequirements(String thisScript, Player thisPlayer) {

		String RequirementsMode = plugin.getConfig().getString("Scripts." + thisScript + ".Requirements.Mode");
		List<String> RequirementsList = plugin.getConfig().getStringList("Scripts." + thisScript + ".Requirements.List");
		if (RequirementsList.isEmpty()) { return true; }

		int NumberOfMetRequirements = 0;
		
		for (String Requirement : RequirementsList) {

		//	None, Time Day, Time Night, Precipitation, No Precipitation, permission, group

			String[] RequirementArgs = Requirement.split(" ");
			
			if (Requirement.equalsIgnoreCase("none")) { return true; }
			if (Requirement.equalsIgnoreCase("time day") && thisPlayer.getWorld().getTime() < 13500) { NumberOfMetRequirements++; }
			if (Requirement.equalsIgnoreCase("time night") && thisPlayer.getWorld().getTime() > 13500) { NumberOfMetRequirements++; }
			if (RequirementArgs[0].equalsIgnoreCase("permission") && thisPlayer.hasPermission(RequirementArgs[1]) == true) { NumberOfMetRequirements++; }
			if (Requirement.equalsIgnoreCase("precipitation") && thisPlayer.getWorld().hasStorm() == true) { NumberOfMetRequirements++; }
			if (Requirement.equalsIgnoreCase("no precipitation") && thisPlayer.getWorld().hasStorm() == false) { NumberOfMetRequirements++; }
			if (RequirementArgs[0].equalsIgnoreCase("level") && thisPlayer.getLevel() >= Integer.parseInt(RequirementArgs[1])) { NumberOfMetRequirements++; }
			if (Requirement.equalsIgnoreCase("starving") && thisPlayer.getSaturation() == 0) { NumberOfMetRequirements++; }
			if (Requirement.equalsIgnoreCase("full") && thisPlayer.getSaturation() > 10) { NumberOfMetRequirements++; }
			
			RequirementArgs[0].
			 nb
		}

		if (RequirementsMode.equalsIgnoreCase("all")) {

		}

		return false;
	}




	// GET DENIZENS WITHIN RANGE
	public List<net.citizensnpcs.api.npc.NPC> GetDenizensWithinRange (Location PlayerLocation, World PlayerWorld, int Distance) {

		List<net.citizensnpcs.api.npc.NPC> DenizensWithinRange = null;
		Collection<net.citizensnpcs.api.npc.NPC> DenizenNPCs = CitizensAPI.getNPCManager().getNPCs(DenizenCharacter.class); 
		if (DenizenNPCs.isEmpty()) { return null; }
		List<net.citizensnpcs.api.npc.NPC> DenizenList = new ArrayList<NPC>(DenizenNPCs);
		for (int x = 0; x < DenizenList.size(); x++) {
			if (DenizenList.get(x).getBukkitEntity().getWorld().equals(PlayerWorld)) {
				if (DenizenList.get(x).getBukkitEntity().getLocation().distance(PlayerLocation) < Distance) {
					DenizensWithinRange.add(DenizenList.get(x));
				}
			}
		}
		return DenizensWithinRange;
	}

}
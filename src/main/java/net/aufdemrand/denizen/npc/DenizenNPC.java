package net.aufdemrand.denizen.npc;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

import org.bukkit.craftbukkit.entity.CraftLivingEntity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import net.aufdemrand.denizen.Denizen;
import net.aufdemrand.denizen.npc.SpeechEngine.Reason;
import net.aufdemrand.denizen.npc.SpeechEngine.TalkType;
import net.aufdemrand.denizen.scriptEngine.ScriptHelper;
import net.citizensnpcs.api.ai.Navigator;
import net.citizensnpcs.api.npc.NPC;

public class DenizenNPC {

	private NPC citizensNPC;
	private Denizen plugin;
	private ScriptHelper sE;
	
	
	
	DenizenNPC(NPC citizensNPC) {
		this.citizensNPC = citizensNPC;
		this.plugin = (Denizen) Bukkit.getServer().getPluginManager().getPlugin("Denizen");
		this.sE = plugin.getScriptEngine().helper;
	}


	public NPC getCitizensEntity() {
		return citizensNPC;
	}


	public LivingEntity getEntity() {
		return citizensNPC.getBukkitEntity();
	}


	public void talk(TalkType talkType, Player thePlayer, String theText) {
		plugin.getSpeechEngine().talk(this, thePlayer, theText, talkType);
	}


	public void talk(TalkType talkType, Player thePlayer, Reason theReason) {
		// TODO: Finish before 0.7 release.
	}


	public boolean isToggled() {
		return citizensNPC.getTrait(DenizenTrait.class).isToggled();
	}


	public EntityType getEntityType() {
		return citizensNPC.getBukkitEntity().getType();
	}

	
	public Navigator getNavigator() {
		return citizensNPC.getNavigator();
	}


	public int getId() {
		return citizensNPC.getId();
	}


	public String getName() {
		return citizensNPC.getName();
	}


	public void showInfo(Player theClicker) {
		sE.showInfo(theClicker, citizensNPC);
	}


	public boolean IsInteractable(String triggerName, Player thePlayer) {
		return sE.denizenIsInteractable(triggerName, citizensNPC, thePlayer);
	}


	public String getInteractScript(Player thePlayer) {
		return sE.getInteractScript(citizensNPC, thePlayer);
	}


	public boolean isSpawned() {
		return citizensNPC.isSpawned();
	}
	
	
	public Location getLocation() {
		return citizensNPC.getBukkitEntity().getLocation();
	}


	public World getWorld() {
	return citizensNPC.getBukkitEntity().getWorld();
	}
	
	
	public void setHealth(int newHealth) {
		((CraftLivingEntity) getEntity()).getHandle().setHealth(newHealth);
	}
	
	@Override
	public String toString() {
		return "DenizenNPC " + citizensNPC.getName() + "/" + citizensNPC.getId();
	}
	
}

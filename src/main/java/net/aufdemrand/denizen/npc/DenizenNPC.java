package net.aufdemrand.denizen.npc;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;


import org.bukkit.craftbukkit.v1_4_6.entity.CraftLivingEntity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import net.aufdemrand.denizen.Denizen;
import net.aufdemrand.denizen.npc.traits.AssignmentTrait;
import net.aufdemrand.denizen.scripts.ScriptHelper;
import net.aufdemrand.denizen.scripts.commands.core.EngageCommand;
import net.aufdemrand.denizen.scripts.triggers.AbstractTrigger;
import net.citizensnpcs.api.ai.Navigator;
import net.citizensnpcs.api.npc.NPC;
import net.minecraft.server.v1_4_6.EntityLiving;


public class DenizenNPC {

	private NPC npc;
	private Denizen plugin;
	private ScriptHelper sH;

	DenizenNPC(NPC citizensNPC) {
		this.npc = citizensNPC;
		this.plugin = (Denizen) Bukkit.getServer().getPluginManager().getPlugin("Denizen");
		this.sH = plugin.getScriptEngine().getScriptHelper();
	}

	public EntityLiving getHandle() {
		return ((CraftLivingEntity) getEntity()).getHandle();
	}

	public NPC getCitizen() {
		return npc;
	}

	public LivingEntity getEntity() {
		try { return npc.getBukkitEntity();
		} catch (NullPointerException e) { return null;	}
	}

	public EntityType getEntityType() {
		return npc.getBukkitEntity().getType();
	}

	public Navigator getNavigator() {
		return npc.getNavigator();
	}

	public int getId() {
		return npc.getId();
	}

	public String getName() {
		return npc.getName();
	}

	public void showInfo(Player theClicker) {
		// plugin.getNPCRegistry().showInfo(theClicker, this);
	}

	public String getInteractScript(Player thePlayer, Class<? extends AbstractTrigger> triggerType) {
		return sH.getInteractScript(getCitizen(), thePlayer, triggerType);
	}

	public boolean isSpawned() {
		return npc.isSpawned();
	}

	public Location getLocation() {
		return npc.getBukkitEntity().getLocation();
	}

	public World getWorld() {
		return npc.getBukkitEntity().getWorld();
	}

	public void setHealth(int newHealth) {
		((CraftLivingEntity) getEntity()).getHandle().setHealth(newHealth);
	}
	
	public int getHealth() {
        return ((CraftLivingEntity) getEntity()).getHandle().getHealth();
    }
    
	@Override
	public String toString() {
		return npc.getName() + "/" + npc.getId();
	}

	public boolean isInteracting() {
		if (!plugin.getCommandRegistry().get(EngageCommand.class).getEngaged(getCitizen())) return true;
		else return false;
	}

	public String getAssignment() {
		return npc.getTrait(AssignmentTrait.class).getAssignment();
	}

	public boolean setAssignment(String assignment) {
		return npc.getTrait(AssignmentTrait.class).setAssignment(assignment);
	}
	
	public void action(String actionName, Player player) {
	    if (npc.hasTrait(AssignmentTrait.class)) 
	        plugin.getNPCRegistry().getActionHandler().doAction(actionName, this, player, getAssignment());
	}

}

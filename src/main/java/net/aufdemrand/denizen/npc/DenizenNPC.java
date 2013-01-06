package net.aufdemrand.denizen.npc;

import net.aufdemrand.denizen.Denizen;
import net.aufdemrand.denizen.npc.traits.AssignmentTrait;
import net.aufdemrand.denizen.scripts.ScriptHelper;
import net.aufdemrand.denizen.scripts.commands.core.EngageCommand;
import net.aufdemrand.denizen.scripts.triggers.AbstractTrigger;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.ai.Navigator;
import net.citizensnpcs.api.npc.NPC;
import net.minecraft.server.v1_4_6.EntityLiving;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_4_6.entity.CraftLivingEntity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

public class DenizenNPC {

	private NPC npc;
    private int npcid;
	private Denizen plugin;
	private ScriptHelper sH;

	DenizenNPC(NPC citizensNPC) {
		this.npc = citizensNPC;
        this.npcid = citizensNPC.getId();
		this.plugin = (Denizen) Bukkit.getServer().getPluginManager().getPlugin("Denizen");
		this.sH = plugin.getScriptEngine().getScriptHelper();
	}

	public EntityLiving getHandle() {
		return ((CraftLivingEntity) getEntity()).getHandle();
	}

	public NPC getCitizen() {
		if (npc != null)
            return npc;
        else {
            this.npc = CitizensAPI.getNPCRegistry().getById(npcid);
            if (npc == null)
                dB.log("Uh oh! Denizen has encountered an NPE while trying to fetch a NPC. Has this NPC been removed?");
            return npc;
        }
	}

	public LivingEntity getEntity() {
		try {
            return getCitizen().getBukkitEntity();
		} catch (NullPointerException e) {
            dB.log("Uh oh! Denizen has encountered an NPE while trying to fetch a NPC entity. Has this NPC been removed?");
            return null;
        }
	}

	public EntityType getEntityType() {
		return getCitizen().getBukkitEntity().getType();
	}

	public Navigator getNavigator() {
		return getCitizen().getNavigator();
	}

	public int getId() {
		return getCitizen().getId();
	}

	public String getName() {
		return getCitizen().getName();
	}

	public String getInteractScript(Player thePlayer, Class<? extends AbstractTrigger> triggerType) {
		return sH.getInteractScript(getCitizen(), thePlayer, triggerType);
	}

	public boolean isSpawned() {
		return getCitizen().isSpawned();
	}

	public Location getLocation() {
		return getCitizen().getBukkitEntity().getLocation();
	}

	public World getWorld() {
		return getCitizen().getBukkitEntity().getWorld();
	}

	public void setHealth(int newHealth) {
		((CraftLivingEntity) getEntity()).getHandle().setHealth(newHealth);
	}
	
	public int getHealth() {
        return ((CraftLivingEntity) getEntity()).getHandle().getHealth();
    }
    
	@Override
	public String toString() {
		return getCitizen().getName() + "/" + getCitizen().getId();
	}

	public boolean isInteracting() {
		if (!plugin.getCommandRegistry().get(EngageCommand.class).getEngaged(getCitizen())) return true;
		else return false;
	}

    public String getAssignment() {
        if (getCitizen().hasTrait(AssignmentTrait.class))
            return getCitizen().getTrait(AssignmentTrait.class).getAssignment();
        else return null;
    }

    public boolean hasAssignment() {
        if (!getCitizen().hasTrait(AssignmentTrait.class)) return false;
        else return getCitizen().getTrait(AssignmentTrait.class).hasAssignment();
    }

	public boolean setAssignment(String assignment) {
		if (!getCitizen().hasTrait(AssignmentTrait.class)) getCitizen().addTrait(AssignmentTrait.class);
        return getCitizen().getTrait(AssignmentTrait.class).setAssignment(assignment);
	}
	
	public void action(String actionName, Player player) {
	    if (getCitizen().hasTrait(AssignmentTrait.class))
	        plugin.getNPCRegistry().getActionHandler().doAction(actionName, this, player, getAssignment());
	}

}

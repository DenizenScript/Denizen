package net.aufdemrand.denizen.objects;

import net.aufdemrand.denizen.interfaces.dScriptArgument;
import net.aufdemrand.denizen.npc.traits.AssignmentTrait;
import net.aufdemrand.denizen.npc.traits.HealthTrait;
import net.aufdemrand.denizen.npc.traits.NicknameTrait;
import net.aufdemrand.denizen.npc.traits.TriggerTrait;
import net.aufdemrand.denizen.scripts.commands.core.EngageCommand;
import net.aufdemrand.denizen.scripts.containers.core.InteractScriptContainer;
import net.aufdemrand.denizen.scripts.containers.core.InteractScriptHelper;
import net.aufdemrand.denizen.scripts.triggers.AbstractTrigger;
import net.aufdemrand.denizen.tags.Attribute;
import net.aufdemrand.denizen.utilities.DenizenAPI;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.ai.Navigator;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.trait.trait.Owner;
import net.minecraft.server.v1_5_R3.EntityLiving;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_5_R3.entity.CraftLivingEntity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

public class dNPC implements dScriptArgument {

    private int npcid;
    private final org.bukkit.Location locationCache = new org.bukkit.Location(null, 0, 0, 0);

    public dNPC(NPC citizensNPC) {
        this.npcid = citizensNPC.getId();
    }

    public EntityLiving getHandle() {
        return ((CraftLivingEntity) getEntity()).getHandle();
    }

    public NPC getCitizen() {
        NPC npc = CitizensAPI.getNPCRegistry().getById(npcid);
        if (npc == null)
            dB.log("Uh oh! Denizen has encountered an NPE while trying to fetch a NPC. Has this NPC been removed?");
        return npc;
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

    public InteractScriptContainer getInteractScript(Player player, Class<? extends AbstractTrigger> triggerType) {
        return InteractScriptHelper.getInteractScript(this, player, triggerType);
    }

    public InteractScriptContainer getInteractScriptQuietly(Player player, Class<? extends AbstractTrigger> triggerType) {
        boolean db = dB.debugMode;
        dB.debugMode = false;
        InteractScriptContainer script = InteractScriptHelper.getInteractScript(this, player, triggerType);
        dB.debugMode = db;
        return script;
    }

    public dLocation getLocation() {
        if (isSpawned()) return
                new dLocation(getCitizen().getBukkitEntity().getLocation(locationCache));
        else return null;
    }

    public dLocation getEyeLocation() {
        if (isSpawned()) return
                new dLocation(getCitizen().getBukkitEntity().getEyeLocation());
        else return null;
    }

    public World getWorld() {
        if (isSpawned()) return getEntity().getWorld();
        else return null;
    }

    @Override
    public String toString() {
        return getCitizen().getName() + "/" + getCitizen().getId();
    }

    public boolean isEngaged() {
        return EngageCommand.getEngaged(getCitizen());
    }

    public boolean isSpawned() {
        return getCitizen().isSpawned();
    }

    public boolean isVulnerable() {
        return true;
    }

    public String getOwner() {
        return getCitizen().getTrait(Owner.class).getOwner();
    }

    public AssignmentTrait getAssignmentTrait() {
        if (!getCitizen().hasTrait(AssignmentTrait.class))
            getCitizen().addTrait(AssignmentTrait.class);
        return getCitizen().getTrait(AssignmentTrait.class);
    }

    public NicknameTrait getNicknameTrait() {
        if (!getCitizen().hasTrait(NicknameTrait.class))
            getCitizen().addTrait(NicknameTrait.class);
        return getCitizen().getTrait(NicknameTrait.class);
    }

    public HealthTrait getHealthTrait() {
        if (!getCitizen().hasTrait(HealthTrait.class))
            getCitizen().addTrait(HealthTrait.class);
        return getCitizen().getTrait(HealthTrait.class);
    }

    public TriggerTrait getTriggerTrait() {
        if (!getCitizen().hasTrait(TriggerTrait.class))
            getCitizen().addTrait(TriggerTrait.class);
        return getCitizen().getTrait(TriggerTrait.class);
    }

    public void action(String actionName, Player player) {
    	if (getCitizen() != null)
    	{
    		if (getCitizen().hasTrait(AssignmentTrait.class))
    			DenizenAPI.getCurrentInstance().getNPCRegistry()
                    .getActionHandler().doAction(
                    actionName,
                    this,
                    player,
                    getAssignmentTrait().getAssignment());
    	}
    }



    private String prefix = "npc";

    @Override
    public String getPrefix() {
        return prefix;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public String debug() {
        return null;
    }

    @Override
    public boolean isUnique() {
        return true;
    }

    @Override
    public String getType() {
        return "npc";
    }

    @Override
    public String identify() {
        return "n@" + npcid;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public dNPC setPrefix(String prefix) {

        return this;
    }

    @Override
    public String getAttribute(Attribute attribute) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }
}

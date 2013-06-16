package net.aufdemrand.denizen.objects;

import net.aufdemrand.denizen.npc.traits.AssignmentTrait;
import net.aufdemrand.denizen.npc.traits.HealthTrait;
import net.aufdemrand.denizen.npc.traits.NicknameTrait;
import net.aufdemrand.denizen.npc.traits.TriggerTrait;
import net.aufdemrand.denizen.scripts.commands.npc.EngageCommand;
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
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_5_R3.entity.CraftLivingEntity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;

public class dNPC implements dObject {

    @ObjectFetcher("n")
    public static dNPC valueOf(String string) {
        if (string == null) return null;

        ////////
        // Match NPC id

        string = string.replace("n@", "");
        NPC npc;
        if (aH.matchesInteger(string)) {
            npc = CitizensAPI.getNPCRegistry().getById(aH.getIntegerFrom(string));
            if (npc != null) return new dNPC(npc);
        }

        return null;
    }


    public static boolean matches(String string) {

        string = string.replace("n@", "");
        NPC npc;
        if (aH.matchesInteger(string)) {
            npc = CitizensAPI.getNPCRegistry().getById(aH.getIntegerFrom(string));
            if (npc != null) return true;
        }

        return false;
    }

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

    public InteractScriptContainer getInteractScript(dPlayer player, Class<? extends AbstractTrigger> triggerType) {
        return InteractScriptHelper.getInteractScript(this, player, triggerType);
    }

    public InteractScriptContainer getInteractScriptQuietly(dPlayer player, Class<? extends AbstractTrigger> triggerType) {
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

    public void action(String actionName, dPlayer player) {
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
        return (prefix + "='<A>" + identify() + "<G>'  ");
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

        if (type.equals("NAME")) {
            event.setReplaced(ChatColor.stripColor(n.getName()));
            if (subType.equals("NICKNAME")) {
                if (n.getCitizen().hasTrait(NicknameTrait.class))
                    event.setReplaced(n.getCitizen().getTrait(NicknameTrait.class).getNickname());
            }

        } else if (type.equals("HEALTH")) {

            if (subType.equals("MAX"))
                event.setReplaced(String.valueOf(n.getHealthTrait().getMaxhealth()));
            else
                event.setReplaced(String.valueOf(n.getHealthTrait().getHealth()));

        } else if (type.equals("TYPE")) {
            if (subType.equals("FORMATTED"))
                event.setReplaced(String.valueOf(n.getEntityType().name().toLowerCase().replace('_', ' ')));
            else
                event.setReplaced(String.valueOf(n.getEntityType().name()));

        } else if (type.equals("ID")) {
            event.setReplaced(String.valueOf(n.getId()));

        } else if (type.equals("OWNER")) {
            event.setReplaced(String.valueOf(n.getOwner()));

        } else if (type.equals("LOCATION")) {
            dLocation loc = n.getLocation();
            event.setReplaced(loc.getX()
                    + "," + loc.getY()
                    + "," + loc.getZ()
                    + "," + n.getWorld().getName());
            if (subType.equals("BLOCK"))
                event.setReplaced(loc.getBlockX()
                        + "," + loc.getBlockY()
                        + "," + loc.getBlockZ()
                        + "," + n.getWorld().getName());
            else if (subType.equals("FORMATTED"))
                event.setReplaced("X '" + loc.getX()
                        + "', Y '" + loc.getY()
                        + "', Z '" + loc.getZ()
                        + "', in world '" + n.getWorld().getName() + "'");
            else if (subType.equals("X"))
                event.setReplaced(String.valueOf(n.getLocation().getX()));
            else if (subType.equals("Y"))
                event.setReplaced(String.valueOf(n.getLocation().getY()));
            else if (subType.equals("Z"))
                event.setReplaced(String.valueOf(n.getLocation().getZ()));
            else if (subType.equals("STANDING_ON"))
                event.setReplaced(loc.add(0, -1, 0).getBlock().getType().name());
            else if (subType.equals("STANDING_ON_DISPLAY"))
                event.setReplaced(n.getLocation().add(0, -1, 0).getBlock().getType().name().toLowerCase().replace('_', ' '));
            else if (subType.equals("WORLD_SPAWN"))
                event.setReplaced(n.getWorld().getSpawnLocation().getX()
                        + "," + n.getWorld().getSpawnLocation().getY()
                        + "," + n.getWorld().getSpawnLocation().getZ()
                        + "," + n.getWorld().getName());
            else if (subType.equals("WORLD"))
                event.setReplaced(n.getWorld().getName());
            else if (subType.equals("PREVIOUS_LOCATION"))
                if (previousLocations.containsKey(n.getId()))
                    event.setReplaced(previousLocations.get(n.getId()).identify());

        } else if (type.equals("NAVIGATOR")) {
            if (subType.equals("IS_NAVIGATING"))
                event.setReplaced(Boolean.toString(n.getNavigator().isNavigating()));
            else if (subType.equals("SPEED"))
                event.setReplaced(String.valueOf(n.getNavigator().getLocalParameters().speedModifier()));
            else if (subType.equals("AVOID_WATER"))
                event.setReplaced(Boolean.toString(n.getNavigator().getLocalParameters().avoidWater()));
            else if (subType.equals("TARGET_LOCATION")) {
                dLocation loc = new dLocation(n.getNavigator().getTargetAsLocation());
                if (loc != null) event.setReplaced(loc.identify());
            } else if (subType.equals("IS_FIGHTING")) {
                event.setReplaced(String.valueOf(event.getNPC().getNavigator().getEntityTarget().isAggressive()));
            } else if (subType.equals("TARGET_TYPE")) {
                event.setReplaced(event.getNPC().getNavigator().getTargetType().toString());
            }

        }


    }
}

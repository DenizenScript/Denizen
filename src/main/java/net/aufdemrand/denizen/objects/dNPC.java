package net.aufdemrand.denizen.objects;

import net.aufdemrand.denizen.flags.FlagManager;
import net.aufdemrand.denizen.npc.dNPCRegistry;
import net.aufdemrand.denizen.npc.traits.AssignmentTrait;
import net.aufdemrand.denizen.npc.traits.HealthTrait;
import net.aufdemrand.denizen.npc.traits.NicknameTrait;
import net.aufdemrand.denizen.npc.traits.TriggerTrait;
import net.aufdemrand.denizen.scripts.commands.npc.EngageCommand;
import net.aufdemrand.denizen.scripts.containers.core.InteractScriptContainer;
import net.aufdemrand.denizen.scripts.containers.core.InteractScriptHelper;
import net.aufdemrand.denizen.scripts.triggers.AbstractTrigger;
import net.aufdemrand.denizen.tags.Attribute;
import net.aufdemrand.denizen.tags.core.NPCTags;
import net.aufdemrand.denizen.utilities.DenizenAPI;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.ai.Navigator;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.trait.trait.Owner;
import net.citizensnpcs.trait.Anchors;
import net.citizensnpcs.util.Anchor;
import net.minecraft.server.v1_5_R3.EntityLiving;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_5_R3.entity.CraftLivingEntity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class dNPC implements dObject {

    public static dNPC mirrorCitizensNPC(NPC npc) {
        if (dNPCRegistry.denizenNPCs.containsKey(npc.getId())) return dNPCRegistry.denizenNPCs.get(npc.getId());
        else return new dNPC(npc);
    }

    @ObjectFetcher("n")
    public static dNPC valueOf(String string) {
        if (string == null) return null;

        ////////
        // Match NPC id

        string = string.replace("n@", "");
        NPC npc;
        if (aH.matchesInteger(string)) {
            if (dNPCRegistry.denizenNPCs.containsKey(aH.getIntegerFrom(string)))
                return dNPCRegistry.denizenNPCs.get(aH.getIntegerFrom(string));

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



    private int npcid = -1;
    private boolean is_valid = true;
    private final org.bukkit.Location locationCache = new org.bukkit.Location(null, 0, 0, 0);

    public dNPC(NPC citizensNPC) {
        if (citizensNPC != null)
            this.npcid = citizensNPC.getId();
        if (npcid >= 0 && !dNPCRegistry.denizenNPCs.containsKey(npcid))
            dNPCRegistry.denizenNPCs.put(npcid, this);
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

        if (attribute == null) return "null";

        if (attribute.startsWith("name.nickname"))
            return new Element(getCitizen().hasTrait(NicknameTrait.class) ? getCitizen().getTrait(NicknameTrait.class)
                    .getNickname() : getName()).getAttribute(attribute.fulfill(2));

        if (attribute.startsWith("name"))
            return new Element(ChatColor.stripColor(getName()))
                    .getAttribute(attribute.fulfill(1));

        if (attribute.startsWith("anchor.list")
                || attribute.startsWith("anchors.list")) {
            List<String> list = new ArrayList<String>();
            for (Anchor anchor : getCitizen().getTrait(Anchors.class).getAnchors())
                list.add(anchor.getName());
            return new dList(list).getAttribute(attribute.fulfill(1));
        }

        if (attribute.startsWith("has_anchors")) {
            return (new Element(String.valueOf(getCitizen().getTrait(Anchors.class).getAnchors().size() > 0)))
                    .getAttribute(attribute.fulfill(1));
        }

        if (attribute.startsWith("anchor")) {
            if (attribute.hasContext(1)
                    && getCitizen().getTrait(Anchors.class).getAnchor(attribute.getContext(1)) != null)
                return new dLocation(getCitizen().getTrait(Anchors.class)
                        .getAnchor(attribute.getContext(1)).getLocation())
                .getAttribute(attribute.fulfill(1));
        }

        if (attribute.startsWith("flag")) {
            if (attribute.hasContext(1)) {
                if (FlagManager.npcHasFlag(this, attribute.getContext(1)))
                    return new dList(DenizenAPI.getCurrentInstance().flagManager()
                            .getNPCFlag(getId(), attribute.getContext(1)))
                            .getAttribute(attribute.fulfill(1));
                return "null";
            }
            else return null;
        }

        if (attribute.startsWith("id"))
            return new Element(String.valueOf(getId())).getAttribute(attribute.fulfill(1));

        if (attribute.startsWith("owner"))
            return new Element(getOwner()).getAttribute(attribute.fulfill(1));

        if (attribute.startsWith("is_spawned"))
            return new Element(String.valueOf(isSpawned())).getAttribute(attribute.fulfill(1));

        if (attribute.startsWith("location.previous_location"))
            return (NPCTags.previousLocations.containsKey(getId())
                    ? NPCTags.previousLocations.get(getId()).getAttribute(attribute.fulfill(2))
                    : "null");

        if (attribute.startsWith("navigator.is_navigating"))
            return new Element(String.valueOf(getNavigator().isNavigating())).getAttribute(attribute.fulfill(2));

        if (attribute.startsWith("navigator.speed"))
            return new Element(String.valueOf(getNavigator().getLocalParameters().speed()))
                    .getAttribute(attribute.fulfill(2));

        if (attribute.startsWith("navigator.attack_strategy"))
            return new Element(String.valueOf(getNavigator().getLocalParameters().attackStrategy().toString()))
                    .getAttribute(attribute.fulfill(2));

        if (attribute.startsWith("navigator.speed_modifier"))
            return new Element(String.valueOf(getNavigator().getLocalParameters().speedModifier()))
                    .getAttribute(attribute.fulfill(2));

        if (attribute.startsWith("navigator.base_speed"))
            return new Element(String.valueOf(getNavigator().getLocalParameters().baseSpeed()))
                    .getAttribute(attribute.fulfill(2));

        if (attribute.startsWith("navigator.avoid_water"))
            return new Element(String.valueOf(getNavigator().getLocalParameters().avoidWater()))
                    .getAttribute(attribute.fulfill(2));

        if (attribute.startsWith("navigator.target_location"))
            return (getNavigator().getTargetAsLocation() != null
                    ? new dLocation(getNavigator().getTargetAsLocation()).getAttribute(attribute.fulfill(2))
                    : "null");

        if (attribute.startsWith("navigator.is_fighting"))
            return new Element(String.valueOf(getNavigator().getEntityTarget().isAggressive()))
                    .getAttribute(attribute.fulfill(2));

        if (attribute.startsWith("navigator.target_type"))
            return new Element(String.valueOf(getNavigator().getTargetType().toString()))
                    .getAttribute(attribute.fulfill(2));

        if (attribute.startsWith("navigator.target_entity"))
            return (getNavigator().getEntityTarget().getTarget() != null
                    ? new dEntity(getNavigator().getEntityTarget().getTarget()).getAttribute(attribute.fulfill(2))
                    : "null");

        return (getEntity() != null
                ? new dEntity(getEntity()).getAttribute(attribute.fulfill(0))
                : new Element(identify()).getAttribute(attribute.fulfill(0)));

    }

}

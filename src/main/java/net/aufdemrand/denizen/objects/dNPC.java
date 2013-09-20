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
import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.api.trait.trait.Owner;
import net.citizensnpcs.trait.Anchors;
import net.citizensnpcs.util.Anchor;
import net.minecraft.server.v1_6_R2.EntityLiving;

import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_6_R2.entity.CraftLivingEntity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;

import java.util.ArrayList;
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

        string = string.toUpperCase().replace("N@", "");
        NPC npc;
        if (aH.matchesInteger(string)) {
            if (dNPCRegistry.denizenNPCs.containsKey(aH.getIntegerFrom(string)))
                return dNPCRegistry.denizenNPCs.get(aH.getIntegerFrom(string));

            npc = CitizensAPI.getNPCRegistry().getById(aH.getIntegerFrom(string));
            if (npc != null) return new dNPC(npc);
        }

        ////////
        // Match NPC name
        else {
            for (NPC test : CitizensAPI.getNPCRegistry()) {
                if (test.getName().equalsIgnoreCase(string)) {
                    return new dNPC(test);
                }
            }
        }

        return null;
    }


    public static boolean matches(String string) {
        string = string.toUpperCase().replace("N@", "");
        NPC npc;
        if (aH.matchesInteger(string)) {
            npc = CitizensAPI.getNPCRegistry().getById(aH.getIntegerFrom(string));
            if (npc != null) return true;
        }
        else {
            for (NPC test : CitizensAPI.getNPCRegistry()) {
                if (test.getName().equalsIgnoreCase(string)) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean isValid() {
        return getCitizen() != null;
    }

    private int npcid = -1;
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
            dB.log("Uh oh! Denizen has encountered a NPE while trying to fetch a NPC. Has this NPC been removed?");
        return npc;
    }

    public LivingEntity getEntity() {
        try {
            return getCitizen().getBukkitEntity();
        } catch (NullPointerException e) {
            dB.log("Uh oh! Denizen has encountered a NPE while trying to fetch a NPC entity. Has this NPC been removed?");
            return null;
        }
    }

    public dEntity getDenizenEntity() {
        try {
            return new dEntity(getCitizen().getBukkitEntity());
        } catch (NullPointerException e) {
            dB.log("Uh oh! Denizen has encountered a NPE while trying to fetch a NPC entity. Has this NPC been removed?");
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

    public void destroy() {
        getCitizen().destroy();
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

    public void action(String actionName, dPlayer player, Map<String, dObject> context) {
        if (getCitizen() != null)
        {
            if (getCitizen().hasTrait(AssignmentTrait.class))
                DenizenAPI.getCurrentInstance().getNPCRegistry()
                        .getActionHandler().doAction(
                        actionName,
                        this,
                        player,
                        getAssignmentTrait().getAssignment(),
                        context);
        }
    }

    public void action(String actionName, dPlayer player) {
        action(actionName, player, null);
    }

    private String prefix = "npc";

    @Override
    public String getPrefix() {
        return prefix;
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
    public String getObjectType() {
        return "NPC";
    }

    @Override
    public String identify() {
        return "n@" + npcid;
    }

    @Override
    public dNPC setPrefix(String prefix) {
        return this;
    }

    @Override
    public String getAttribute(Attribute attribute) {

        if (attribute == null) return "null";

        // <--[tag]
        // @attribute <npc.name.nickname>
        // @returns Element
        // @description
        // returns the NPC's nickname provided by the nickname trait, or null if the NPC does not have the nickname trait.
        // -->
        if (attribute.startsWith("name.nickname"))
            return new Element(getCitizen().hasTrait(NicknameTrait.class) ? getCitizen().getTrait(NicknameTrait.class)
                    .getNickname() : getName()).getAttribute(attribute.fulfill(2));

        // <--[tag]
        // @attribute <npc.name>
        // @returns Element
        // @description
        // returns the name of the NPC.
        // -->
        if (attribute.startsWith("name"))
            return new Element(ChatColor.stripColor(getName()))
                    .getAttribute(attribute.fulfill(1));

        // <--[tag]
        // @attribute <npc.list_traits>
        // @returns dList
        // @description
        // Returns a dList of all of the NPC's trait names.
        // -->
        if (attribute.startsWith("list_traits")) {
            List<String> list = new ArrayList<String>();
            for (Trait trait : getCitizen().getTraits())
                list.add(trait.getName());
            return new dList(list).getAttribute(attribute.fulfill(1));
        }

        // <--[tag]
        // @attribute <npc.has_trait[<trait>]>
        // @returns Element(boolean)
        // @description
        // Returns whether or not the NPC has a specified trait.
        // -->
        if (attribute.startsWith("has_trait")) {
            if (attribute.hasContext(1)) {
                Class<? extends Trait> trait = CitizensAPI.getTraitFactory().getTraitClass(attribute.getContext(1));
                if (trait != null)
                    return new Element(getCitizen().hasTrait(trait))
                            .getAttribute(attribute.fulfill(1));
            }
        }

        // <--[tag]
        // @attribute <npc.anchor.list>
        // @returns dList
        // @description
        // returns a dList of anchor names currently assigned to the NPC.
        // -->
        if (attribute.startsWith("anchor.list")
                || attribute.startsWith("anchors.list")) {
            List<String> list = new ArrayList<String>();
            for (Anchor anchor : getCitizen().getTrait(Anchors.class).getAnchors())
                list.add(anchor.getName());
            return new dList(list).getAttribute(attribute.fulfill(2));
        }

        // <--[tag]
        // @attribute <npc.has_anchors>
        // @returns Element(boolean)
        // @description
        // returns true if the NPC has anchors assigned, false otherwise.
        // -->
        if (attribute.startsWith("has_anchors")) {
            return (new Element(getCitizen().getTrait(Anchors.class).getAnchors().size() > 0))
                    .getAttribute(attribute.fulfill(1));
        }

        // <--[tag]
        // @attribute <npc.anchor[name]>
        // @returns dLocation
        // @description
        // returns a dLocation associated with the specified anchor, or 'null' if it doesn't exist.
        // -->
        if (attribute.startsWith("anchor")) {
            if (attribute.hasContext(1)
                    && getCitizen().getTrait(Anchors.class).getAnchor(attribute.getContext(1)) != null)
                return new dLocation(getCitizen().getTrait(Anchors.class)
                        .getAnchor(attribute.getContext(1)).getLocation())
                        .getAttribute(attribute.fulfill(1));
        }

        // <--[tag]
        // @attribute <npc.flag[flag_name]>
        // @returns Flag dList
        // @description
        // returns 'flag dList' of the NPC's flag_name specified.
        // -->
         if (attribute.startsWith("flag")) {
            String flag_name;
            if (attribute.hasContext(1)) flag_name = attribute.getContext(1);
            else return "null";
            attribute.fulfill(1);
            if (attribute.startsWith("is_expired")
                    || attribute.startsWith("isexpired"))
                return new Element(!FlagManager.npcHasFlag(this, flag_name))
                        .getAttribute(attribute.fulfill(1));
            if (attribute.startsWith("size") && !FlagManager.npcHasFlag(this, flag_name))
                return new Element(0).getAttribute(attribute.fulfill(1));
            if (FlagManager.npcHasFlag(this, flag_name))
                return new dList(DenizenAPI.getCurrentInstance().flagManager()
                        .getNPCFlag(getId(), flag_name))
                        .getAttribute(attribute);
            else return "null";
        }

        // <--[tag]
        // @attribute <npc.id>
        // @returns Element(number)
        // @description
        // returns the NPC's 'npcid' provided by Citizens.
        // -->
        if (attribute.startsWith("id"))
            return new Element(getId()).getAttribute(attribute.fulfill(1));

        // <--[tag]
        // @attribute <npc.owner>
        // @returns Element
        // @description
        // returns the owner of the NPC.
        // -->
        if (attribute.startsWith("owner"))
            return new Element(getOwner()).getAttribute(attribute.fulfill(1));

        // <--[tag]
        // @attribute <npc.inventory>
        // @returns dInventory
        // @description
        // Returns the dInventory of the NPC.
        // NOTE: This currently only works with player-type NPCs.
        // -->
        if (attribute.startsWith("inventory"))
            return new dInventory(getEntity()).getAttribute(attribute.fulfill(1));

        // <--[tag]
        // @attribute <npc.is_spawned>
        // @returns Element(boolean)
        // @description
        // returns 'true' if the NPC is spawned, otherwise 'false'.
        // -->
        if (attribute.startsWith("is_spawned"))
            return new Element(isSpawned()).getAttribute(attribute.fulfill(1));

        // <--[tag]
        // @attribute <npc.location.previous_location>
        // @returns dLocation
        // @description
        // returns the NPC's previous navigated location.
        // -->
        if (attribute.startsWith("location.previous_location"))
            return (NPCTags.previousLocations.containsKey(getId())
                    ? NPCTags.previousLocations.get(getId()).getAttribute(attribute.fulfill(2))
                    : "null");

        // <--[tag]
        // @attribute <npc.script>
        // @returns dScript
        // @description
        // returns the NPC's assigned script.
        // -->
        if (attribute.startsWith("script")) {
            NPC citizen = getCitizen();
            if (!citizen.hasTrait(AssignmentTrait.class) || !citizen.getTrait(AssignmentTrait.class).hasAssignment()) {
                return "null";
            }
            else {
                return new Element(citizen.getTrait(AssignmentTrait.class).getAssignment().getName())
                    .getAttribute(attribute.fulfill(1));
            }
        }

        // <--[tag]
        // @attribute <npc.navigator.is_navigating>
        // @returns Element(boolean)
        // @description
        // returns true if the NPC is currently navigating, false otherwise.
        // -->
        if (attribute.startsWith("navigator.is_navigating"))
            return new Element(getNavigator().isNavigating()).getAttribute(attribute.fulfill(2));

        // <--[tag]
        // @attribute <npc.navigator.speed>
        // @returns Element(number)
        // @description
        // returns the current speed of the NPC.
        // -->
        if (attribute.startsWith("navigator.speed"))
            return new Element(getNavigator().getLocalParameters().speed())
                    .getAttribute(attribute.fulfill(2));

        // <--[tag]
        // @attribute <npc.navigator.range>
        // @returns Element(number)
        // @description
        // returns the maximum pathfinding range
        // -->
        if (attribute.startsWith("navigator.range"))
            return new Element(getNavigator().getLocalParameters().range())
                    .getAttribute(attribute.fulfill(2));

        // <--[tag]
        // @attribute <npc.navigator.attack_strategy>
        // @returns Element
        // @description
        // returns the NPC's attack strategy
        // -->
        if (attribute.startsWith("navigator.attack_strategy"))
            return new Element(getNavigator().getLocalParameters().attackStrategy().toString())
                    .getAttribute(attribute.fulfill(2));

        // <--[tag]
        // @attribute <npc.navigator.speed_modifier>
        // @returns Element(number)
        // @description
        // returns the NPC movement speed modifier
        // -->
        if (attribute.startsWith("navigator.speed_modifier"))
            return new Element(getNavigator().getLocalParameters().speedModifier())
                    .getAttribute(attribute.fulfill(2));

        // <--[tag]
        // @attribute <npc.navigator.base_speed>
        // @returns Element(number)
        // @description
        // returns the base navigation speed
        // -->
        if (attribute.startsWith("navigator.base_speed"))
            return new Element(getNavigator().getLocalParameters().baseSpeed())
                    .getAttribute(attribute.fulfill(2));

        // <--[tag]
        // @attribute <npc.navigator.avoid_water>
        // @returns Element(boolean)
        // @description
        // returns whether the NPC will avoid water
        // -->
        if (attribute.startsWith("navigator.avoid_water"))
            return new Element(getNavigator().getLocalParameters().avoidWater())
                    .getAttribute(attribute.fulfill(2));

        // <--[tag]
        // @attribute <npc.navigator.target_location>
        // @returns dLocation
        // @description
        // returns the location the NPC is curently navigating towards
        // -->
        if (attribute.startsWith("navigator.target_location"))
            return (getNavigator().getTargetAsLocation() != null
                    ? new dLocation(getNavigator().getTargetAsLocation()).getAttribute(attribute.fulfill(2))
                    : "null");

        // <--[tag]
        // @attribute <npc.navigator.is_fighting>
        // @returns Element(boolean)
        // @description
        // returns whether the NPC is in combat
        // -->
        if (attribute.startsWith("navigator.is_fighting"))
            return new Element(getNavigator().getEntityTarget().isAggressive())
                    .getAttribute(attribute.fulfill(2));

        // <--[tag]
        // @attribute <npc.navigator.target_type>
        // @returns Element
        // @description
        // returns the entity type of the target
        // -->
        if (attribute.startsWith("navigator.target_type"))
            return new Element(getNavigator().getTargetType().toString())
                    .getAttribute(attribute.fulfill(2));

        // <--[tag]
        // @attribute <npc.navigator.target_entity>
        // @returns dEntity
        // @description
        // returns the entity being targeted
        // -->
        if (attribute.startsWith("navigator.target_entity"))
            return (getNavigator().getEntityTarget().getTarget() != null
                    ? new dEntity(getNavigator().getEntityTarget().getTarget()).getAttribute(attribute.fulfill(2))
                    : "null");

        return (getEntity() != null
                ? new dEntity(getEntity()).getAttribute(attribute)
                : new Element(identify()).getAttribute(attribute));

    }

}

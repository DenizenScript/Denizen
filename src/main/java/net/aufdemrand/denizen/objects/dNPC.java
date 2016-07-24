package net.aufdemrand.denizen.objects;

import net.aufdemrand.denizen.flags.FlagManager;
import net.aufdemrand.denizen.npc.dNPCRegistry;
import net.aufdemrand.denizen.npc.examiners.PathBlockExaminer;
import net.aufdemrand.denizen.npc.traits.*;
import net.aufdemrand.denizen.scripts.commands.npc.EngageCommand;
import net.aufdemrand.denizen.scripts.containers.core.InteractScriptContainer;
import net.aufdemrand.denizen.scripts.containers.core.InteractScriptHelper;
import net.aufdemrand.denizen.scripts.triggers.AbstractTrigger;
import net.aufdemrand.denizen.tags.core.NPCTags;
import net.aufdemrand.denizen.utilities.DenizenAPI;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.aufdemrand.denizencore.objects.*;
import net.aufdemrand.denizencore.objects.properties.Property;
import net.aufdemrand.denizencore.objects.properties.PropertyParser;
import net.aufdemrand.denizencore.tags.Attribute;
import net.aufdemrand.denizencore.tags.TagContext;
import net.aufdemrand.denizencore.utilities.CoreUtilities;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.ai.Navigator;
import net.citizensnpcs.api.ai.TeleportStuckAction;
import net.citizensnpcs.api.astar.pathfinder.FlyingBlockExaminer;
import net.citizensnpcs.api.astar.pathfinder.MinecraftBlockExaminer;
import net.citizensnpcs.api.event.DespawnReason;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.api.trait.trait.Equipment;
import net.citizensnpcs.api.trait.trait.Owner;
import net.citizensnpcs.trait.Anchors;
import net.citizensnpcs.trait.LookClose;
import net.citizensnpcs.trait.Poses;
import net.citizensnpcs.trait.waypoint.Waypoint;
import net.citizensnpcs.trait.waypoint.WaypointProvider;
import net.citizensnpcs.trait.waypoint.Waypoints;
import net.citizensnpcs.util.Anchor;
import net.citizensnpcs.util.Pose;
import net.minecraft.server.v1_10_R1.EntityLiving;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_10_R1.entity.CraftLivingEntity;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public class dNPC implements dObject, Adjustable, InventoryHolder {

    public static dNPC mirrorCitizensNPC(NPC npc) {
        if (dNPCRegistry._isRegistered(npc)) {
            return dNPCRegistry.getDenizen(npc);
        }
        else {
            return new dNPC(npc);
        }
    }

    public static dNPC fromEntity(Entity entity) {
        return mirrorCitizensNPC(CitizensAPI.getNPCRegistry().getNPC(entity));
    }


    public static dNPC valueOf(String string) {
        return valueOf(string, null);
    }

    @Fetchable("n")
    public static dNPC valueOf(String string, TagContext context) {
        if (string == null) {
            return null;
        }

        ////////
        // Match NPC id

        string = string.toUpperCase().replace("N@", "");
        NPC npc;
        if (aH.matchesInteger(string)) {
            int id = aH.getIntegerFrom(string);

            if (dNPCRegistry._isRegistered(id)) {
                return dNPCRegistry.getDenizen(id);
            }

            npc = CitizensAPI.getNPCRegistry().getById(id);
            if (npc != null) {
                return new dNPC(npc);
            }
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

        // If using object notation, assume it's valid
        if (CoreUtilities.toLowerCase(string).startsWith("n@")) {
            return true;
        }

        // Otherwise, let's do checks
        string = string.toUpperCase().replace("N@", "");
        NPC npc;
        if (aH.matchesInteger(string)) {
            npc = CitizensAPI.getNPCRegistry().getById(aH.getIntegerFrom(string));
            if (npc != null) {
                return true;
            }
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
        if (citizensNPC != null) {
            this.npcid = citizensNPC.getId();
        }
        if (npcid >= 0 && !dNPCRegistry._isRegistered(citizensNPC)) {
            dNPCRegistry._registerNPC(this);
        }
    }

    public EntityLiving getHandle() {
        return ((CraftLivingEntity) getEntity()).getHandle();
    }

    public NPC getCitizen() {
        NPC npc = CitizensAPI.getNPCRegistry().getById(npcid);
        if (npc == null) {
            //dB.echoError(new RuntimeException("StackTraceOutput"));
            //dB.log("Uh oh! Denizen has encountered a NPE while trying to fetch an NPC. " +
            //        "Has this NPC been removed?");
        }
        return npc;
    }

    public Entity getEntity() {
        try {
            return getCitizen().getEntity();
        }
        catch (NullPointerException e) {
            dB.log("Uh oh! Denizen has encountered a NPE while trying to fetch an NPC entity. " +
                    "Has this NPC been removed?");
            return null;
        }
    }

    public LivingEntity getLivingEntity() {
        try {
            if (getCitizen().getEntity() instanceof LivingEntity) {
                return (LivingEntity) getCitizen().getEntity();
            }
            else {
                dB.log("Uh oh! Tried to get the living entity of a non-living NPC!");
                return null;
            }
        }
        catch (NullPointerException e) {
            dB.log("Uh oh! Denizen has encountered a NPE while trying to fetch an NPC livingEntity. " +
                    "Has this NPC been removed?");
            return null;
        }
    }


    public dEntity getDenizenEntity() {
        try {
            return new dEntity(getCitizen().getEntity());
        }
        catch (NullPointerException e) {
            dB.log("Uh oh! Denizen has encountered a NPE while trying to fetch an NPC dEntity. " +
                    "Has this NPC been removed?");
            return null;
        }
    }

    @Override
    public Inventory getInventory() {
        return dNPCRegistry.getInventory(getCitizen());
    }

    public dInventory getDenizenInventory() {
        return new dInventory(this);
    }

    public EntityType getEntityType() {
        return getCitizen().getEntity().getType();
    }

    public Navigator getNavigator() {
        return getCitizen().getNavigator();
    }

    public int getId() {
        return npcid;
    }

    public String getName() {
        return getCitizen().getName();
    }

    public InteractScriptContainer getInteractScript(dPlayer player, Class<? extends AbstractTrigger> triggerType) {
        return InteractScriptHelper.getInteractScript(this, player, triggerType);
    }

    public InteractScriptContainer getInteractScriptQuietly(dPlayer player, Class<? extends AbstractTrigger> triggerType) {
        boolean db = dB.showDebug;
        dB.showDebug = false;
        InteractScriptContainer script = InteractScriptHelper.getInteractScript(this, player, triggerType);
        dB.showDebug = db;
        return script;
    }

    public void destroy() {
        getCitizen().destroy();
    }

    public dLocation getLocation() {
        if (isSpawned()) {
            return new dLocation(getEntity().getLocation());
        }
        else {
            return new dLocation(getCitizen().getStoredLocation());
        }
    }

    public dLocation getEyeLocation() {
        if (isSpawned() && getCitizen().getEntity() instanceof LivingEntity) {
            return new dLocation(((LivingEntity) getCitizen().getEntity()).getEyeLocation());
        }
        else if (isSpawned()) {
            return new dLocation(getEntity().getLocation());
        }
        else {
            return new dLocation(getCitizen().getStoredLocation());
        }
    }

    public World getWorld() {
        if (isSpawned()) {
            return getEntity().getWorld();
        }
        else {
            return null;
        }
    }

    @Override
    public String toString() {
        return getCitizen().getName() + '/' + getCitizen().getId();
    }

    public boolean isEngaged() {
        return EngageCommand.getEngaged(getCitizen());
    }

    public boolean isSpawned() {
        NPC npc = CitizensAPI.getNPCRegistry().getById(npcid);
        return npc != null && npc.isSpawned();
    }

    public String getOwner() {
        if (getCitizen().getTrait(Owner.class).getOwnerId() == null) {
            return getCitizen().getTrait(Owner.class).getOwner();
        }
        return getCitizen().getTrait(Owner.class).getOwnerId().toString();
    }

    public AssignmentTrait getAssignmentTrait() {
        NPC npc = getCitizen();
        if (!npc.hasTrait(AssignmentTrait.class)) {
            npc.addTrait(AssignmentTrait.class);
        }
        return npc.getTrait(AssignmentTrait.class);
    }

    public Equipment getEquipmentTrait() {
        NPC npc = getCitizen();
        if (!npc.hasTrait(Equipment.class)) {
            npc.addTrait(Equipment.class);
        }
        return npc.getTrait(Equipment.class);
    }

    public NicknameTrait getNicknameTrait() {
        NPC npc = getCitizen();
        if (!npc.hasTrait(NicknameTrait.class)) {
            npc.addTrait(NicknameTrait.class);
        }
        return npc.getTrait(NicknameTrait.class);
    }

    public FishingTrait getFishingTrait() {
        NPC npc = getCitizen();
        if (!npc.hasTrait(FishingTrait.class)) {
            npc.addTrait(FishingTrait.class);
        }
        return npc.getTrait(FishingTrait.class);
    }

    public HealthTrait getHealthTrait() {
        NPC npc = getCitizen();
        if (!npc.hasTrait(HealthTrait.class)) {
            npc.addTrait(HealthTrait.class);
        }
        return npc.getTrait(HealthTrait.class);
    }

    public net.citizensnpcs.api.trait.trait.Inventory getInventoryTrait() {
        NPC npc = getCitizen();
        if (!npc.hasTrait(net.citizensnpcs.api.trait.trait.Inventory.class)) {
            npc.addTrait(net.citizensnpcs.api.trait.trait.Inventory.class);
        }
        return npc.getTrait(net.citizensnpcs.api.trait.trait.Inventory.class);
    }

    public PushableTrait getPushableTrait() {
        NPC npc = getCitizen();
        if (!npc.hasTrait(PushableTrait.class)) {
            npc.addTrait(PushableTrait.class);
        }
        return npc.getTrait(PushableTrait.class);
    }

    public LookClose getLookCloseTrait() {
        NPC npc = getCitizen();
        if (!npc.hasTrait(LookClose.class)) {
            npc.addTrait(LookClose.class);
        }
        return npc.getTrait(LookClose.class);
    }

    public TriggerTrait getTriggerTrait() {
        NPC npc = getCitizen();
        if (!npc.hasTrait(TriggerTrait.class)) {
            npc.addTrait(TriggerTrait.class);
        }
        return npc.getTrait(TriggerTrait.class);
    }

    public String action(String actionName, dPlayer player, Map<String, dObject> context) {
        if (getCitizen() != null) {
            if (getCitizen().hasTrait(AssignmentTrait.class))
            // Return the result from the ActionHandler
            {
                return DenizenAPI.getCurrentInstance().getNPCRegistry()
                        .getActionHandler().doAction(
                                actionName,
                                this,
                                player,
                                getAssignmentTrait().getAssignment(),
                                context);
            }
        }

        return "none";
    }

    public String action(String actionName, dPlayer player) {
        return action(actionName, player, null);
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
    public String identifySimple() {
        return identify();
    }

    @Override
    public dNPC setPrefix(String prefix) {
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null) {
            return false;
        }
        if (!(o instanceof dNPC)) {
            return false;
        }
        return getId() == ((dNPC) o).getId();
    }

    @Override
    public int hashCode() {
        return getId();
    }

    @Override
    public String getAttribute(Attribute attribute) {

        if (attribute == null) {
            return null;
        }

        // Defined in dEntity
        if (attribute.startsWith("is_npc")) {
            return Element.TRUE.getAttribute(attribute.fulfill(1));
        }

        // Defined in dEntity
        if (attribute.startsWith("location") && !isSpawned()) {
            return getLocation().getAttribute(attribute.fulfill(1));
        }


        // Defined in dEntity
        if (attribute.startsWith("eye_location")) {
            return getEyeLocation().getAttribute(attribute.fulfill(1));
        }

        // <--[tag]
        // @attribute <n@npc.has_nickname>
        // @returns Element(Boolean)
        // @description
        // Returns true if the NPC has a nickname.
        // -->
        if (attribute.startsWith("has_nickname")) {
            NPC citizen = getCitizen();
            return new Element(citizen.hasTrait(NicknameTrait.class) && citizen.getTrait(NicknameTrait.class).hasNickname())
                    .getAttribute(attribute.fulfill(1));
        }

        // <--[tag]
        // @attribute <n@npc.name.nickname>
        // @returns Element
        // @description
        // Returns the NPC's display name.
        // -->
        if (attribute.startsWith("name.nickname")) {
            return new Element(getCitizen().hasTrait(NicknameTrait.class) ? getCitizen().getTrait(NicknameTrait.class)
                    .getNickname() : getName()).getAttribute(attribute.fulfill(2));
        }

        // <--[tag]
        // @attribute <n@npc.name>
        // @returns Element
        // @description
        // Returns the name of the NPC.
        // -->
        if (attribute.startsWith("name")) {
            return new Element(getName())
                    .getAttribute(attribute.fulfill(1));
        }

        // <--[tag]
        // @attribute <n@npc.list_traits>
        // @returns dList
        // @description
        // Returns a list of all of the NPC's traits.
        // -->
        if (attribute.startsWith("list_traits")) {
            List<String> list = new ArrayList<String>();
            for (Trait trait : getCitizen().getTraits()) {
                list.add(trait.getName());
            }
            return new dList(list).getAttribute(attribute.fulfill(1));
        }

        // <--[tag]
        // @attribute <n@npc.has_trait[<trait>]>
        // @returns Element(Boolean)
        // @description
        // Returns whether the NPC has a specified trait.
        // -->
        if (attribute.startsWith("has_trait")) {
            if (attribute.hasContext(1)) {
                Class<? extends Trait> trait = CitizensAPI.getTraitFactory().getTraitClass(attribute.getContext(1));
                if (trait != null) {
                    return new Element(getCitizen().hasTrait(trait))
                            .getAttribute(attribute.fulfill(1));
                }
            }
        }

        // <--[tag]
        // @attribute <n@npc.pushable>
        // @returns Element(Boolean)
        // @description
        // Returns whether the NPC is pushable.
        // -->
        if (attribute.startsWith("pushable") || attribute.startsWith("is_pushable")) {
            return new Element(getPushableTrait().isPushable()).getAttribute(attribute.fulfill(1));
        }

        // <--[tag]
        // @attribute <n@npc.has_trigger[<trigger>]>
        // @returns Element(Boolean)
        // @description
        // Returns whether the NPC has a specified trigger.
        // -->
        if (attribute.startsWith("has_trigger")
                && attribute.hasContext(1)) {
            if (!getCitizen().hasTrait(TriggerTrait.class)) {
                return Element.FALSE.getAttribute(attribute.fulfill(1));
            }
            TriggerTrait trait = getCitizen().getTrait(TriggerTrait.class);
            return new Element(trait.hasTrigger(attribute.getContext(1)))
                    .getAttribute(attribute.fulfill(1));
        }

        // <--[tag]
        // @attribute <n@npc.anchor.list>
        // @returns dList
        // @description
        // Returns a list of anchor names currently assigned to the NPC.
        // -->
        if (attribute.startsWith("anchor.list")
                || attribute.startsWith("anchors.list")) {
            List<String> list = new ArrayList<String>();
            for (Anchor anchor : getCitizen().getTrait(Anchors.class).getAnchors()) {
                list.add(anchor.getName());
            }
            return new dList(list).getAttribute(attribute.fulfill(2));
        }

        // <--[tag]
        // @attribute <n@npc.has_anchors>
        // @returns Element(Boolean)
        // @description
        // Returns whether the NPC has anchors assigned.
        // -->
        if (attribute.startsWith("has_anchors")) {
            return (new Element(getCitizen().getTrait(Anchors.class).getAnchors().size() > 0))
                    .getAttribute(attribute.fulfill(1));
        }

        // <--[tag]
        // @attribute <n@npc.anchor[<name>]>
        // @returns dLocation
        // @description
        // Returns the location associated with the specified anchor, or null if it doesn't exist.
        // -->
        if (attribute.startsWith("anchor")) {
            if (attribute.hasContext(1)
                    && getCitizen().getTrait(Anchors.class).getAnchor(attribute.getContext(1)) != null) {
                return new dLocation(getCitizen().getTrait(Anchors.class)
                        .getAnchor(attribute.getContext(1)).getLocation())
                        .getAttribute(attribute.fulfill(1));
            }
        }

        // <--[tag]
        // @attribute <n@npc.has_flag[<flag_name>]>
        // @returns Element(Boolean)
        // @description
        // Returns true if the NPC has the specified flag, otherwise returns false.
        // -->
        if (attribute.startsWith("has_flag")) {
            String flag_name;
            if (attribute.hasContext(1)) {
                flag_name = attribute.getContext(1);
            }
            else {
                return null;
            }
            return new Element(FlagManager.npcHasFlag(this, flag_name)).getAttribute(attribute.fulfill(1));
        }

        // <--[tag]
        // @attribute <n@npc.flag[<flag_name>]>
        // @returns Flag dList
        // @description
        // Returns the specified flag from the NPC.
        // -->
        if (attribute.startsWith("flag")) {
            String flag_name;
            if (attribute.hasContext(1)) {
                flag_name = attribute.getContext(1);
            }
            else {
                return null;
            }
            if (attribute.getAttribute(2).equalsIgnoreCase("is_expired")
                    || attribute.startsWith("isexpired")) {
                return new Element(!FlagManager.npcHasFlag(this, flag_name))
                        .getAttribute(attribute.fulfill(2));
            }
            if (attribute.getAttribute(2).equalsIgnoreCase("size") && !FlagManager.npcHasFlag(this, flag_name)) {
                return new Element(0).getAttribute(attribute.fulfill(2));
            }
            if (FlagManager.npcHasFlag(this, flag_name)) {
                FlagManager.Flag flag = DenizenAPI.getCurrentInstance().flagManager()
                        .getNPCFlag(getId(), flag_name);
                return new dList(flag.toString(), true, flag.values())
                        .getAttribute(attribute.fulfill(1));
            }
            return new Element(identify()).getAttribute(attribute);
        }

        // <--[tag]
        // @attribute <n@npc.list_flags[(regex:)<search>]>
        // @returns dList
        // @description
        // Returns a list of an NPC's flag names, with an optional search for
        // names containing a certain pattern.
        // -->
        if (attribute.startsWith("list_flags")) {
            dList allFlags = new dList(DenizenAPI.getCurrentInstance().flagManager().listNPCFlags(getId()));
            dList searchFlags = null;
            if (!allFlags.isEmpty() && attribute.hasContext(1)) {
                searchFlags = new dList();
                String search = attribute.getContext(1);
                if (search.startsWith("regex:")) {
                    try {
                        Pattern pattern = Pattern.compile(search.substring(6), Pattern.CASE_INSENSITIVE);
                        for (String flag : allFlags) {
                            if (pattern.matcher(flag).matches()) {
                                searchFlags.add(flag);
                            }
                        }
                    }
                    catch (Exception e) {
                        dB.echoError(e);
                    }
                }
                else {
                    search = CoreUtilities.toLowerCase(search);
                    for (String flag : allFlags) {
                        if (CoreUtilities.toLowerCase(flag).contains(search)) {
                            searchFlags.add(flag);
                        }
                    }
                }
            }
            return searchFlags == null ? allFlags.getAttribute(attribute.fulfill(1))
                    : searchFlags.getAttribute(attribute.fulfill(1));
        }

        // <--[tag]
        // @attribute <n@npc.constant[<constant_name>]>
        // @returns Element
        // @description
        // Returns the specified constant from the NPC.
        // -->
        if (attribute.startsWith("constant")) {
            if (attribute.hasContext(1)) {
                if (getCitizen().hasTrait(ConstantsTrait.class)
                        && getCitizen().getTrait(ConstantsTrait.class).getConstant(attribute.getContext(1)) != null) {
                    return new Element(getCitizen().getTrait(ConstantsTrait.class)
                            .getConstant(attribute.getContext(1))).getAttribute(attribute.fulfill(1));
                }
                else {
                    return null;
                }
            }
        }

        // <--[tag]
        // @attribute <n@npc.has_pose[<name>]>
        // @returns Element(Boolean)
        // @description
        // Returns true if the NPC has the specified pose, otherwise returns false.
        // -->
        if (attribute.startsWith("has_pose")) {
            if (attribute.hasContext(1)) {
                return new Element(getCitizen().getTrait(Poses.class).hasPose(attribute.getContext(1)))
                        .getAttribute(attribute.fulfill(1));
            }
            else {
                return null;
            }
        }

        // <--[tag]
        // @attribute <n@npc.pose[<name>]>
        // @returns dLocation
        // @description
        // Returns the pose as a dLocation with x, y, and z set to 0, and the world set to the first
        // possible available world Bukkit knows about.
        // -->
        if (attribute.startsWith("pose") || attribute.startsWith("get_pose")) {
            if (attribute.hasContext(1)) {
                Pose pose = getCitizen().getTrait(Poses.class).getPose(attribute.getContext(1));
                return new dLocation(org.bukkit.Bukkit.getWorlds().get(0), 0, 0, 0, pose.getYaw(), pose.getPitch())
                        .getAttribute(attribute.fulfill(1));
            }
            else {
                return null;
            }
        }

        // <--[tag]
        // @attribute <n@npc.is_engaged>
        // @returns Element(Boolean)
        // @description
        // Returns whether the NPC is currently engaged.
        // See <@link command engage>
        // -->
        if (attribute.startsWith("engaged") || attribute.startsWith("is_engaged")) {
            return new Element(isEngaged()).getAttribute(attribute.fulfill(1));
        }

        // <--[tag]
        // @attribute <n@npc.invulnerable>
        // @returns Element(Boolean)
        // @description
        // Returns whether the NPC is currently invulnerable.
        // See <@link command vulnerable>
        // -->
        if (attribute.startsWith("invulnerable") || attribute.startsWith("vulnerable")) {
            return new Element(getCitizen().data().get(NPC.DEFAULT_PROTECTED_METADATA, true)).getAttribute(attribute.fulfill(1));
        }

        // <--[tag]
        // @attribute <n@npc.id>
        // @returns Element(Number)
        // @description
        // Returns the NPC's ID number.
        // -->
        if (attribute.startsWith("id")) {
            return new Element(getId()).getAttribute(attribute.fulfill(1));
        }

        // <--[tag]
        // @attribute <n@npc.owner>
        // @returns dPlayer/Element
        // @description
        // Returns the owner of the NPC as a dPlayer if it's a player, otherwise as just the name.
        // -->
        if (attribute.startsWith("owner")) {
            String owner = getOwner();
            dPlayer player = null;
            if (!owner.equalsIgnoreCase("server")) {
                player = dPlayer.valueOfInternal(owner, false);
            }
            if (player != null) {
                return player.getAttribute(attribute.fulfill(1));
            }
            else {
                return new Element(owner).getAttribute(attribute.fulfill(1));
            }
        }

        // <--[tag]
        // @attribute <n@npc.has_skin>
        // @returns Element
        // @description
        // Returns whether the NPC has a custom skinskin.
        // -->
        if (attribute.startsWith("has_skin")) {
            return new Element(getCitizen().data().has(NPC.PLAYER_SKIN_UUID_METADATA)).getAttribute(attribute.fulfill(1));
        }

        // <--[tag]
        // @attribute <n@npc.skin_blob>
        // @returns Element
        // @description
        // Returns the NPC's custom skin blob, if any.
        // -->
        if (attribute.startsWith("skin_blob")) {
            if (getCitizen().data().has(NPC.PLAYER_SKIN_TEXTURE_PROPERTIES_METADATA)) {
                return new Element(getCitizen().data().get(NPC.PLAYER_SKIN_TEXTURE_PROPERTIES_METADATA).toString()).getAttribute(attribute.fulfill(1));
            }
        }

        // <--[tag]
        // @attribute <n@npc.skin>
        // @returns Element
        // @description
        // Returns the NPC's custom skin, if any.
        // -->
        if (attribute.startsWith("skin")) {
            if (getCitizen().data().has(NPC.PLAYER_SKIN_UUID_METADATA)) {
                return new Element(getCitizen().data().get(NPC.PLAYER_SKIN_UUID_METADATA).toString()).getAttribute(attribute.fulfill(1));
            }
        }

        // <--[tag]
        // @attribute <n@npc.inventory>
        // @returns dInventory
        // @description
        // Returns the dInventory of the NPC.
        // -->
        if (attribute.startsWith("inventory")) {
            return getDenizenInventory().getAttribute(attribute.fulfill(1));
        }

        // <--[tag]
        // @attribute <n@npc.is_spawned>
        // @returns Element(Boolean)
        // @description
        // Returns whether the NPC is spawned.
        // -->
        if (attribute.startsWith("is_spawned")) {
            return new Element(isSpawned()).getAttribute(attribute.fulfill(1));
        }

        // <--[tag]
        // @attribute <n@npc.is_protected>
        // @returns Element(Boolean)
        // @description
        // Returns whether the NPC is protected.
        // -->
        if (attribute.startsWith("is_protected")) {
            return new Element(getCitizen().isProtected()).getAttribute(attribute.fulfill(1));
        }

        // <--[tag]
        // @attribute <n@npc.lookclose>
        // @returns Element(Boolean)
        // @description
        // Returns the NPC's "lookclose" value.
        // -->
        if (attribute.startsWith("lookclose")) {
            NPC citizen = getCitizen();
            if (citizen.hasTrait(LookClose.class)) {
                // There is no method to check if the NPC has LookClose enabled...
                // LookClose.toString() returns "LookClose{" + enabled + "}"
                String lookclose = citizen.getTrait(LookClose.class).toString();
                lookclose = lookclose.substring(10, lookclose.length() - 1);
                return new Element(Boolean.valueOf(lookclose)).getAttribute(attribute.fulfill(1));
            }
            return Element.FALSE.getAttribute(attribute.fulfill(1));
        }

        // <--[tag]
        // @attribute <n@npc.location.previous_location>
        // @returns dLocation
        // @description
        // Returns the NPC's previous navigated location.
        // -->
        if (attribute.startsWith("location.previous_location")) {
            return (NPCTags.previousLocations.containsKey(getId())
                    ? NPCTags.previousLocations.get(getId()).getAttribute(attribute.fulfill(2))
                    : null);
        }

        // <--[tag]
        // @attribute <n@npc.teleport_on_stuck>
        // @returns dLocation
        // @mechanism dNPC.teleport_on_stuck
        // @description
        // Returns whether the NPC teleports when it is stuck.
        // -->
        if (attribute.startsWith("teleport_on_stuck")) {
            return new Element(getNavigator().getDefaultParameters().stuckAction() == TeleportStuckAction.INSTANCE)
                    .getAttribute(attribute.fulfill(1));
        }

        // <--[tag]
        // @attribute <n@npc.has_script>
        // @returns Element(Boolean)
        // @description
        // Returns true if the NPC has an assignment script.
        // -->
        if (attribute.startsWith("has_script")) {
            NPC citizen = getCitizen();
            return new Element(citizen.hasTrait(AssignmentTrait.class) && citizen.getTrait(AssignmentTrait.class).hasAssignment())
                    .getAttribute(attribute.fulfill(1));
        }

        // <--[tag]
        // @attribute <n@npc.script>
        // @returns dScript
        // @description
        // Returns the NPC's assigned script.
        // -->
        if (attribute.startsWith("script")) {
            NPC citizen = getCitizen();
            if (!citizen.hasTrait(AssignmentTrait.class) || !citizen.getTrait(AssignmentTrait.class).hasAssignment()) {
                return null;
            }
            else {
                return new dScript(citizen.getTrait(AssignmentTrait.class).getAssignment().getName())
                        .getAttribute(attribute.fulfill(1));
            }
        }

        // <--[tag]
        // @attribute <n@npc.navigator.is_navigating>
        // @returns Element(Boolean)
        // @description
        // Returns whether the NPC is currently navigating.
        // -->
        if (attribute.startsWith("navigator.is_navigating")) {
            return new Element(getNavigator().isNavigating()).getAttribute(attribute.fulfill(2));
        }

        // <--[tag]
        // @attribute <n@npc.navigator.speed>
        // @returns Element(Number)
        // @description
        // Returns the current speed of the NPC.
        // -->
        if (attribute.startsWith("navigator.speed")) {
            return new Element(getNavigator().getLocalParameters().speed())
                    .getAttribute(attribute.fulfill(2));
        }

        // <--[tag]
        // @attribute <n@npc.navigator.range>
        // @returns Element(Number)
        // @description
        // Returns the maximum pathfinding range.
        // -->
        if (attribute.startsWith("navigator.range")) {
            return new Element(getNavigator().getLocalParameters().range())
                    .getAttribute(attribute.fulfill(2));
        }

        // <--[tag]
        // @attribute <n@npc.navigator.attack_range>
        // @returns Element(Number)
        // @description
        // Returns the maximum attack range.
        // -->
        if (attribute.startsWith("navigator.attack_range")) {
            return new Element(getNavigator().getLocalParameters().attackRange())
                    .getAttribute(attribute.fulfill(2));
        }

        // <--[tag]
        // @attribute <n@npc.navigator.attack_strategy>
        // @returns Element
        // @description
        // Returns the NPC's attack strategy.
        // -->
        if (attribute.startsWith("navigator.attack_strategy")) {
            return new Element(getNavigator().getLocalParameters().attackStrategy().toString())
                    .getAttribute(attribute.fulfill(2));
        }

        // <--[tag]
        // @attribute <n@npc.navigator.speed_modifier>
        // @returns Element(Number)
        // @description
        // Returns the NPC movement speed modifier.
        // -->
        if (attribute.startsWith("navigator.speed_modifier")) {
            return new Element(getNavigator().getLocalParameters().speedModifier())
                    .getAttribute(attribute.fulfill(2));
        }

        // <--[tag]
        // @attribute <n@npc.navigator.base_speed>
        // @returns Element(Number)
        // @description
        // Returns the base navigation speed.
        // -->
        if (attribute.startsWith("navigator.base_speed")) {
            return new Element(getNavigator().getLocalParameters().baseSpeed())
                    .getAttribute(attribute.fulfill(2));
        }

        // <--[tag]
        // @attribute <n@npc.navigator.avoid_water>
        // @returns Element(Boolean)
        // @description
        // Returns whether the NPC will avoid water.
        // -->
        if (attribute.startsWith("navigator.avoid_water")) {
            return new Element(getNavigator().getLocalParameters().avoidWater())
                    .getAttribute(attribute.fulfill(2));
        }

        // <--[tag]
        // @attribute <n@npc.navigator.target_location>
        // @returns dLocation
        // @description
        // Returns the location the NPC is curently navigating towards.
        // -->
        if (attribute.startsWith("navigator.target_location")) {
            return (getNavigator().getTargetAsLocation() != null
                    ? new dLocation(getNavigator().getTargetAsLocation()).getAttribute(attribute.fulfill(2))
                    : null);
        }

        // <--[tag]
        // @attribute <n@npc.navigator.is_fighting>
        // @returns Element(Boolean)
        // @description
        // Returns whether the NPC is in combat.
        // -->
        if (attribute.startsWith("navigator.is_fighting")) {
            return new Element(getNavigator().getEntityTarget() != null && getNavigator().getEntityTarget().isAggressive())
                    .getAttribute(attribute.fulfill(2));
        }

        // <--[tag]
        // @attribute <n@npc.navigator.target_type>
        // @returns Element
        // @description
        // Returns the entity type of the target.
        // -->
        if (attribute.startsWith("navigator.target_type"))
        // TODO: IMPROVE
        {
            return new Element(getNavigator().getTargetType() == null ? "null"
                    : getNavigator().getTargetType().toString())
                    .getAttribute(attribute.fulfill(2));
        }

        // <--[tag]
        // @attribute <n@npc.navigator.target_entity>
        // @returns dEntity
        // @description
        // Returns the entity being targeted.
        // -->
        if (attribute.startsWith("navigator.target_entity")) {
            return (getNavigator().getEntityTarget() != null && getNavigator().getEntityTarget().getTarget() != null
                    ? new dEntity(getNavigator().getEntityTarget().getTarget()).getAttribute(attribute.fulfill(2))
                    : null);
        }

        // <--[tag]
        // @attribute <n@npc.type>
        // @returns Element
        // @description
        // Always returns 'NPC' for dNPC objects. All objects fetchable by the Object Fetcher will return the
        // type of object that is fulfilling this attribute.
        // -->
        if (attribute.startsWith("type")) {
            return new Element("NPC").getAttribute(attribute.fulfill(1));
        }

        // Iterate through this object's properties' attributes
        for (Property property : PropertyParser.getProperties(this)) {
            String returned = property.getAttribute(attribute);
            if (returned != null) {
                return returned;
            }
        }

        return (getEntity() != null
                ? new dEntity(this).getAttribute(attribute)
                : new Element(identify()).getAttribute(attribute));

    }

    public void applyProperty(Mechanism mechanism) {
        dB.echoError("Cannot apply properties to an NPC!");
    }

    @Override
    public void adjust(Mechanism mechanism) {

        Element value = mechanism.getValue();

        // TODO: For all the mechanism tags, add the @Mechanism link!

        // <--[mechanism]
        // @object dNPC
        // @name set_assignment
        // @input dScript
        // @description
        // Sets the NPC's assignment script.
        // @tags
        // <n@npc.script>
        // -->
        if (mechanism.matches("set_assignment") && mechanism.requireObject(dScript.class)) {
            getAssignmentTrait().setAssignment(value.asType(dScript.class).getName(), null);
        }

        // <--[mechanism]
        // @object dNPC
        // @name remove_assignment
        // @input none
        // @description
        // Removes the NPC's assigment script.
        // @tags
        // <n@npc.has_script>
        // -->
        if (mechanism.matches("remove_assignment")) {
            getAssignmentTrait().removeAssignment(null);
        }

        // <--[mechanism]
        // @object dNPC
        // @name set_nickname
        // @input Element
        // @description
        // Sets the NPC's nickname.
        // @tags
        // <n@npc.name.nickname>
        // -->
        if (mechanism.matches("set_nickname")) {
            getNicknameTrait().setNickname(value.asString());
        }

        // <--[mechanism]
        // @object dNPC
        // @name remove_nickname
        // @input none
        // @description
        // Removes the NPC's nickname.
        // @tags
        // <n@npc.has_nickname>
        // -->
        if (mechanism.matches("remove_nickname")) {
            getNicknameTrait().removeNickname();
        }

        // <--[mechanism]
        // @object dNPC
        // @name set_entity_type
        // @input dEntity
        // @description
        // Sets the NPC's entity type.
        // @tags
        // <n@npc.entity_type>
        // -->
        if (mechanism.matches("set_entity_type") && mechanism.requireObject(dEntity.class)) {
            getCitizen().setBukkitEntityType(value.asType(dEntity.class).getBukkitEntityType());
        }

        // <--[mechanism]
        // @object dNPC
        // @name name
        // @input Element
        // @description
        // Sets the name of the NPC.
        // @tags
        // <n@npc.name>
        // -->
        if (mechanism.matches("name") || mechanism.matches("set_name")) {
            getCitizen().setName(value.asString().length() > 64 ? value.asString().substring(0, 64) : value.asString());
        }

        // <--[mechanism]
        // @object dNPC
        // @name owner
        // @input Element
        // @description
        // Sets the owner of the NPC.
        // @tags
        // <n@npc.owner>
        // -->
        if (mechanism.matches("owner")) {
            getCitizen().getTrait(Owner.class).setOwner(value.asString());
        }

        // <--[mechanism]
        // @object dNPC
        // @name skin_blob
        // @input Element
        // @description
        // Sets the skin blob of an NPC.
        // @tags
        // <n@npc.skin>
        // -->
        if (mechanism.matches("skin_blob")) {
            if (!mechanism.hasValue()) {
                getCitizen().data().remove(NPC.PLAYER_SKIN_TEXTURE_PROPERTIES_METADATA);
            }
            else {
                getCitizen().data().setPersistent(NPC.PLAYER_SKIN_TEXTURE_PROPERTIES_METADATA, mechanism.getValue().asString());
            }
            if (getCitizen().isSpawned()) {
                getCitizen().despawn(DespawnReason.PENDING_RESPAWN);
                getCitizen().spawn(getCitizen().getStoredLocation());
            }
        }

        // <--[mechanism]
        // @object dNPC
        // @name skin
        // @input Element
        // @description
        // Sets the skin of an NPC.
        // @tags
        // <n@npc.skin>
        // -->
        if (mechanism.matches("skin")) {
            if (!mechanism.hasValue()) {
                getCitizen().data().remove(NPC.PLAYER_SKIN_UUID_METADATA);
            }
            else {
                getCitizen().data().setPersistent(NPC.PLAYER_SKIN_UUID_METADATA, mechanism.getValue().asString());
            }
            if (getCitizen().isSpawned()) {
                getCitizen().despawn(DespawnReason.PENDING_RESPAWN);
                getCitizen().spawn(getCitizen().getStoredLocation());
            }
        }

        // <--[mechanism]
        // @object dNPC
        // @name item_type
        // @input dItem
        // @description
        // Sets the item type of the item.
        // @tags
        // None
        // -->
        if (mechanism.matches("item_type") && mechanism.requireObject(dItem.class)) {
            dItem item = mechanism.getValue().asType(dItem.class);
            Material mat = item.getMaterial().getMaterial();
            int data = item.getMaterial().getData((byte) 0);
            switch (getEntity().getType()) {
                case DROPPED_ITEM:
                    ((org.bukkit.entity.Item) getEntity()).getItemStack().setType(mat);
                    //((ItemController.ItemNPC) getEntity()).setType(mat, data);
                    break;
                case ITEM_FRAME:
                    ((ItemFrame) getEntity()).getItem().setType(mat);
                    //((ItemFrameController.ItemFrameNPC) getEntity()).setType(mat, data);
                    break;
                case FALLING_BLOCK:
                    //((FallingBlockController.FallingBlockNPC) getEntity()).setType(mat, data);
                    break;
                default:
                    dB.echoError("NPC is the not an item type!");
                    break;
            }
            if (getCitizen().isSpawned()) {
                getCitizen().despawn();
                getCitizen().spawn(getCitizen().getStoredLocation());
            }
        }

        // <--[mechanism]
        // @object dNPC
        // @name spawn
        // @input dLocation
        // @description
        // Spawns the NPC at a location. If no location is specified, the NPC will spawn
        // at its last known location.
        // @tags
        // <n@npc.is_spawned>
        // -->
        if (mechanism.matches("spawn")) {
            if (mechanism.requireObject("Invalid dLocation specified. Assuming last known NPC location.", dLocation.class)) {
                getCitizen().spawn(value.asType(dLocation.class));
            }
            else {
                getCitizen().spawn(getCitizen().getStoredLocation());
            }
        }

        // <--[mechanism]
        // @object dNPC
        // @name range
        // @input Element(Decimal)
        // @description
        // Sets the maximum movement distance of the NPC.
        // @tags
        // <n@npc.navigator.range>
        // -->
        if (mechanism.matches("range") && mechanism.requireFloat()) {
            getCitizen().getNavigator().getDefaultParameters().range(mechanism.getValue().asFloat());
        }

        // <--[mechanism]
        // @object dNPC
        // @name attack_range
        // @input Element(Decimal)
        // @description
        // Sets the maximum attack distance of the NPC.
        // @tags
        // <n@npc.navigator.attack_range>
        // -->
        if (mechanism.matches("attack_range") && mechanism.requireFloat()) {
            getCitizen().getNavigator().getDefaultParameters().attackRange(mechanism.getValue().asFloat());
        }

        // <--[mechanism]
        // @object dNPC
        // @name speed
        // @input Element(Decimal)
        // @description
        // Sets the movement speed of the NPC.
        // @tags
        // <n@npc.navigator.speed>
        // -->
        if (mechanism.matches("speed") && mechanism.requireFloat()) {
            getCitizen().getNavigator().getDefaultParameters().speedModifier(mechanism.getValue().asFloat());
        }

        // <--[mechanism]
        // @object dNPC
        // @name despawn
        // @input none
        // @description
        // Despawns the NPC.
        // @tags
        // <n@npc.is_spawned>
        // -->
        if (mechanism.matches("despawn")) {
            getCitizen().despawn(DespawnReason.PLUGIN);
        }

        // <--[mechanism]
        // @object dNPC
        // @name set_protected
        // @input Element(Boolean)
        // @description
        // Sets whether or not the NPC is protected.
        // @tags
        // <n@npc.is_protected>
        // -->
        if (mechanism.matches("set_protected") && mechanism.requireBoolean()) {
            getCitizen().setProtected(value.asBoolean());
        }

        // <--[mechanism]
        // @object dNPC
        // @name set_lookclose
        // @input Element(Boolean)
        // @description
        // Sets the NPC's lookclose value.
        // @tags
        // <n@npc.lookclose>
        // -->
        if (mechanism.matches("lookclose") && mechanism.requireBoolean()) {
            getLookCloseTrait().lookClose(value.asBoolean());
        }

        // <--[mechanism]
        // @object dNPC
        // @name set_examiner
        // @input Element
        // @description
        // Sets the NPC's block examiner
        // @tags
        // TODO
        // -->
        if (mechanism.matches("set_examiner")) {

            if (mechanism.getValue().toString().equalsIgnoreCase("default")) {
                getNavigator().getLocalParameters().clearExaminers();
                getNavigator().getLocalParameters().examiner(new MinecraftBlockExaminer());

            }
            else if (mechanism.getValue().toString().equalsIgnoreCase("fly")) {
                getNavigator().getLocalParameters().clearExaminers();
                getNavigator().getLocalParameters().examiner(new FlyingBlockExaminer());

            }
            else if (mechanism.getValue().toString().equalsIgnoreCase("path")) {
                getNavigator().getLocalParameters().clearExaminers();
                getNavigator().getLocalParameters().examiner(new PathBlockExaminer(this, null));
            }

        }

        // <--[mechanism]
        // @object dNPC
        // @name teleport_on_stuck
        // @input Element(Boolean)
        // @description
        // Sets whether the NPC teleports when it is stuck.
        // @tags
        // <n@npc.teleport_on_stuck>
        // -->
        if (mechanism.matches("teleport_on_stuck") && mechanism.requireBoolean()) {
            if (value.asBoolean()) {
                getNavigator().getDefaultParameters().stuckAction(TeleportStuckAction.INSTANCE);
            }
            else {
                getNavigator().getDefaultParameters().stuckAction(null);
            }
        }

        // <--[mechanism]
        // @object dNPC
        // @name set_distance
        // @input Element
        // @description
        // Sets the NPC's distance margin.
        // @tags
        // TODO
        // -->
        if (mechanism.matches("set_distance") && mechanism.requireDouble()) {
            getNavigator().getDefaultParameters().distanceMargin(mechanism.getValue().asDouble());
        }

        // <--[mechanism]
        // @object dNPC
        // @name add_waypoint
        // @input dLocation
        // @description
        // Add a waypoint location to the NPC's path.
        // @tags
        // TODO
        // -->
        if (mechanism.matches("add_waypoint") && mechanism.requireObject(dLocation.class)) {
            if (!getCitizen().hasTrait(Waypoints.class)) {
                getCitizen().addTrait(Waypoints.class);
            }
            Waypoints wp = getCitizen().getTrait(Waypoints.class);
            if ((wp.getCurrentProvider() instanceof WaypointProvider.EnumerableWaypointProvider)) {
                ((List<Waypoint>) ((WaypointProvider.EnumerableWaypointProvider) wp.getCurrentProvider()).waypoints())
                        .add(new Waypoint(value.asType(dLocation.class)));
            }
        }

        // Iterate through this object's properties' mechanisms
        for (Property property : PropertyParser.getProperties(this)) {
            property.adjust(mechanism);
            if (mechanism.fulfilled()) {
                break;
            }
        }

        // Pass along to dEntity mechanism handler if not already handled.
        if (!mechanism.fulfilled()) {
            if (isSpawned()) {
                new dEntity(getEntity()).adjust(mechanism);
            }
            else {
                mechanism.reportInvalid();
            }
        }
    }
}

package com.denizenscript.denizen.objects;

import com.denizenscript.denizen.npc.traits.*;
import com.denizenscript.denizen.scripts.commands.npc.EngageCommand;
import com.denizenscript.denizen.scripts.containers.core.InteractScriptContainer;
import com.denizenscript.denizen.scripts.containers.core.InteractScriptHelper;
import com.denizenscript.denizen.scripts.triggers.AbstractTrigger;
import com.denizenscript.denizen.utilities.DenizenAPI;
import com.denizenscript.denizencore.tags.ObjectTagProcessor;
import com.denizenscript.denizencore.tags.TagRunnable;
import com.denizenscript.denizencore.utilities.Deprecations;
import com.denizenscript.denizencore.utilities.debugging.Debug;
import com.denizenscript.denizencore.objects.*;
import com.denizenscript.denizen.flags.FlagManager;
import com.denizenscript.denizen.npc.DenizenNPCHelper;
import com.denizenscript.denizen.tags.core.NPCTagBase;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.core.ListTag;
import com.denizenscript.denizencore.objects.core.ScriptTag;
import com.denizenscript.denizencore.tags.Attribute;
import com.denizenscript.denizencore.tags.TagContext;
import com.denizenscript.denizencore.utilities.CoreUtilities;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.ai.Navigator;
import net.citizensnpcs.api.ai.TeleportStuckAction;
import net.citizensnpcs.api.event.DespawnReason;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.api.trait.trait.Equipment;
import net.citizensnpcs.api.trait.trait.Owner;
import net.citizensnpcs.npc.skin.SkinnableEntity;
import net.citizensnpcs.trait.Anchors;
import net.citizensnpcs.trait.LookClose;
import net.citizensnpcs.trait.Poses;
import net.citizensnpcs.trait.waypoint.*;
import net.citizensnpcs.util.Anchor;
import net.citizensnpcs.util.Pose;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.*;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public class NPCTag implements ObjectTag, Adjustable, InventoryHolder, EntityFormObject {

    // <--[language]
    // @name NPCTag
    // @group Object System
    // @description
    // A NPCTag represents an NPC configured through Citizens.
    //
    // For format info, see <@link language n@>
    //
    // -->

    // <--[language]
    // @name n@
    // @group Object Fetcher System
    // @description
    // n@ refers to the 'object identifier' of a NPCTag. The 'n@' is notation for Denizen's Object
    // Fetcher. The constructor for a NPCTag is the NPC's id number.
    // For example, 'n@5'.
    //
    // For general info, see <@link language NPCTag>
    //
    // -->

    public static NPCTag mirrorCitizensNPC(NPC npc) {
        return new NPCTag(npc);
    }

    public static NPCTag fromEntity(Entity entity) {
        return mirrorCitizensNPC(CitizensAPI.getNPCRegistry().getNPC(entity));
    }


    public static NPCTag valueOf(String string) {
        return valueOf(string, null);
    }

    @Fetchable("n")
    public static NPCTag valueOf(String string, TagContext context) {
        if (string == null) {
            return null;
        }

        ////////
        // Match NPC id

        string = string.toUpperCase().replace("N@", "");
        NPC npc;
        if (ArgumentHelper.matchesInteger(string)) {
            int id = ArgumentHelper.getIntegerFrom(string);

            npc = CitizensAPI.getNPCRegistry().getById(id);
            if (npc != null) {
                return new NPCTag(npc);
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
        if (ArgumentHelper.matchesInteger(string)) {
            npc = CitizensAPI.getNPCRegistry().getById(ArgumentHelper.getIntegerFrom(string));
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

    public NPCTag(NPC citizensNPC) {
        if (citizensNPC != null) {
            this.npcid = citizensNPC.getId();
        }
    }

    public NPC getCitizen() {
        if (npcid < 0) {
            return null;
        }
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
        catch (NullPointerException ex) {
            Debug.echoError("Uh oh! Denizen has encountered a NPE while trying to fetch an NPC entity. " +
                    "Has this NPC been removed?");
            if (Debug.verbose) {
                Debug.echoError(ex);
            }
            return null;
        }
    }

    public LivingEntity getLivingEntity() {
        try {
            if (getCitizen().getEntity() instanceof LivingEntity) {
                return (LivingEntity) getCitizen().getEntity();
            }
            else {
                Debug.log("Uh oh! Tried to get the living entity of a non-living NPC!");
                return null;
            }
        }
        catch (NullPointerException ex) {
            Debug.echoError("Uh oh! Denizen has encountered a NPE while trying to fetch an NPC livingEntity. " +
                    "Has this NPC been removed?");
            if (Debug.verbose) {
                Debug.echoError(ex);
            }
            return null;
        }
    }


    @Override
    public EntityTag getDenizenEntity() {
        try {
            return new EntityTag(getCitizen().getEntity());
        }
        catch (NullPointerException ex) {
            Debug.echoError("Uh oh! Denizen has encountered a NPE while trying to fetch an NPC EntityTag. " +
                    "Has this NPC been removed?");
            if (Debug.verbose) {
                Debug.echoError(ex);
            }
            return null;
        }
    }

    @Override
    public Inventory getInventory() {
        return DenizenNPCHelper.getInventory(getCitizen());
    }

    public InventoryTag getDenizenInventory() {
        return new InventoryTag(this);
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

    public InteractScriptContainer getInteractScript(PlayerTag player, Class<? extends AbstractTrigger> triggerType) {
        return InteractScriptHelper.getInteractScript(this, player, triggerType);
    }

    public InteractScriptContainer getInteractScriptQuietly(PlayerTag player, Class<? extends AbstractTrigger> triggerType) {
        InteractScriptHelper.debugGet = false;
        InteractScriptContainer script = InteractScriptHelper.getInteractScript(this, player, triggerType);
        InteractScriptHelper.debugGet = true;
        return script;
    }

    public void destroy() {
        getCitizen().destroy();
    }

    public LocationTag getLocation() {
        if (isSpawned()) {
            return new LocationTag(getEntity().getLocation());
        }
        else {
            return new LocationTag(getCitizen().getStoredLocation());
        }
    }

    public LocationTag getEyeLocation() {
        if (isSpawned() && getCitizen().getEntity() instanceof LivingEntity) {
            return new LocationTag(((LivingEntity) getCitizen().getEntity()).getEyeLocation());
        }
        else if (isSpawned()) {
            return new LocationTag(getEntity().getLocation());
        }
        else {
            return new LocationTag(getCitizen().getStoredLocation());
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
        return identify();
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

    public String action(String actionName, PlayerTag player, Map<String, ObjectTag> context) {
        if (getCitizen() != null) {
            if (getCitizen().hasTrait(AssignmentTrait.class))
            // Return the result from the ActionHandler
            {
                return DenizenAPI.getCurrentInstance().getNPCHelper()
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

    public String action(String actionName, PlayerTag player) {
        return action(actionName, player, null);
    }

    private String prefix = "npc";

    @Override
    public String getPrefix() {
        return prefix;
    }

    @Override
    public String debuggable() {
        return "n@" + npcid + "<GR> (" + getName() + ")";
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
    public NPCTag setPrefix(String prefix) {
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null) {
            return false;
        }
        if (!(o instanceof NPCTag)) {
            return false;
        }
        return getId() == ((NPCTag) o).getId();
    }

    @Override
    public int hashCode() {
        return getId();
    }

    public static void registerTags() {

        // Defined in EntityTag
        registerTag("is_npc", new TagRunnable.ObjectForm<NPCTag>() {
            @Override
            public ObjectTag run(Attribute attribute, NPCTag object) {
                return new ElementTag(true);
            }
        });

        // Defined in EntityTag
        registerTag("location", new TagRunnable.ObjectForm<NPCTag>() {
            @Override
            public ObjectTag run(Attribute attribute, NPCTag object) {

                if (attribute.startsWith("previous_location", 2)) {
                    attribute.fulfill(1);
                    Deprecations.npcPreviousLocationTag.warn(attribute.context);
                    return NPCTagBase.previousLocations.get(object.getId());
                }
                if (object.isSpawned()) {
                    return new EntityTag(object).getObjectAttribute(attribute);
                }
                return object.getLocation();
            }
        });

        // <--[tag]
        // @attribute <NPCTag.previous_location>
        // @returns LocationTag
        // @description
        // Returns the NPC's previous navigated location.
        // -->
        registerTag("previous_location", new TagRunnable.ObjectForm<NPCTag>() {
            @Override
            public ObjectTag run(Attribute attribute, NPCTag object) {
                return NPCTagBase.previousLocations.get(object.getId());
            }
        });

        // Defined in EntityTag
        registerTag("eye_location", new TagRunnable.ObjectForm<NPCTag>() {
            @Override
            public ObjectTag run(Attribute attribute, NPCTag object) {
                return object.getEyeLocation();
            }
        });

        // <--[tag]
        // @attribute <NPCTag.has_nickname>
        // @returns ElementTag(Boolean)
        // @description
        // Returns true if the NPC has a nickname.
        // -->
        registerTag("has_nickname", new TagRunnable.ObjectForm<NPCTag>() {
            @Override
            public ObjectTag run(Attribute attribute, NPCTag object) {
                NPC citizen = object.getCitizen();
                return new ElementTag(citizen.hasTrait(NicknameTrait.class) && citizen.getTrait(NicknameTrait.class).hasNickname());
            }
        });

        // <--[tag]
        // @attribute <NPCTag.nickname>
        // @returns ElementTag
        // @description
        // Returns the NPC's display name, as set by the Nickname trait (or the default NPC name).
        // -->
        registerTag("nickname", new TagRunnable.ObjectForm<NPCTag>() {
            @Override
            public ObjectTag run(Attribute attribute, NPCTag object) {
                return new ElementTag(object.getCitizen().hasTrait(NicknameTrait.class) ? object.getCitizen().getTrait(NicknameTrait.class)
                        .getNickname() : object.getName());
            }
        });

        // <--[tag]
        // @attribute <NPCTag.name>
        // @returns ElementTag
        // @description
        // Returns the name of the NPC.
        // -->
        registerTag("name", new TagRunnable.ObjectForm<NPCTag>() {
            @Override
            public ObjectTag run(Attribute attribute, NPCTag object) {

                if (attribute.startsWith("nickname", 2)) {
                    Deprecations.npcNicknameTag.warn(attribute.context);
                    attribute.fulfill(1);
                    return new ElementTag(object.getCitizen().hasTrait(NicknameTrait.class) ? object.getCitizen().getTrait(NicknameTrait.class)
                            .getNickname() : object.getName());
                }
                return new ElementTag(object.getName());
            }
        });

        // <--[tag]
        // @attribute <NPCTag.list_traits>
        // @returns ListTag
        // @description
        // Returns a list of all of the NPC's traits.
        // -->
        registerTag("list_traits", new TagRunnable.ObjectForm<NPCTag>() {
            @Override
            public ObjectTag run(Attribute attribute, NPCTag object) {
                List<String> list = new ArrayList<>();
                for (Trait trait : object.getCitizen().getTraits()) {
                    list.add(trait.getName());
                }
                return new ListTag(list);
            }
        });

        // <--[tag]
        // @attribute <NPCTag.has_trait[<trait>]>
        // @returns ElementTag(Boolean)
        // @description
        // Returns whether the NPC has a specified trait.
        // -->
        registerTag("has_trait", new TagRunnable.ObjectForm<NPCTag>() {
            @Override
            public ObjectTag run(Attribute attribute, NPCTag object) {
                if (attribute.hasContext(1)) {
                    Class<? extends Trait> trait = CitizensAPI.getTraitFactory().getTraitClass(attribute.getContext(1));
                    if (trait != null) {
                        return new ElementTag(object.getCitizen().hasTrait(trait));
                    }
                }
                return null;
            }
        });

        // <--[tag]
        // @attribute <NPCTag.pushable>
        // @returns ElementTag(Boolean)
        // @description
        // Returns whether the NPC is pushable.
        // -->
        registerTag("pushable", new TagRunnable.ObjectForm<NPCTag>() {
            @Override
            public ObjectTag run(Attribute attribute, NPCTag object) {
                return new ElementTag(object.getPushableTrait().isPushable());
            }
        });
        registerTag("is_pushable", tagProcessor.registeredObjectTags.get("pushable"));

        // <--[tag]
        // @attribute <NPCTag.has_trigger[<trigger>]>
        // @returns ElementTag(Boolean)
        // @description
        // Returns whether the NPC has a specified trigger.
        // -->
        registerTag("has_trigger", new TagRunnable.ObjectForm<NPCTag>() {
            @Override
            public ObjectTag run(Attribute attribute, NPCTag object) {
                if (!attribute.hasContext(1)) {
                    return null;
                }
                if (!object.getCitizen().hasTrait(TriggerTrait.class)) {
                    return new ElementTag(false);
                }
                TriggerTrait trait = object.getCitizen().getTrait(TriggerTrait.class);
                return new ElementTag(trait.hasTrigger(attribute.getContext(1)));
            }
        });


        // <--[tag]
        // @attribute <NPCTag.has_anchors>
        // @returns ElementTag(Boolean)
        // @description
        // Returns whether the NPC has anchors assigned.
        // -->
        registerTag("has_anchors", new TagRunnable.ObjectForm<NPCTag>() {
            @Override
            public ObjectTag run(Attribute attribute, NPCTag object) {
                return (new ElementTag(object.getCitizen().getTrait(Anchors.class).getAnchors().size() > 0));
            }
        });


        // <--[tag]
        // @attribute <NPCTag.list_anchors>
        // @returns ListTag
        // @description
        // Returns a list of anchor names currently assigned to the NPC.
        // -->
        registerTag("list_anchors", new TagRunnable.ObjectForm<NPCTag>() {
            @Override
            public ObjectTag run(Attribute attribute, NPCTag object) {
                ListTag list = new ListTag();
                for (Anchor anchor : object.getCitizen().getTrait(Anchors.class).getAnchors()) {
                    list.add(anchor.getName());
                }
                return list;
            }
        });

        // <--[tag]
        // @attribute <NPCTag.anchor[<name>]>
        // @returns LocationTag
        // @description
        // Returns the location associated with the specified anchor, or null if it doesn't exist.
        // -->
        registerTag("anchor", new TagRunnable.ObjectForm<NPCTag>() {
            @Override
            public ObjectTag run(Attribute attribute, NPCTag object) {
                if (attribute.hasContext(1)
                        && object.getCitizen().getTrait(Anchors.class).getAnchor(attribute.getContext(1)) != null) {
                    return new LocationTag(object.getCitizen().getTrait(Anchors.class)
                            .getAnchor(attribute.getContext(1)).getLocation());
                }
                else if (attribute.startsWith("list", 2)) {
                    attribute.fulfill(1);
                    Deprecations.npcAnchorListTag.warn(attribute.context);
                    ListTag list = new ListTag();
                    for (Anchor anchor : object.getCitizen().getTrait(Anchors.class).getAnchors()) {
                        list.add(anchor.getName());
                    }
                    return list;
                }
                return null;
            }
        });
        registerTag("anchors", tagProcessor.registeredObjectTags.get("anchor"));

        // <--[tag]
        // @attribute <NPCTag.has_flag[<flag_name>]>
        // @returns ElementTag(Boolean)
        // @description
        // Returns true if the NPC has the specified flag, otherwise returns false.
        // -->
        registerTag("has_flag", new TagRunnable.ObjectForm<NPCTag>() {
            @Override
            public ObjectTag run(Attribute attribute, NPCTag object) {
                String flag_name;
                if (attribute.hasContext(1)) {
                    flag_name = attribute.getContext(1);
                }
                else {
                    return null;
                }
                return new ElementTag(FlagManager.npcHasFlag(object, flag_name));
            }
        });

        // <--[tag]
        // @attribute <NPCTag.flag[<flag_name>]>
        // @returns Flag ListTag
        // @description
        // Returns the specified flag from the NPC.
        // -->
        registerTag("flag", new TagRunnable.ObjectForm<NPCTag>() {
            @Override
            public ObjectTag run(Attribute attribute, NPCTag object) {
                if (!attribute.hasContext(1)) {
                    return null;
                }
                String flag_name = attribute.getContext(1);

                // <--[tag]
                // @attribute <NPCTag.flag[<flag_name>].is_expired>
                // @returns ElementTag(Boolean)
                // @description
                // returns true if the flag is expired or does not exist, false if it is not yet expired or has no expiration.
                // -->
                if (attribute.startsWith("is_expired", 2) || attribute.startsWith("isexpired", 22)) {
                    attribute.fulfill(1);
                    return new ElementTag(!FlagManager.npcHasFlag(object, flag_name));
                }
                if (attribute.startsWith("size", 2) && !FlagManager.npcHasFlag(object, flag_name)) {
                    attribute.fulfill(1);
                    return new ElementTag(0);
                }
                if (FlagManager.npcHasFlag(object, flag_name)) {
                    FlagManager.Flag flag = DenizenAPI.getCurrentInstance().flagManager()
                            .getNPCFlag(object.getId(), flag_name);

                    // <--[tag]
                    // @attribute <NPCTag.flag[<flag_name>].expiration>
                    // @returns DurationTag
                    // @description
                    // Returns a DurationTag of the time remaining on the flag, if it has an expiration.
                    // -->
                    if (attribute.startsWith("expiration", 2)) {
                        attribute.fulfill(1);
                        return flag.expiration();
                    }

                    return new ListTag(flag.toString(), true, flag.values());
                }
                return null;
            }
        });

        // <--[tag]
        // @attribute <NPCTag.list_flags[(regex:)<search>]>
        // @returns ListTag
        // @description
        // Returns a list of an NPC's flag names, with an optional search for
        // names containing a certain pattern.
        // -->
        registerTag("list_flags", new TagRunnable.ObjectForm<NPCTag>() {
            @Override
            public ObjectTag run(Attribute attribute, NPCTag object) {
                ListTag allFlags = new ListTag(DenizenAPI.getCurrentInstance().flagManager().listNPCFlags(object.getId()));
                ListTag searchFlags = null;
                if (!allFlags.isEmpty() && attribute.hasContext(1)) {
                    searchFlags = new ListTag();
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
                            Debug.echoError(e);
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
                return searchFlags == null ? allFlags
                        : searchFlags;
            }
        });

        // <--[tag]
        // @attribute <NPCTag.constant[<constant_name>]>
        // @returns ElementTag
        // @description
        // Returns the specified constant from the NPC.
        // -->
        registerTag("constant", new TagRunnable.ObjectForm<NPCTag>() {
            @Override
            public ObjectTag run(Attribute attribute, NPCTag object) {
                if (attribute.hasContext(1)) {
                    if (object.getCitizen().hasTrait(ConstantsTrait.class)
                            && object.getCitizen().getTrait(ConstantsTrait.class).getConstant(attribute.getContext(1)) != null) {
                        return new ElementTag(object.getCitizen().getTrait(ConstantsTrait.class)
                                .getConstant(attribute.getContext(1)));
                    }
                    else {
                        return null;
                    }
                }
                return null;
            }
        });

        // <--[tag]
        // @attribute <NPCTag.has_pose[<name>]>
        // @returns ElementTag(Boolean)
        // @description
        // Returns true if the NPC has the specified pose, otherwise returns false.
        // -->
        registerTag("has_pose", new TagRunnable.ObjectForm<NPCTag>() {
            @Override
            public ObjectTag run(Attribute attribute, NPCTag object) {
                if (attribute.hasContext(1)) {
                    return new ElementTag(object.getCitizen().getTrait(Poses.class).hasPose(attribute.getContext(1)));
                }
                else {
                    return null;
                }
            }
        });

        // <--[tag]
        // @attribute <NPCTag.pose[<name>]>
        // @returns LocationTag
        // @description
        // Returns the pose as a LocationTag with x, y, and z set to 0, and the world set to the first
        // possible available world Bukkit knows about.
        // -->
        registerTag("pose", new TagRunnable.ObjectForm<NPCTag>() {
            @Override
            public ObjectTag run(Attribute attribute, NPCTag object) {
                if (attribute.hasContext(1)) {
                    Pose pose = object.getCitizen().getTrait(Poses.class).getPose(attribute.getContext(1));
                    return new LocationTag(org.bukkit.Bukkit.getWorlds().get(0), 0, 0, 0, pose.getYaw(), pose.getPitch());
                }
                else {
                    return null;
                }
            }
        });
        registerTag("get_pose", tagProcessor.registeredObjectTags.get("pose"));

        // <--[tag]
        // @attribute <NPCTag.is_sneaking>
        // @returns ElementTag(Boolean)
        // @description
        // Returns whether the NPC is currently sneaking. Only works for player-type NPCs.
        // -->
        registerTag("is_sneaking", new TagRunnable.ObjectForm<NPCTag>() {
            @Override
            public ObjectTag run(Attribute attribute, NPCTag object) {
                if (!object.isSpawned() && object.getEntity() instanceof Player) {
                    return null;
                }
                return new ElementTag(((Player) object.getEntity()).isSneaking());
            }
        });

        // <--[tag]
        // @attribute <NPCTag.is_engaged>
        // @returns ElementTag(Boolean)
        // @description
        // Returns whether the NPC is currently engaged.
        // See <@link command engage>
        // -->
        registerTag("engaged", new TagRunnable.ObjectForm<NPCTag>() {
            @Override
            public ObjectTag run(Attribute attribute, NPCTag object) {
                return new ElementTag(object.isEngaged());
            }
        });
        registerTag("is_engaged", tagProcessor.registeredObjectTags.get("engaged"));

        // <--[tag]
        // @attribute <NPCTag.invulnerable>
        // @returns ElementTag(Boolean)
        // @description
        // Returns whether the NPC is currently invulnerable.
        // See <@link command vulnerable>
        // -->
        registerTag("invulnerable", new TagRunnable.ObjectForm<NPCTag>() {
            @Override
            public ObjectTag run(Attribute attribute, NPCTag object) {
                return new ElementTag(object.getCitizen().data().get(NPC.DEFAULT_PROTECTED_METADATA, true));
            }
        });
        registerTag("vulnerable", tagProcessor.registeredObjectTags.get("invulnerable"));

        // <--[tag]
        // @attribute <NPCTag.id>
        // @returns ElementTag(Number)
        // @description
        // Returns the NPC's ID number.
        // -->
        registerTag("id", new TagRunnable.ObjectForm<NPCTag>() {
            @Override
            public ObjectTag run(Attribute attribute, NPCTag object) {
                return new ElementTag(object.getId());
            }
        });

        // <--[tag]
        // @attribute <NPCTag.owner>
        // @returns PlayerTag/Element
        // @description
        // Returns the owner of the NPC as a PlayerTag if it's a player, otherwise as just the name.
        // -->
        registerTag("owner", new TagRunnable.ObjectForm<NPCTag>() {
            @Override
            public ObjectTag run(Attribute attribute, NPCTag object) {
                String owner = object.getOwner();
                PlayerTag player = null;
                if (!owner.equalsIgnoreCase("server")) {
                    player = PlayerTag.valueOfInternal(owner, false);
                }
                if (player != null) {
                    return player;
                }
                else {
                    return new ElementTag(owner);
                }
            }
        });

        // <--[tag]
        // @attribute <NPCTag.has_skin>
        // @returns ElementTag
        // @description
        // Returns whether the NPC has a custom skinskin.
        // -->
        registerTag("has_skin", new TagRunnable.ObjectForm<NPCTag>() {
            @Override
            public ObjectTag run(Attribute attribute, NPCTag object) {
                return new ElementTag(object.getCitizen().data().has(NPC.PLAYER_SKIN_UUID_METADATA));
            }
        });

        // <--[tag]
        // @attribute <NPCTag.skin_blob>
        // @returns ElementTag
        // @description
        // Returns the NPC's custom skin blob, if any.
        // -->
        registerTag("skin_blob", new TagRunnable.ObjectForm<NPCTag>() {
            @Override
            public ObjectTag run(Attribute attribute, NPCTag object) {
                if (object.getCitizen().data().has(NPC.PLAYER_SKIN_TEXTURE_PROPERTIES_METADATA)) {
                    String tex = object.getCitizen().data().get(NPC.PLAYER_SKIN_TEXTURE_PROPERTIES_METADATA).toString();
                    String sign = "";
                    if (object.getCitizen().data().has(NPC.PLAYER_SKIN_TEXTURE_PROPERTIES_SIGN_METADATA)) {
                        sign = ";" + object.getCitizen().data().get(NPC.PLAYER_SKIN_TEXTURE_PROPERTIES_SIGN_METADATA).toString();
                    }
                    return new ElementTag(tex + sign);
                }
                return null;
            }
        });

        // <--[tag]
        // @attribute <NPCTag.skin>
        // @returns ElementTag
        // @description
        // Returns the NPC's custom skin, if any.
        // -->
        registerTag("skin", new TagRunnable.ObjectForm<NPCTag>() {
            @Override
            public ObjectTag run(Attribute attribute, NPCTag object) {
                if (object.getCitizen().data().has(NPC.PLAYER_SKIN_UUID_METADATA)) {
                    return new ElementTag(object.getCitizen().data().get(NPC.PLAYER_SKIN_UUID_METADATA).toString());
                }
                return null;
            }
        });

        // <--[tag]
        // @attribute <NPCTag.inventory>
        // @returns InventoryTag
        // @description
        // Returns the InventoryTag of the NPC.
        // -->
        registerTag("inventory", new TagRunnable.ObjectForm<NPCTag>() {
            @Override
            public ObjectTag run(Attribute attribute, NPCTag object) {
                return object.getDenizenInventory();
            }
        });

        // <--[tag]
        // @attribute <NPCTag.is_spawned>
        // @returns ElementTag(Boolean)
        // @description
        // Returns whether the NPC is spawned.
        // -->
        registerTag("is_spawned", new TagRunnable.ObjectForm<NPCTag>() {
            @Override
            public ObjectTag run(Attribute attribute, NPCTag object) {
                return new ElementTag(object.isSpawned());
            }
        });

        // <--[tag]
        // @attribute <NPCTag.is_protected>
        // @returns ElementTag(Boolean)
        // @description
        // Returns whether the NPC is protected.
        // -->
        registerTag("is_protected", new TagRunnable.ObjectForm<NPCTag>() {
            @Override
            public ObjectTag run(Attribute attribute, NPCTag object) {
                return new ElementTag(object.getCitizen().isProtected());
            }
        });

        // <--[tag]
        // @attribute <NPCTag.lookclose>
        // @returns ElementTag(Boolean)
        // @description
        // Returns the NPC's "lookclose" value.
        // -->
        registerTag("lookclose", new TagRunnable.ObjectForm<NPCTag>() {
            @Override
            public ObjectTag run(Attribute attribute, NPCTag object) {
                NPC citizen = object.getCitizen();
                if (citizen.hasTrait(LookClose.class)) {
                    // There is no method to check if the NPC has LookClose enabled...
                    // LookClose.toString() returns "LookClose{" + enabled + "}"
                    String lookclose = citizen.getTrait(LookClose.class).toString();
                    lookclose = lookclose.substring(10, lookclose.length() - 1);
                    return new ElementTag(Boolean.valueOf(lookclose));
                }
                return new ElementTag(false);
            }
        });

        // <--[tag]
        // @attribute <NPCTag.teleport_on_stuck>
        // @returns LocationTag
        // @mechanism NPCTag.teleport_on_stuck
        // @description
        // Returns whether the NPC teleports when it is stuck.
        // -->
        registerTag("teleport_on_stuck", new TagRunnable.ObjectForm<NPCTag>() {
            @Override
            public ObjectTag run(Attribute attribute, NPCTag object) {
                return new ElementTag(object.getNavigator().getDefaultParameters().stuckAction() == TeleportStuckAction.INSTANCE);
            }
        });

        // <--[tag]
        // @attribute <NPCTag.has_script>
        // @returns ElementTag(Boolean)
        // @description
        // Returns true if the NPC has an assignment script.
        // -->
        registerTag("has_script", new TagRunnable.ObjectForm<NPCTag>() {
            @Override
            public ObjectTag run(Attribute attribute, NPCTag object) {
                NPC citizen = object.getCitizen();
                return new ElementTag(citizen.hasTrait(AssignmentTrait.class) && citizen.getTrait(AssignmentTrait.class).hasAssignment());
            }
        });

        // <--[tag]
        // @attribute <NPCTag.script>
        // @returns ScriptTag
        // @description
        // Returns the NPC's assigned script.
        // -->
        registerTag("script", new TagRunnable.ObjectForm<NPCTag>() {
            @Override
            public ObjectTag run(Attribute attribute, NPCTag object) {
                NPC citizen = object.getCitizen();
                if (!citizen.hasTrait(AssignmentTrait.class) || !citizen.getTrait(AssignmentTrait.class).hasAssignment()) {
                    return null;
                }
                else {
                    return new ScriptTag(citizen.getTrait(AssignmentTrait.class).getAssignment().getName());
                }
            }
        });

        // <--[tag]
        // @attribute <NPCTag.distance_margin>
        // @returns ElementTag(Decimal)
        // @mechanism distance_margin
        // @description
        // Returns the NPC's current pathfinding distance margin. That is, how close it needs to get to its destination (in block-lengths).
        // -->
        registerTag("distance_margin", new TagRunnable.ObjectForm<NPCTag>() {
            @Override
            public ObjectTag run(Attribute attribute, NPCTag object) {
                return new ElementTag(object.getNavigator().getDefaultParameters().distanceMargin());
            }
        });

        // <--[tag]
        // @attribute <NPCTag.path_distance_margin>
        // @returns ElementTag(Decimal)
        // @mechanism path_distance_margin
        // @description
        // Returns the NPC's current pathfinding distance margin. That is, how close it needs to get to individual points along its path.
        // -->
        registerTag("path_distance_margin", new TagRunnable.ObjectForm<NPCTag>() {
            @Override
            public ObjectTag run(Attribute attribute, NPCTag object) {
                return new ElementTag(object.getNavigator().getDefaultParameters().pathDistanceMargin());
            }
        });

        // <--[tag]
        // @attribute <NPCTag.is_navigating>
        // @returns ElementTag(Boolean)
        // @description
        // Returns whether the NPC is currently navigating.
        // -->
        registerTag("is_navigating", new TagRunnable.ObjectForm<NPCTag>() {
            @Override
            public ObjectTag run(Attribute attribute, NPCTag object) {
                return new ElementTag(object.getNavigator().isNavigating());
            }
        });

        // <--[tag]
        // @attribute <NPCTag.speed>
        // @returns ElementTag(Decimal)
        // @description
        // Returns the current speed of the NPC.
        // -->
        registerTag("speed", new TagRunnable.ObjectForm<NPCTag>() {
            @Override
            public ObjectTag run(Attribute attribute, NPCTag object) {
                return new ElementTag(object.getNavigator().getLocalParameters().speed());
            }
        });

        // <--[tag]
        // @attribute <NPCTag.range>
        // @returns ElementTag(Decimal)
        // @description
        // Returns the NPC's current maximum pathfinding range.
        // -->
        registerTag("range", new TagRunnable.ObjectForm<NPCTag>() {
            @Override
            public ObjectTag run(Attribute attribute, NPCTag object) {
                return new ElementTag(object.getNavigator().getLocalParameters().range());
            }
        });

        // <--[tag]
        // @attribute <NPCTag.attack_range>
        // @returns ElementTag(Decimal)
        // @description
        // Returns the NPC's current navigator attack range limit.
        // -->
        registerTag("attack_range", new TagRunnable.ObjectForm<NPCTag>() {
            @Override
            public ObjectTag run(Attribute attribute, NPCTag object) {
                return new ElementTag(object.getNavigator().getLocalParameters().attackRange());
            }
        });

        // <--[tag]
        // @attribute <NPCTag.attack_strategy>
        // @returns ElementTag
        // @description
        // Returns the NPC's current navigator attack strategy.
        // -->
        registerTag("attack_strategy", new TagRunnable.ObjectForm<NPCTag>() {
            @Override
            public ObjectTag run(Attribute attribute, NPCTag object) {
                return new ElementTag(object.getNavigator().getLocalParameters().attackStrategy().toString());
            }
        });

        // <--[tag]
        // @attribute <NPCTag.speed_modifier>
        // @returns ElementTag(Decimal)
        // @description
        // Returns the NPC's current movement speed modifier (a multiplier applied over their base speed).
        // -->
        registerTag("speed_modifier", new TagRunnable.ObjectForm<NPCTag>() {
            @Override
            public ObjectTag run(Attribute attribute, NPCTag object) {
                return new ElementTag(object.getNavigator().getLocalParameters().speedModifier());
            }
        });

        // <--[tag]
        // @attribute <NPCTag.base_speed>
        // @returns ElementTag(Decimal)
        // @description
        // Returns the NPC's base navigation speed.
        // -->
        registerTag("base_speed", new TagRunnable.ObjectForm<NPCTag>() {
            @Override
            public ObjectTag run(Attribute attribute, NPCTag object) {
                return new ElementTag(object.getNavigator().getLocalParameters().baseSpeed());
            }
        });

        // <--[tag]
        // @attribute <NPCTag.avoid_water>
        // @returns ElementTag(Boolean)
        // @description
        // Returns whether the NPC will avoid water.
        // -->
        registerTag("avoid_water", new TagRunnable.ObjectForm<NPCTag>() {
            @Override
            public ObjectTag run(Attribute attribute, NPCTag object) {
                return new ElementTag(object.getNavigator().getLocalParameters().avoidWater());
            }
        });

        // <--[tag]
        // @attribute <NPCTag.target_location>
        // @returns LocationTag
        // @description
        // Returns the location the NPC is currently navigating towards (if any).
        // -->
        registerTag("target_location", new TagRunnable.ObjectForm<NPCTag>() {
            @Override
            public ObjectTag run(Attribute attribute, NPCTag object) {
                if (object.getNavigator().getTargetAsLocation() == null) {
                    return null;
                }
                return new LocationTag(object.getNavigator().getTargetAsLocation());
            }
        });

        // <--[tag]
        // @attribute <NPCTag.is_fighting>
        // @returns ElementTag(Boolean)
        // @description
        // Returns whether the NPC is in combat.
        // -->
        registerTag("is_fighting", new TagRunnable.ObjectForm<NPCTag>() {
            @Override
            public ObjectTag run(Attribute attribute, NPCTag object) {
                return new ElementTag(object.getNavigator().getEntityTarget() != null && object.getNavigator().getEntityTarget().isAggressive());
            }
        });

        // <--[tag]
        // @attribute <NPCTag.target_type>
        // @returns ElementTag
        // @description
        // Returns the entity type of the NPC's current navigation target (if any).
        // -->
        registerTag("target_type", new TagRunnable.ObjectForm<NPCTag>() {
            @Override
            public ObjectTag run(Attribute attribute, NPCTag object) {
                if (object.getNavigator().getTargetType() == null) {
                    return null;
                }
                return new ElementTag(object.getNavigator().getTargetType().toString());
            }
        });

        // <--[tag]
        // @attribute <NPCTag.target_entity>
        // @returns EntityTag
        // @description
        // Returns the entity being targeted by the NPC's current navigation (if any).
        // -->
        registerTag("target_entity", new TagRunnable.ObjectForm<NPCTag>() {
            @Override
            public ObjectTag run(Attribute attribute, NPCTag object) {
                if (object.getNavigator().getEntityTarget() == null || object.getNavigator().getEntityTarget().getTarget() == null) {
                    return null;
                }
                return new EntityTag(object.getNavigator().getEntityTarget().getTarget());
            }
        });

        registerTag("navigator", new TagRunnable.ObjectForm<NPCTag>() {
            @Override
            public ObjectTag run(Attribute attribute, NPCTag object) {
                Deprecations.oldNPCNavigator.warn(attribute.context);
                return object;
            }
        });

        // <--[tag]
        // @attribute <NPCTag.type>
        // @returns ElementTag
        // @description
        // Always returns 'NPC' for NPCTag objects. All objects fetchable by the Object Fetcher will return the
        // type of object that is fulfilling this attribute.
        // -->
        registerTag("type", new TagRunnable.ObjectForm<NPCTag>() {
            @Override
            public ObjectTag run(Attribute attribute, NPCTag object) {
                return new ElementTag("NPC");
            }
        });
    }

    public static ObjectTagProcessor<NPCTag> tagProcessor = new ObjectTagProcessor<>();

    public static void registerTag(String name, TagRunnable.ObjectForm<NPCTag> runnable) {
        tagProcessor.registerTag(name, runnable);
    }

    @Override
    public ObjectTag getObjectAttribute(Attribute attribute) {
        return tagProcessor.getObjectAttribute(this, attribute);
    }

    @Override
    public ObjectTag getNextObjectTypeDown() {
        if (getEntity() != null) {
            return new EntityTag(this);
        }
        return new ElementTag(identify());
    }

    public void applyProperty(Mechanism mechanism) {
        Debug.echoError("Cannot apply properties to an NPC!");
    }

    @Override
    public void adjust(Mechanism mechanism) {

        // TODO: For all the mechanism tags, add the @Mechanism link!

        // <--[mechanism]
        // @object NPCTag
        // @name set_assignment
        // @input ScriptTag
        // @description
        // Sets the NPC's assignment script.
        // @tags
        // <NPCTag.script>
        // -->
        if (mechanism.matches("set_assignment") && mechanism.requireObject(ScriptTag.class)) {
            getAssignmentTrait().setAssignment(mechanism.valueAsType(ScriptTag.class).getName(), null);
        }

        // <--[mechanism]
        // @object NPCTag
        // @name remove_assignment
        // @input none
        // @description
        // Removes the NPC's assigment script.
        // @tags
        // <NPCTag.has_script>
        // -->
        if (mechanism.matches("remove_assignment")) {
            getAssignmentTrait().removeAssignment(null);
        }

        // <--[mechanism]
        // @object NPCTag
        // @name set_nickname
        // @input Element
        // @description
        // Sets the NPC's nickname.
        // @tags
        // <NPCTag.nickname>
        // -->
        if (mechanism.matches("set_nickname")) {
            getNicknameTrait().setNickname(mechanism.getValue().asString());
        }

        // <--[mechanism]
        // @object NPCTag
        // @name remove_nickname
        // @input none
        // @description
        // Removes the NPC's nickname.
        // @tags
        // <NPCTag.has_nickname>
        // -->
        if (mechanism.matches("remove_nickname")) {
            getNicknameTrait().removeNickname();
        }

        // <--[mechanism]
        // @object NPCTag
        // @name set_entity_type
        // @input EntityTag
        // @description
        // Sets the NPC's entity type.
        // @tags
        // <NPCTag.entity_type>
        // -->
        if (mechanism.matches("set_entity_type") && mechanism.requireObject(EntityTag.class)) {
            getCitizen().setBukkitEntityType(mechanism.valueAsType(EntityTag.class).getBukkitEntityType());
        }

        // <--[mechanism]
        // @object NPCTag
        // @name name
        // @input Element
        // @description
        // Sets the name of the NPC.
        // @tags
        // <NPCTag.name>
        // -->
        if (mechanism.matches("name") || mechanism.matches("set_name")) {
            getCitizen().setName(mechanism.getValue().asString().length() > 64 ? mechanism.getValue().asString().substring(0, 64) : mechanism.getValue().asString());
        }

        // <--[mechanism]
        // @object NPCTag
        // @name owner
        // @input Element
        // @description
        // Sets the owner of the NPC.
        // @tags
        // <NPCTag.owner>
        // -->
        if (mechanism.matches("owner")) {
            getCitizen().getTrait(Owner.class).setOwner(mechanism.getValue().asString());
        }

        // <--[mechanism]
        // @object NPCTag
        // @name skin_blob
        // @input Element
        // @description
        // Sets the skin blob of an NPC, in the form of "texture;signature;name".
        // Call with no value to clear the custom skin value.
        // @tags
        // <NPCTag.skin>
        // -->
        if (mechanism.matches("skin_blob")) {
            if (!mechanism.hasValue()) {
                getCitizen().data().remove("cached-skin-uuid");
                getCitizen().data().remove(NPC.PLAYER_SKIN_TEXTURE_PROPERTIES_METADATA);
                getCitizen().data().remove(NPC.PLAYER_SKIN_TEXTURE_PROPERTIES_SIGN_METADATA);
                if (getCitizen().isSpawned()) {
                    getCitizen().despawn(DespawnReason.PENDING_RESPAWN);
                    getCitizen().spawn(getCitizen().getStoredLocation());
                }
            }
            else {
                String[] dat = mechanism.getValue().asString().split(";");
                getCitizen().data().remove("cached-skin-uuid");
                getCitizen().data().setPersistent(NPC.PLAYER_SKIN_TEXTURE_PROPERTIES_METADATA, dat[0]);
                getCitizen().data().setPersistent(NPC.PLAYER_SKIN_TEXTURE_PROPERTIES_SIGN_METADATA, dat.length > 1 ? dat[1] : null);
                if (dat.length > 2) {
                    getCitizen().data().setPersistent(NPC.PLAYER_SKIN_UUID_METADATA, dat[2]);
                }
                if (getCitizen().isSpawned() && getCitizen().getEntity() instanceof SkinnableEntity) {
                    ((SkinnableEntity) getCitizen().getEntity()).setSkinPersistent(dat.length > 2 ? dat[2] : "unspecified", dat.length > 1 ? dat[1] : null, dat[0]);
                    ((SkinnableEntity) getCitizen().getEntity()).getSkinTracker().notifySkinChange(true);
                }
            }
        }

        // <--[mechanism]
        // @object NPCTag
        // @name skin
        // @input Element
        // @description
        // Sets the skin of an NPC by name.
        // Call with no value to clear the custom skin value.
        // @tags
        // <NPCTag.skin>
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
        // @object NPCTag
        // @name item_type
        // @input ItemTag
        // @description
        // Sets the item type of the item.
        // @tags
        // None
        // -->
        if (mechanism.matches("item_type") && mechanism.requireObject(ItemTag.class)) {
            ItemTag item = mechanism.valueAsType(ItemTag.class);
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
                    Debug.echoError("NPC is the not an item type!");
                    break;
            }
            if (getCitizen().isSpawned()) {
                getCitizen().despawn();
                getCitizen().spawn(getCitizen().getStoredLocation());
            }
        }

        // <--[mechanism]
        // @object NPCTag
        // @name spawn
        // @input LocationTag
        // @description
        // Spawns the NPC at a location. If no location is specified, the NPC will spawn
        // at its last known location.
        // @tags
        // <NPCTag.is_spawned>
        // -->
        if (mechanism.matches("spawn")) {
            if (mechanism.requireObject("Invalid LocationTag specified. Assuming last known NPC location.", LocationTag.class)) {
                getCitizen().spawn(mechanism.valueAsType(LocationTag.class));
            }
            else {
                getCitizen().spawn(getCitizen().getStoredLocation());
            }
        }

        // <--[mechanism]
        // @object NPCTag
        // @name range
        // @input Element(Decimal)
        // @description
        // Sets the maximum movement distance of the NPC.
        // @tags
        // <NPCTag.navigator.range>
        // -->
        if (mechanism.matches("range") && mechanism.requireFloat()) {
            getCitizen().getNavigator().getDefaultParameters().range(mechanism.getValue().asFloat());
        }

        // <--[mechanism]
        // @object NPCTag
        // @name attack_range
        // @input Element(Decimal)
        // @description
        // Sets the maximum attack distance of the NPC.
        // @tags
        // <NPCTag.navigator.attack_range>
        // -->
        if (mechanism.matches("attack_range") && mechanism.requireFloat()) {
            getCitizen().getNavigator().getDefaultParameters().attackRange(mechanism.getValue().asFloat());
        }

        // <--[mechanism]
        // @object NPCTag
        // @name speed
        // @input Element(Decimal)
        // @description
        // Sets the movement speed of the NPC.
        // @tags
        // <NPCTag.navigator.speed>
        // -->
        if (mechanism.matches("speed") && mechanism.requireFloat()) {
            getCitizen().getNavigator().getDefaultParameters().speedModifier(mechanism.getValue().asFloat());
        }

        // <--[mechanism]
        // @object NPCTag
        // @name despawn
        // @input none
        // @description
        // Despawns the NPC.
        // @tags
        // <NPCTag.is_spawned>
        // -->
        if (mechanism.matches("despawn")) {
            getCitizen().despawn(DespawnReason.PLUGIN);
        }

        // <--[mechanism]
        // @object NPCTag
        // @name set_sneaking
        // @input Element(Boolean)
        // @description
        // Sets whether the NPC is sneaking or not. Only works for player-type NPCs.
        // @tags
        // <NPCTag.is_sneaking>
        // -->
        if (mechanism.matches("set_sneaking") && mechanism.requireBoolean()) {
            if (!getCitizen().hasTrait(SneakingTrait.class)) {
                getCitizen().addTrait(SneakingTrait.class);
            }
            SneakingTrait trait = getCitizen().getTrait(SneakingTrait.class);
            if (trait.isSneaking() && !mechanism.getValue().asBoolean()) {
                trait.sneak();
            }
            else if (!trait.isSneaking() && mechanism.getValue().asBoolean()) {
                trait.stand();
            }
        }

        // <--[mechanism]
        // @object NPCTag
        // @name set_protected
        // @input Element(Boolean)
        // @description
        // Sets whether or not the NPC is protected.
        // @tags
        // <NPCTag.is_protected>
        // -->
        if (mechanism.matches("set_protected") && mechanism.requireBoolean()) {
            getCitizen().setProtected(mechanism.getValue().asBoolean());
        }

        // <--[mechanism]
        // @object NPCTag
        // @name lookclose
        // @input Element(Boolean)
        // @description
        // Sets the NPC's lookclose value.
        // @tags
        // <NPCTag.lookclose>
        // -->
        if (mechanism.matches("lookclose") && mechanism.requireBoolean()) {
            getLookCloseTrait().lookClose(mechanism.getValue().asBoolean());
        }

        // <--[mechanism]
        // @object NPCTag
        // @name teleport_on_stuck
        // @input Element(Boolean)
        // @description
        // Sets whether the NPC teleports when it is stuck.
        // @tags
        // <NPCTag.teleport_on_stuck>
        // -->
        if (mechanism.matches("teleport_on_stuck") && mechanism.requireBoolean()) {
            if (mechanism.getValue().asBoolean()) {
                getNavigator().getDefaultParameters().stuckAction(TeleportStuckAction.INSTANCE);
            }
            else {
                getNavigator().getDefaultParameters().stuckAction(null);
            }
        }

        // <--[mechanism]
        // @object NPCTag
        // @name distance_margin
        // @input Element(Decimal)
        // @description
        // Sets the NPC's distance margin.
        // @tags
        // <NPCTag.distance_margin>
        // -->
        if ((mechanism.matches("distance_margin") || mechanism.matches("set_distance")) && mechanism.requireDouble()) {
            getNavigator().getDefaultParameters().distanceMargin(mechanism.getValue().asDouble());
        }

        // <--[mechanism]
        // @object NPCTag
        // @name path_distance_margin
        // @input Element(Decimal)
        // @description
        // Sets the NPC's path distance margin.
        // @tags
        // <NPCTag.path_distance_margin>
        // -->
        if (mechanism.matches("path_distance_margin") && mechanism.requireDouble()) {
            getNavigator().getDefaultParameters().pathDistanceMargin(mechanism.getValue().asDouble());
        }

        // <--[mechanism]
        // @object NPCTag
        // @name name_visible
        // @input Element
        // @description
        // Sets whether the NPC's nameplate is visible. Input is 'true' (always visible), 'false' (never visible), or 'hover' (only visible while looking at the NPC).
        // @tags
        // TODO
        // -->
        if (mechanism.matches("name_visible")) {
            getCitizen().data().setPersistent(NPC.NAMEPLATE_VISIBLE_METADATA, mechanism.getValue().asString());
        }

        // <--[mechanism]
        // @object NPCTag
        // @name clear_waypoints
        // @input None
        // @description
        // Clears all waypoint locations in the NPC's path.
        // @tags
        // TODO
        // -->
        if (mechanism.matches("clear_waypoints")) {
            if (!getCitizen().hasTrait(Waypoints.class)) {
                getCitizen().addTrait(Waypoints.class);
            }
            Waypoints wp = getCitizen().getTrait(Waypoints.class);
            if ((wp.getCurrentProvider() instanceof WaypointProvider.EnumerableWaypointProvider)) {
                ((List<Waypoint>) ((WaypointProvider.EnumerableWaypointProvider) wp.getCurrentProvider()).waypoints()).clear();
            }
            else if ((wp.getCurrentProvider() instanceof WanderWaypointProvider)) {
                List<Location> locs = ((WanderWaypointProvider) wp.getCurrentProvider()).getRegionCentres();
                for (Location loc : locs) {
                    locs.remove(loc); // Manual clear to ensure recalculation for the forwarding list
                }

            }
        }

        // <--[mechanism]
        // @object NPCTag
        // @name add_waypoint
        // @input LocationTag
        // @description
        // Add a waypoint location to the NPC's path.
        // @tags
        // TODO
        // -->
        if (mechanism.matches("add_waypoint") && mechanism.requireObject(LocationTag.class)) {
            if (!getCitizen().hasTrait(Waypoints.class)) {
                getCitizen().addTrait(Waypoints.class);
            }
            Waypoints wp = getCitizen().getTrait(Waypoints.class);
            if ((wp.getCurrentProvider() instanceof WaypointProvider.EnumerableWaypointProvider)) {
                ((List<Waypoint>) ((WaypointProvider.EnumerableWaypointProvider) wp.getCurrentProvider()).waypoints())
                        .add(new Waypoint(mechanism.valueAsType(LocationTag.class)));
            }
            else if ((wp.getCurrentProvider() instanceof WanderWaypointProvider)) {
                ((WanderWaypointProvider) wp.getCurrentProvider()).getRegionCentres()
                        .add(mechanism.valueAsType(LocationTag.class));
            }
        }

        CoreUtilities.autoPropertyMechanism(this, mechanism);

        // Pass along to EntityTag mechanism handler if not already handled.
        if (!mechanism.fulfilled()) {
            if (isSpawned()) {
                new EntityTag(getEntity()).adjust(mechanism);
            }
        }
    }
}

package com.denizenscript.denizen.objects;

import com.denizenscript.denizen.Denizen;
import com.denizenscript.denizen.npc.traits.*;
import com.denizenscript.denizen.scripts.commands.npc.EngageCommand;
import com.denizenscript.denizen.scripts.containers.core.InteractScriptContainer;
import com.denizenscript.denizen.scripts.containers.core.InteractScriptHelper;
import com.denizenscript.denizen.scripts.triggers.AbstractTrigger;
import com.denizenscript.denizencore.flags.AbstractFlagTracker;
import com.denizenscript.denizencore.flags.FlaggableObject;
import com.denizenscript.denizencore.tags.ObjectTagProcessor;
import com.denizenscript.denizencore.tags.TagRunnable;
import com.denizenscript.denizencore.utilities.Deprecations;
import com.denizenscript.denizencore.utilities.ReflectionHelper;
import com.denizenscript.denizencore.utilities.debugging.Debug;
import com.denizenscript.denizencore.objects.*;
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
import net.citizensnpcs.api.npc.NPCRegistry;
import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.api.trait.trait.Equipment;
import net.citizensnpcs.api.trait.trait.Owner;
import net.citizensnpcs.npc.ai.NPCHolder;
import net.citizensnpcs.npc.skin.SkinnableEntity;
import net.citizensnpcs.trait.*;
import net.citizensnpcs.trait.waypoint.*;
import net.citizensnpcs.util.Anchor;
import net.citizensnpcs.util.Pose;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.*;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class NPCTag implements ObjectTag, Adjustable, InventoryHolder, EntityFormObject, FlaggableObject {

    // <--[language]
    // @name NPCTag Objects
    // @group Object System
    // @description
    // An NPCTag represents an NPC configured through Citizens.
    //
    // These use the object notation "n@".
    // The identity format for NPCs is the NPC's id number.
    // For example, 'n@5'.
    //
    // -->

    public static NPCRegistry getRegistryByName(String name) {
        NPCRegistry registry = CitizensAPI.getNamedNPCRegistry(name);
        if (registry != null) {
            return registry;
        }
        for (NPCRegistry possible : CitizensAPI.getNPCRegistries()) {
            if (possible.getName().equals(name)) {
                return possible;
            }
        }
        return null;
    }

    public static NPCTag fromEntity(Entity entity) {
        return new NPCTag(((NPCHolder) entity).getNPC());
    }

    @Deprecated
    public static NPCTag valueOf(String string) {
        return valueOf(string, null);
    }

    @Fetchable("n")
    public static NPCTag valueOf(String string, TagContext context) {
        if (string == null) {
            return null;
        }
        if (string.startsWith("n@")) {
            string = string.substring("n@".length());
        }
        NPCRegistry registry;
        int commaIndex = string.indexOf(',');
        String idText = string;
        if (commaIndex == -1) {
            registry = CitizensAPI.getNPCRegistry();
        }
        else {
            registry = getRegistryByName(string.substring(commaIndex + 1));
            if (registry == null) {
                if (context == null || context.showErrors()) {
                    Debug.echoError("Unknown NPC registry for '" + string + "'.");
                }
                return null;
            }
            idText = string.substring(0, commaIndex);
        }
        if (ArgumentHelper.matchesInteger(idText)) {
            int id = Integer.parseInt(idText);
            NPC npc = registry.getById(id);
            if (npc != null) {
                return new NPCTag(npc);
            }
            else if (context == null || context.showErrors()) {
                Debug.echoError("NPC '" + id + "' does not exist in " + registry.getName() + ".");
            }
        }
        return null;
    }

    public static boolean matches(String string) {
        if (CoreUtilities.toLowerCase(string).startsWith("n@")) {
            return true;
        }
        if (valueOf(string, CoreUtilities.noDebugContext) != null) {
            return true;
        }
        return false;
    }

    public boolean isValid() {
        return npc != null && npc.getOwningRegistry().getById(npc.getId()) != null;
    }

    @Override
    public AbstractFlagTracker getFlagTracker() {
        return npc.getOrAddTrait(DenizenFlagsTrait.class).fullFlagData;
    }

    public boolean hasFlag(String flag) {
        DenizenFlagsTrait flagTrait = npc.getTraitNullable(DenizenFlagsTrait.class);
        if (flagTrait == null) {
            return false;
        }
        return flagTrait.fullFlagData.hasFlag(flag);
    }

    @Override
    public void reapplyTracker(AbstractFlagTracker tracker) {
        // Nothing to do.
    }

    public NPC npc;

    public NPCTag(NPC citizensNPC) {
        this.npc = citizensNPC;
    }

    public NPC getCitizen() {
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
        return new InventoryTag(getInventory(), this);
    }

    public EntityType getEntityType() {
        return getCitizen().getEntity().getType();
    }

    public Navigator getNavigator() {
        return getCitizen().getNavigator();
    }

    public int getId() {
        return npc.getId();
    }

    public String getName() {
        return getCitizen().getName();
    }

    public InteractScriptContainer getInteractScript() {
        return InteractScriptHelper.getInteractScript(this);
    }

    public InteractScriptContainer getInteractScript(PlayerTag player, Class<? extends AbstractTrigger> triggerType) {
        return InteractScriptHelper.getInteractScript(this, player, true, triggerType);
    }

    public InteractScriptContainer getInteractScriptQuietly(PlayerTag player, Class<? extends AbstractTrigger> triggerType) {
        return InteractScriptHelper.getInteractScript(this, player, false, triggerType);
    }

    public void destroy() {
        getCitizen().destroy();
    }

    @Override
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
        return npc.isSpawned();
    }

    public String getOwner() {
        Owner trait = getCitizen().getOrAddTrait(Owner.class);
        if (trait.getOwnerId() == null) {
            return trait.getOwner();
        }
        return trait.getOwnerId().toString();
    }

    public AssignmentTrait getAssignmentTrait() {
        return getCitizen().getOrAddTrait(AssignmentTrait.class);
    }

    public Equipment getEquipmentTrait() {
        return getCitizen().getOrAddTrait(Equipment.class);
    }

    public NicknameTrait getNicknameTrait() {
        return getCitizen().getOrAddTrait(NicknameTrait.class);
    }

    public FishingTrait getFishingTrait() {
        return getCitizen().getOrAddTrait(FishingTrait.class);
    }

    public net.citizensnpcs.api.trait.trait.Inventory getInventoryTrait() {
        return getCitizen().getOrAddTrait(net.citizensnpcs.api.trait.trait.Inventory.class);
    }

    public PushableTrait getPushableTrait() {
        return getCitizen().getOrAddTrait(PushableTrait.class);
    }

    public LookClose getLookCloseTrait() {
        return getCitizen().getOrAddTrait(LookClose.class);
    }

    public TriggerTrait getTriggerTrait() {
        return getCitizen().getOrAddTrait(TriggerTrait.class);
    }

    public String action(String actionName, PlayerTag player, Map<String, ObjectTag> context) {
        if (getCitizen() != null) {
            if (getCitizen().hasTrait(AssignmentTrait.class)) {
                return Denizen.getInstance().getNPCHelper().getActionHandler().doAction(actionName, this, player, getAssignmentTrait().getAssignment(), context);
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
        if (npc.getOwningRegistry() == CitizensAPI.getNPCRegistry()) {
            return "n@" + npc.getId() + "<GR> (" + getName() + ")";
        }
        else {
            return "n@" + npc.getId() + "<G>," + npc.getOwningRegistry().getName() + "<GR> (" + getName() + ")";
        }
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
        if (npc.getOwningRegistry() == CitizensAPI.getNPCRegistry()) {
            return "n@" + npc.getId();
        }
        else {
            return "n@" + npc.getId() + "," + npc.getOwningRegistry().getName();
        }
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

        AbstractFlagTracker.registerFlagHandlers(tagProcessor);

        // Defined in EntityTag
        registerTag("is_npc", (attribute, object) -> {
            return new ElementTag(true);
        });

        // Defined in EntityTag
        registerTag("location", (attribute, object) -> {
            if (attribute.startsWith("previous_location", 2)) {
                attribute.fulfill(1);
                Deprecations.npcPreviousLocationTag.warn(attribute.context);
                return NPCTagBase.previousLocations.get(object.getId());
            }
            if (object.isSpawned()) {
                return new EntityTag(object).getObjectAttribute(attribute);
            }
            return object.getLocation();
        });

        // <--[tag]
        // @attribute <NPCTag.previous_location>
        // @returns LocationTag
        // @description
        // Returns the NPC's previous navigated location.
        // -->
        registerTag("previous_location", (attribute, object) -> {
            return NPCTagBase.previousLocations.get(object.getId());
        });

        // Defined in EntityTag
        registerTag("eye_location", (attribute, object) -> {
            return object.getEyeLocation();
        });

        // <--[tag]
        // @attribute <NPCTag.has_nickname>
        // @returns ElementTag(Boolean)
        // @description
        // Returns true if the NPC has a nickname.
        // -->
        registerTag("has_nickname", (attribute, object) -> {
            NPC citizen = object.getCitizen();
            return new ElementTag(citizen.hasTrait(NicknameTrait.class) && citizen.getOrAddTrait(NicknameTrait.class).hasNickname());
        });

        // <--[tag]
        // @attribute <NPCTag.is_sitting>
        // @returns ElementTag(Boolean)
        // @description
        // Returns true if the NPC is sitting. Relates to <@link command sit>.
        // -->
        registerTag("is_sitting", (attribute, object) -> {
            NPC citizen = object.getCitizen();
            return new ElementTag(citizen.hasTrait(SittingTrait.class) && citizen.getOrAddTrait(SittingTrait.class).isSitting());
        });

        // <--[tag]
        // @attribute <NPCTag.is_sleeping>
        // @returns ElementTag(Boolean)
        // @description
        // Returns true if the NPC is sleeping. Relates to <@link command sleep>.
        // -->
        registerTag("is_sleeping", (attribute, object) -> {
            NPC citizen = object.getCitizen();
            return new ElementTag(citizen.hasTrait(SleepingTrait.class) && citizen.getOrAddTrait(SleepingTrait.class).isSleeping());
        });

        // <--[tag]
        // @attribute <NPCTag.nickname>
        // @returns ElementTag
        // @description
        // Returns the NPC's display name, as set by the Nickname trait (or the default NPC name).
        // -->
        registerTag("nickname", (attribute, object) -> {
            return new ElementTag(object.getCitizen().hasTrait(NicknameTrait.class) ? object.getCitizen().getOrAddTrait(NicknameTrait.class)
                    .getNickname() : object.getName());
        });

        // Documented in EntityTag
        registerTag("name", (attribute, object) -> {
            if (attribute.startsWith("nickname", 2)) {
                Deprecations.npcNicknameTag.warn(attribute.context);
                attribute.fulfill(1);
                return new ElementTag(object.getCitizen().hasTrait(NicknameTrait.class) ? object.getCitizen().getOrAddTrait(NicknameTrait.class)
                        .getNickname() : object.getName());
            }
            return new ElementTag(object.getName());
        });

        // <--[tag]
        // @attribute <NPCTag.traits>
        // @returns ListTag
        // @description
        // Returns a list of all of the NPC's traits.
        // -->
        registerTag("traits", (attribute, object) -> {
            List<String> list = new ArrayList<>();
            for (Trait trait : object.getCitizen().getTraits()) {
                list.add(trait.getName());
            }
            return new ListTag(list);
        }, "list_traits");

        // <--[tag]
        // @attribute <NPCTag.has_trait[<trait>]>
        // @returns ElementTag(Boolean)
        // @description
        // Returns whether the NPC has a specified trait.
        // -->
        registerTag("has_trait", (attribute, object) -> {
            if (attribute.hasContext(1)) {
                Class<? extends Trait> trait = CitizensAPI.getTraitFactory().getTraitClass(attribute.getContext(1));
                if (trait != null) {
                    return new ElementTag(object.getCitizen().hasTrait(trait));
                }
            }
            return null;
        });

        // <--[tag]
        // @attribute <NPCTag.pushable>
        // @returns ElementTag(Boolean)
        // @description
        // Returns whether the NPC is pushable.
        // -->
        registerTag("pushable", (attribute, object) -> {
            return new ElementTag(object.getPushableTrait().isPushable());
        }, "is_pushable");

        // <--[tag]
        // @attribute <NPCTag.has_trigger[<trigger>]>
        // @returns ElementTag(Boolean)
        // @description
        // Returns whether the NPC has a specified trigger.
        // -->
        registerTag("has_trigger", (attribute, object) -> {
            if (!attribute.hasContext(1)) {
                return null;
            }
            if (!object.getCitizen().hasTrait(TriggerTrait.class)) {
                return new ElementTag(false);
            }
            TriggerTrait trait = object.getCitizen().getOrAddTrait(TriggerTrait.class);
            return new ElementTag(trait.hasTrigger(attribute.getContext(1)));
        });

        // <--[tag]
        // @attribute <NPCTag.has_anchors>
        // @returns ElementTag(Boolean)
        // @description
        // Returns whether the NPC has anchors assigned.
        // -->
        registerTag("has_anchors", (attribute, object) -> {
            return (new ElementTag(object.getCitizen().getOrAddTrait(Anchors.class).getAnchors().size() > 0));
        });

        // <--[tag]
        // @attribute <NPCTag.list_anchors>
        // @returns ListTag
        // @description
        // Returns a list of anchor names currently assigned to the NPC.
        // -->
        registerTag("list_anchors", (attribute, object) -> {
            ListTag list = new ListTag();
            for (Anchor anchor : object.getCitizen().getOrAddTrait(Anchors.class).getAnchors()) {
                list.add(anchor.getName());
            }
            return list;
        });

        // <--[tag]
        // @attribute <NPCTag.anchor[<name>]>
        // @returns LocationTag
        // @description
        // Returns the location associated with the specified anchor, or null if it doesn't exist.
        // -->
        registerTag("anchor", (attribute, object) -> {
            Anchors trait = object.getCitizen().getOrAddTrait(Anchors.class);
            if (attribute.hasContext(1)) {
                Anchor anchor = trait.getAnchor(attribute.getContext(1));
                    if (anchor != null) {
                        return new LocationTag(anchor.getLocation());
                    }
                    else {
                        attribute.echoError("NPC Anchor '" + attribute.getContext(1) + "' is not defined.");
                        return null;
                    }
            }
            else if (attribute.startsWith("list", 2)) {
                attribute.fulfill(1);
                Deprecations.npcAnchorListTag.warn(attribute.context);
                ListTag list = new ListTag();
                for (Anchor anchor : trait.getAnchors()) {
                    list.add(anchor.getName());
                }
                return list;
            }
            else {
                attribute.echoError("npc.anchor[...] tag must have an input.");
            }
            return null;
        }, "anchors");

        // <--[tag]
        // @attribute <NPCTag.constant[<constant_name>]>
        // @returns ElementTag
        // @description
        // Returns the specified constant from the NPC.
        // -->
        registerTag("constant", (attribute, object) -> {
            if (attribute.hasContext(1)) {
                if (object.getCitizen().hasTrait(ConstantsTrait.class)
                        && object.getCitizen().getOrAddTrait(ConstantsTrait.class).getConstant(attribute.getContext(1)) != null) {
                    return new ElementTag(object.getCitizen().getOrAddTrait(ConstantsTrait.class)
                            .getConstant(attribute.getContext(1)));
                }
                else {
                    return null;
                }
            }
            return null;
        });

        // <--[tag]
        // @attribute <NPCTag.has_pose[<name>]>
        // @returns ElementTag(Boolean)
        // @description
        // Returns true if the NPC has the specified pose, otherwise returns false.
        // -->
        registerTag("has_pose", (attribute, object) -> {
            if (attribute.hasContext(1)) {
                return new ElementTag(object.getCitizen().getOrAddTrait(Poses.class).hasPose(attribute.getContext(1)));
            }
            else {
                return null;
            }
        });

        // <--[tag]
        // @attribute <NPCTag.pose[<name>]>
        // @returns LocationTag
        // @description
        // Returns the pose as a LocationTag with x, y, and z set to 0, and the world set to the first
        // possible available world Bukkit knows about.
        // -->
        registerTag("pose", (attribute, object) -> {
            if (attribute.hasContext(1)) {
                Pose pose = object.getCitizen().getOrAddTrait(Poses.class).getPose(attribute.getContext(1));
                return new LocationTag(org.bukkit.Bukkit.getWorlds().get(0), 0, 0, 0, pose.getYaw(), pose.getPitch());
            }
            else {
                return null;
            }
        }, "get_pose");

        // <--[tag]
        // @attribute <NPCTag.name_hologram_npc>
        // @returns NPCTag
        // @description
        // Returns the NPCTag of a hologram attached to this NPC as its nameplate (if any).
        // Note that this can regenerate at any time.
        // -->
        registerTag("name_hologram_npc", (attribute, object) -> {
            if (!object.getCitizen().hasTrait(HologramTrait.class)) {
                return null;
            }
            HologramTrait hologram = object.getCitizen().getTraitNullable(HologramTrait.class);
            NPC npc = ReflectionHelper.getFieldValue(HologramTrait.class, "nameNPC", hologram);
            if (npc == null) {
                return null;
            }
            return new NPCTag(npc);
        });

        // <--[tag]
        // @attribute <NPCTag.hologram_npcs>
        // @returns ListTag(NPCTag)
        // @description
        // Returns the list of hologram NPCs attached to an NPC (if any).
        // Note that these can regenerate at any time.
        // -->
        registerTag("hologram_npcs", (attribute, object) -> {
            if (!object.getCitizen().hasTrait(HologramTrait.class)) {
                return null;
            }
            HologramTrait hologram = object.getCitizen().getTraitNullable(HologramTrait.class);
            List<NPC> npcs = ReflectionHelper.getFieldValue(HologramTrait.class, "hologramNPCs", hologram);
            if (npcs == null || npcs.isEmpty()) {
                return null;
            }
            ListTag output = new ListTag();
            for (NPC npc : npcs) {
                output.addObject(new NPCTag(npc));
            }
            return output;
        });

        // <--[tag]
        // @attribute <NPCTag.hologram_lines>
        // @returns ListTag
        // @mechanism NPCTag.hologram_lines
        // @description
        // Returns the list of hologram lines attached to an NPC.
        // -->
        registerTag("hologram_lines", (attribute, object) -> {
            if (!object.getCitizen().hasTrait(HologramTrait.class)) {
                return null;
            }
            HologramTrait hologram = object.getCitizen().getTraitNullable(HologramTrait.class);
            return new ListTag(hologram.getLines());
        });

        // <--[tag]
        // @attribute <NPCTag.hologram_direction>
        // @returns ElementTag
        // @mechanism NPCTag.hologram_direction
        // @description
        // Returns the direction of an NPC's hologram as "BOTTOM_UP" or "TOP_DOWN".
        // -->
        registerTag("hologram_direction", (attribute, object) -> {
            if (!object.getCitizen().hasTrait(HologramTrait.class)) {
                return null;
            }
            HologramTrait hologram = object.getCitizen().getTraitNullable(HologramTrait.class);
            return new ElementTag(hologram.getDirection().name());
        });

        // <--[tag]
        // @attribute <NPCTag.hologram_line_height>
        // @returns ElementTag(Decimal)
        // @mechanism NPCTag.hologram_line_height
        // @description
        // Returns the line height for an NPC's hologram. Can be -1, indicating a default value should be used.
        // -->
        registerTag("hologram_line_height", (attribute, object) -> {
            if (!object.getCitizen().hasTrait(HologramTrait.class)) {
                return null;
            }
            HologramTrait hologram = object.getCitizen().getTraitNullable(HologramTrait.class);
            return new ElementTag(hologram.getLineHeight());
        });

        // <--[tag]
        // @attribute <NPCTag.is_sneaking>
        // @returns ElementTag(Boolean)
        // @description
        // Returns whether the NPC is currently sneaking. Only works for player-type NPCs.
        // -->
        registerTag("is_sneaking", (attribute, object) -> {
            if (!object.isSpawned() && object.getEntity() instanceof Player) {
                return null;
            }
            return new ElementTag(((Player) object.getEntity()).isSneaking());
        });

        // <--[tag]
        // @attribute <NPCTag.engaged>
        // @returns ElementTag(Boolean)
        // @description
        // Returns whether the NPC is currently engaged.
        // See <@link command engage>
        // -->
        registerTag("engaged", (attribute, object) -> {
            return new ElementTag(object.isEngaged());
        }, "is_engaged");

        // <--[tag]
        // @attribute <NPCTag.invulnerable>
        // @returns ElementTag(Boolean)
        // @description
        // Returns whether the NPC is currently invulnerable.
        // See <@link command vulnerable>
        // -->
        registerTag("invulnerable", (attribute, object) -> {
            return new ElementTag(object.getCitizen().data().get(NPC.DEFAULT_PROTECTED_METADATA, true));
        }, "vulnerable");

        // <--[tag]
        // @attribute <NPCTag.id>
        // @returns ElementTag(Number)
        // @description
        // Returns the NPC's ID number.
        // -->
        registerTag("id", (attribute, object) -> {
            return new ElementTag(object.getId());
        });

        // <--[tag]
        // @attribute <NPCTag.owner>
        // @returns PlayerTag/ElementTag
        // @mechanism NPCTag.owner
        // @description
        // Returns the owner of the NPC as a PlayerTag if it's a player, otherwise as just the name.
        // -->
        registerTag("owner", (attribute, object) -> {
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
        });

        // <--[tag]
        // @attribute <NPCTag.has_skin>
        // @returns ElementTag(Boolean)
        // @mechanism NPCTag.skin
        // @description
        // Returns whether the NPC has a custom skin.
        // -->
        registerTag("has_skin", (attribute, object) -> {
            return new ElementTag(object.getCitizen().hasTrait(SkinTrait.class) && object.getCitizen().getOrAddTrait(SkinTrait.class).getSkinName() != null);
        });

        // <--[tag]
        // @attribute <NPCTag.skin_blob>
        // @returns ElementTag
        // @mechanism NPCTag.skin_blob
        // @description
        // Returns the NPC's custom skin blob, if any.
        // In the format: "texture;signature" (two values separated by a semicolon).
        // See also <@link language Player Entity Skins (Skin Blobs)>.
        // -->
        registerTag("skin_blob", (attribute, object) -> {
            if (object.getCitizen().hasTrait(SkinTrait.class)) {
                SkinTrait skin = object.getCitizen().getOrAddTrait(SkinTrait.class);
                String tex = skin.getTexture();
                String sign = "";
                if (skin.getSignature() != null) {
                    sign = ";" + skin.getSignature();
                }
                return new ElementTag(tex + sign);
            }
            return null;
        });

        // <--[tag]
        // @attribute <NPCTag.skull_skin>
        // @returns ElementTag
        // @description
        // Returns the NPC's current skin blob, formatted for input to a Player Skull item.
        // In the format: "UUID|Texture" (two values separated by pipes).
        // See also <@link language Player Entity Skins (Skin Blobs)>.
        // -->
        registerTag("skull_skin", (attribute, object) -> {
            if (!object.getCitizen().hasTrait(SkinTrait.class)) {
                return null;
            }
            SkinTrait skin = object.getCitizen().getOrAddTrait(SkinTrait.class);
            return new ElementTag(skin.getSkinName() + "|" + skin.getTexture());
        });

        // <--[tag]
        // @attribute <NPCTag.skin>
        // @returns ElementTag
        // @mechanism NPCTag.skin
        // @description
        // Returns the NPC's custom skin, if any.
        // -->
        registerTag("skin", (attribute, object) -> {
            if (object.getCitizen().hasTrait(SkinTrait.class)) {
                return new ElementTag(object.getCitizen().getOrAddTrait(SkinTrait.class).getSkinName());
            }
            return null;
        });

        // <--[tag]
        // @attribute <NPCTag.auto_update_skin>
        // @returns ElementTag(Boolean)
        // @mechanism NPCTag.auto_update_skin
        // @description
        // Returns whether the NPC is set to automatically update skins from name.
        // -->
        registerTag("auto_update_skin", (attribute, object) -> {
            if (object.getCitizen().hasTrait(SkinTrait.class)) {
                return new ElementTag(object.getCitizen().getOrAddTrait(SkinTrait.class).shouldUpdateSkins());
            }
            return null;
        });

        // <--[tag]
        // @attribute <NPCTag.inventory>
        // @returns InventoryTag
        // @description
        // Returns the InventoryTag of the NPC.
        // -->
        registerTag("inventory", (attribute, object) -> {
            return object.getDenizenInventory();
        });

        // <--[tag]
        // @attribute <NPCTag.is_spawned>
        // @returns ElementTag(Boolean)
        // @description
        // Returns whether the NPC is spawned.
        // -->
        registerTag("is_spawned", (attribute, object) -> {
            return new ElementTag(object.isSpawned());
        });

        // <--[tag]
        // @attribute <NPCTag.is_protected>
        // @returns ElementTag(Boolean)
        // @description
        // Returns whether the NPC is protected.
        // -->
        registerTag("is_protected", (attribute, object) -> {
            return new ElementTag(object.getCitizen().isProtected());
        });

        // <--[tag]
        // @attribute <NPCTag.lookclose>
        // @returns ElementTag(Boolean)
        // @mechanism NPCTag.lookclose
        // @description
        // Returns whether the NPC has lookclose enabled.
        // -->
        registerTag("lookclose", (attribute, object) -> {
            NPC citizen = object.getCitizen();
            if (citizen.hasTrait(LookClose.class)) {
                // There is no method to check if the NPC has LookClose enabled...
                // LookClose.toString() returns "LookClose{" + enabled + "}"
                String lookclose = citizen.getOrAddTrait(LookClose.class).toString();
                lookclose = lookclose.substring(10, lookclose.length() - 1);
                return new ElementTag(Boolean.valueOf(lookclose));
            }
            return new ElementTag(false);
        });

        // <--[tag]
        // @attribute <NPCTag.controllable>
        // @returns ElementTag(Boolean)
        // @mechanism NPCTag.controllable
        // @description
        // Returns whether the NPC has controllable enabled.
        // -->
        registerTag("controllable", (attribute, object) -> {
            if (object.getCitizen().hasTrait(Controllable.class)) {
                return new ElementTag(object.getCitizen().getOrAddTrait(Controllable.class).isEnabled());
            }
            return new ElementTag(false);
        });

        // <--[tag]
        // @attribute <NPCTag.targetable>
        // @returns ElementTag(Boolean)
        // @mechanism NPCTag.targetable
        // @description
        // Returns whether the NPC is targetable.
        // -->
        registerTag("targetable", (attribute, object) -> {
            boolean targetable = object.getCitizen().data().get(NPC.TARGETABLE_METADATA, object.getCitizen().data().get(NPC.DEFAULT_PROTECTED_METADATA, true));
            return new ElementTag(targetable);
        });

        // <--[tag]
        // @attribute <NPCTag.teleport_on_stuck>
        // @returns ElementTag(Boolean)
        // @mechanism NPCTag.teleport_on_stuck
        // @description
        // Returns whether the NPC teleports when it is stuck.
        // -->
        registerTag("teleport_on_stuck", (attribute, object) -> {
            return new ElementTag(object.getNavigator().getDefaultParameters().stuckAction() == TeleportStuckAction.INSTANCE);
        });

        registerTag("has_script", (attribute, object) -> {
            Deprecations.hasScriptTags.warn(attribute.context);
            NPC citizen = object.getCitizen();
            return new ElementTag(citizen.hasTrait(AssignmentTrait.class) && citizen.getOrAddTrait(AssignmentTrait.class).hasAssignment());
        });

        // <--[tag]
        // @attribute <NPCTag.script>
        // @returns ScriptTag
        // @description
        // Returns the NPC's assigned script.
        // -->
        registerTag("script", (attribute, object) -> {
            NPC citizen = object.getCitizen();
            if (!citizen.hasTrait(AssignmentTrait.class) || !citizen.getOrAddTrait(AssignmentTrait.class).hasAssignment()) {
                return null;
            }
            else {
                return new ScriptTag(citizen.getOrAddTrait(AssignmentTrait.class).getAssignment().getName());
            }
        });

        // <--[tag]
        // @attribute <NPCTag.distance_margin>
        // @returns ElementTag(Decimal)
        // @mechanism NPCTag.distance_margin
        // @description
        // Returns the NPC's current pathfinding distance margin. That is, how close it needs to get to its destination (in block-lengths).
        // -->
        registerTag("distance_margin", (attribute, object) -> {
            return new ElementTag(object.getNavigator().getDefaultParameters().distanceMargin());
        });

        // <--[tag]
        // @attribute <NPCTag.path_distance_margin>
        // @returns ElementTag(Decimal)
        // @mechanism NPCTag.path_distance_margin
        // @description
        // Returns the NPC's current pathfinding distance margin. That is, how close it needs to get to individual points along its path.
        // -->
        registerTag("path_distance_margin", (attribute, object) -> {
            return new ElementTag(object.getNavigator().getDefaultParameters().pathDistanceMargin());
        });

        // <--[tag]
        // @attribute <NPCTag.is_navigating>
        // @returns ElementTag(Boolean)
        // @description
        // Returns whether the NPC is currently navigating.
        // -->
        registerTag("is_navigating", (attribute, object) -> {
            return new ElementTag(object.getNavigator().isNavigating());
        });

        // <--[tag]
        // @attribute <NPCTag.speed>
        // @returns ElementTag(Decimal)
        // @mechanism NPCTag.speed
        // @description
        // Returns the current speed of the NPC.
        // -->
        registerTag("speed", (attribute, object) -> {
            return new ElementTag(object.getNavigator().getLocalParameters().speed());
        });

        // <--[tag]
        // @attribute <NPCTag.range>
        // @returns ElementTag(Decimal)
        // @mechanism NPCTag.range
        // @description
        // Returns the NPC's current maximum pathfinding range.
        // -->
        registerTag("range", (attribute, object) -> {
            return new ElementTag(object.getNavigator().getLocalParameters().range());
        });

        // <--[tag]
        // @attribute <NPCTag.attack_range>
        // @returns ElementTag(Decimal)
        // @mechanism NPCTag.attack_range
        // @description
        // Returns the NPC's current navigator attack range limit.
        // -->
        registerTag("attack_range", (attribute, object) -> {
            return new ElementTag(object.getNavigator().getLocalParameters().attackRange());
        });

        // <--[tag]
        // @attribute <NPCTag.attack_strategy>
        // @returns ElementTag
        // @description
        // Returns the NPC's current navigator attack strategy.
        // Not related to Sentinel combat.
        // -->
        registerTag("attack_strategy", (attribute, object) -> {
            return new ElementTag(object.getNavigator().getLocalParameters().attackStrategy().toString());
        });

        // <--[tag]
        // @attribute <NPCTag.speed_modifier>
        // @returns ElementTag(Decimal)
        // @description
        // Returns the NPC's current movement speed modifier (a multiplier applied over their base speed).
        // -->
        registerTag("speed_modifier", (attribute, object) -> {
            return new ElementTag(object.getNavigator().getLocalParameters().speedModifier());
        });

        // <--[tag]
        // @attribute <NPCTag.base_speed>
        // @returns ElementTag(Decimal)
        // @description
        // Returns the NPC's base navigation speed.
        // -->
        registerTag("base_speed", (attribute, object) -> {
            return new ElementTag(object.getNavigator().getLocalParameters().baseSpeed());
        });

        // <--[tag]
        // @attribute <NPCTag.avoid_water>
        // @returns ElementTag(Boolean)
        // @description
        // Returns whether the NPC will avoid water.
        // -->
        registerTag("avoid_water", (attribute, object) -> {
            return new ElementTag(object.getNavigator().getLocalParameters().avoidWater());
        });

        // <--[tag]
        // @attribute <NPCTag.target_location>
        // @returns LocationTag
        // @description
        // Returns the location the NPC is currently navigating towards (if any).
        // -->
        registerTag("target_location", (attribute, object) -> {
            if (object.getNavigator().getTargetAsLocation() == null) {
                return null;
            }
            return new LocationTag(object.getNavigator().getTargetAsLocation());
        });

        // <--[tag]
        // @attribute <NPCTag.is_fighting>
        // @returns ElementTag(Boolean)
        // @description
        // Returns whether the NPC is currently targeting an entity for the Citizens internal punching pathfinder.
        // Not compatible with Sentinel.
        // -->
        registerTag("is_fighting", (attribute, object) -> {
            return new ElementTag(object.getNavigator().getEntityTarget() != null && object.getNavigator().getEntityTarget().isAggressive());
        });

        // <--[tag]
        // @attribute <NPCTag.target_type>
        // @returns ElementTag
        // @description
        // Returns the entity type of the NPC's current navigation target (if any).
        // -->
        registerTag("target_type", (attribute, object) -> {
            if (object.getNavigator().getTargetType() == null) {
                return null;
            }
            return new ElementTag(object.getNavigator().getTargetType().toString());
        });

        // <--[tag]
        // @attribute <NPCTag.target_entity>
        // @returns EntityTag
        // @description
        // Returns the entity being targeted by the NPC's current navigation (if any).
        // -->
        registerTag("target_entity", (attribute, object) -> {
            if (object.getNavigator().getEntityTarget() == null || object.getNavigator().getEntityTarget().getTarget() == null) {
                return null;
            }
            return new EntityTag(object.getNavigator().getEntityTarget().getTarget());
        });

        // <--[tag]
        // @attribute <NPCTag.registry_name>
        // @returns ElementTag
        // @description
        // Returns the name of the registry this NPC came from.
        // -->
        registerTag("registry_name", (attribute, object) -> {
            return new ElementTag(object.getCitizen().getOwningRegistry().getName());
        });

        registerTag("navigator", (attribute, object) -> {
            Deprecations.oldNPCNavigator.warn(attribute.context);
            return object;
        });
    }

    public static ObjectTagProcessor<NPCTag> tagProcessor = new ObjectTagProcessor<>();

    public static void registerTag(String name, TagRunnable.ObjectInterface<NPCTag> runnable, String... variants) {
        tagProcessor.registerTag(name, runnable, variants);
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
        // @input None
        // @description
        // Removes the NPC's assigment script.
        // @tags
        // <NPCTag.script>
        // -->
        if (mechanism.matches("remove_assignment")) {
            getAssignmentTrait().removeAssignment(null);
        }

        // <--[mechanism]
        // @object NPCTag
        // @name hologram_lines
        // @input ListTag
        // @description
        // Sets the NPC's hologram line list.
        // @tags
        // <NPCTag.hologram_lines>
        // -->
        if (mechanism.matches("hologram_lines") && mechanism.requireObject(ListTag.class)) {
            HologramTrait hologram = getCitizen().getOrAddTrait(HologramTrait.class);
            hologram.clear();
            for (String str : mechanism.valueAsType(ListTag.class)) {
                hologram.addLine(str);
            }
        }

        // <--[mechanism]
        // @object NPCTag
        // @name hologram_direction
        // @input ElementTag
        // @description
        // Sets the NPC's hologram direction, as either BOTTOM_UP or TOP_DOWN.
        // @tags
        // <NPCTag.hologram_direction>
        // -->
        if (mechanism.matches("hologram_direction") && mechanism.requireEnum(false, HologramTrait.HologramDirection.values())) {
            HologramTrait hologram = getCitizen().getOrAddTrait(HologramTrait.class);
            hologram.setDirection(HologramTrait.HologramDirection.valueOf(mechanism.getValue().asString().toUpperCase()));
        }

        // <--[mechanism]
        // @object NPCTag
        // @name hologram_line_height
        // @input ElementTag(Decimal)
        // @description
        // Sets the NPC's hologram line height. Can be -1 to indicate a default value.
        // @tags
        // <NPCTag.hologram_line_height>
        // -->
        if (mechanism.matches("hologram_line_height") && mechanism.requireDouble()) {
            HologramTrait hologram = getCitizen().getOrAddTrait(HologramTrait.class);
            hologram.setLineHeight(mechanism.getValue().asDouble());
        }

        // <--[mechanism]
        // @object NPCTag
        // @name set_nickname
        // @input ElementTag
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
        // @input None
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
        // @input ElementTag
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
        // @input PlayerTag
        // @description
        // Sets the owner of the NPC.
        // @tags
        // <NPCTag.owner>
        // -->
        if (mechanism.matches("owner")) {
            if (PlayerTag.matches(mechanism.getValue().asString())) {
                getCitizen().getOrAddTrait(Owner.class).setOwner(mechanism.valueAsType(PlayerTag.class).getPlayerEntity());
            }
            else {
                getCitizen().getOrAddTrait(Owner.class).setOwner(mechanism.getValue().asString());
            }
        }

        // <--[mechanism]
        // @object NPCTag
        // @name skin_blob
        // @input ElementTag
        // @description
        // Sets the skin blob of an NPC, in the form of "texture;signature;name".
        // Call with no value to clear the custom skin value.
        // See also <@link language Player Entity Skins (Skin Blobs)>.
        // @tags
        // <NPCTag.skin>
        // -->
        if (mechanism.matches("skin_blob")) {
            if (!mechanism.hasValue()) {
                if (getCitizen().hasTrait(SkinTrait.class)) {
                    getCitizen().getOrAddTrait(SkinTrait.class).clearTexture();
                    if (getCitizen().isSpawned()) {
                        getCitizen().despawn(DespawnReason.PENDING_RESPAWN);
                        getCitizen().spawn(getCitizen().getStoredLocation());
                    }
                }
            }
            else {
                SkinTrait skinTrait = getCitizen().getOrAddTrait(SkinTrait.class);
                String[] dat = mechanism.getValue().asString().split(";");
                if (dat.length < 2) {
                    Debug.echoError("Invalid skin_blob input. Must specify texture;signature;name in full.");
                    return;
                }
                skinTrait.setSkinPersistent(dat.length > 2 ? dat[2] : UUID.randomUUID().toString(), dat[1], dat[0]);
                if (getCitizen().isSpawned() && getCitizen().getEntity() instanceof SkinnableEntity) {
                    ((SkinnableEntity) getCitizen().getEntity()).getSkinTracker().notifySkinChange(true);
                }
            }
        }

        // <--[mechanism]
        // @object NPCTag
        // @name skin
        // @input ElementTag
        // @description
        // Sets the skin of an NPC by name.
        // Call with no value to clear the custom skin value.
        // See also <@link language Player Entity Skins (Skin Blobs)>.
        // @tags
        // <NPCTag.skin>
        // -->
        if (mechanism.matches("skin")) {
            if (!mechanism.hasValue()) {
                if (getCitizen().hasTrait(SkinTrait.class)) {
                    getCitizen().getOrAddTrait(SkinTrait.class).clearTexture();
                }
            }
            else {
                SkinTrait skinTrait = getCitizen().getOrAddTrait(SkinTrait.class);
                skinTrait.setSkinName(mechanism.getValue().asString());
            }
            if (getCitizen().isSpawned()) {
                getCitizen().despawn(DespawnReason.PENDING_RESPAWN);
                getCitizen().spawn(getCitizen().getStoredLocation());
            }
        }

        // <--[mechanism]
        // @object NPCTag
        // @name auto_update_skin
        // @input ElementTag(Boolean)
        // @description
        // Sets whether the NPC will automatically update its skin based on the skin name used.
        // If true, the NPC's skin will change when the relevant account owner changes their skin.
        // @tags
        // <NPCTag.auto_update_skin>
        // -->
        if (mechanism.matches("auto_update_skin") && mechanism.requireBoolean()) {
            getCitizen().getOrAddTrait(SkinTrait.class).setShouldUpdateSkins(mechanism.getValue().asBoolean());
        }

        // <--[mechanism]
        // @object NPCTag
        // @name item_type
        // @input ItemTag
        // @description
        // Sets the item type of the item.
        // -->
        if (mechanism.matches("item_type") && mechanism.requireObject(ItemTag.class)) {
            ItemTag item = mechanism.valueAsType(ItemTag.class);
            Material mat = item.getMaterial().getMaterial();
            switch (getEntity().getType()) {
                case DROPPED_ITEM:
                    ((org.bukkit.entity.Item) getEntity()).getItemStack().setType(mat);
                    break;
                case ITEM_FRAME:
                    ((ItemFrame) getEntity()).getItem().setType(mat);
                    break;
                case FALLING_BLOCK:
                    getCitizen().data().setPersistent(NPC.ITEM_ID_METADATA, mat.name());
                    getCitizen().data().setPersistent(NPC.ITEM_DATA_METADATA, 0);
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

        if (mechanism.matches("spawn")) {
            Deprecations.npcSpawnMechanism.warn(mechanism.context);
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
        // @input ElementTag(Decimal)
        // @description
        // Sets the maximum movement distance of the NPC.
        // @tags
        // <NPCTag.range>
        // -->
        if (mechanism.matches("range") && mechanism.requireFloat()) {
            getCitizen().getNavigator().getDefaultParameters().range(mechanism.getValue().asFloat());
        }

        // <--[mechanism]
        // @object NPCTag
        // @name attack_range
        // @input ElementTag(Decimal)
        // @description
        // Sets the maximum attack distance of the NPC.
        // @tags
        // <NPCTag.attack_range>
        // -->
        if (mechanism.matches("attack_range") && mechanism.requireFloat()) {
            getCitizen().getNavigator().getDefaultParameters().attackRange(mechanism.getValue().asFloat());
        }

        // <--[mechanism]
        // @object NPCTag
        // @name speed
        // @input ElementTag(Decimal)
        // @description
        // Sets the movement speed of the NPC.
        // @tags
        // <NPCTag.speed>
        // -->
        if (mechanism.matches("speed") && mechanism.requireFloat()) {
            getCitizen().getNavigator().getDefaultParameters().speedModifier(mechanism.getValue().asFloat());
        }

        if (mechanism.matches("despawn")) {
            Deprecations.npcDespawnMech.warn(mechanism.context);
            getCitizen().despawn(DespawnReason.PLUGIN);
        }

        // <--[mechanism]
        // @object NPCTag
        // @name set_sneaking
        // @input ElementTag(Boolean)
        // @description
        // Sets whether the NPC is sneaking or not. Only works for player-type NPCs.
        // @tags
        // <NPCTag.is_sneaking>
        // -->
        if (mechanism.matches("set_sneaking") && mechanism.requireBoolean()) {
            if (!getCitizen().hasTrait(SneakingTrait.class)) {
                getCitizen().addTrait(SneakingTrait.class);
            }
            SneakingTrait trait = getCitizen().getOrAddTrait(SneakingTrait.class);
            if (trait.isSneaking() && !mechanism.getValue().asBoolean()) {
                trait.stand();
            }
            else if (!trait.isSneaking() && mechanism.getValue().asBoolean()) {
                trait.sneak();
            }
        }

        // <--[mechanism]
        // @object NPCTag
        // @name set_protected
        // @input ElementTag(Boolean)
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
        // @input ElementTag(Boolean)
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
        // @name controllable
        // @input ElementTag(Boolean)
        // @description
        // Sets whether the NPC is controllable.
        // @tags
        // <NPCTag.controllable>
        // -->
        if (mechanism.matches("controllable") && mechanism.requireBoolean()) {
            getCitizen().getOrAddTrait(Controllable.class).setEnabled(mechanism.getValue().asBoolean());
        }

        // <--[mechanism]
        // @object NPCTag
        // @name targetable
        // @input ElementTag(Boolean)
        // @description
        // Sets whether the NPC is targetable.
        // @tags
        // <NPCTag.targetable>
        // -->
        if (mechanism.matches("targetable") && mechanism.requireBoolean()) {
            getCitizen().data().setPersistent(NPC.TARGETABLE_METADATA, mechanism.getValue().asBoolean());
        }

        // <--[mechanism]
        // @object NPCTag
        // @name teleport_on_stuck
        // @input ElementTag(Boolean)
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
        // @input ElementTag(Decimal)
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
        // @input ElementTag(Decimal)
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
        // @input ElementTag
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
        // @name glow_color
        // @input ElementTag
        // @description
        // Sets the color the NPC will glow with, when it's glowing. Input must be from <@link url https://hub.spigotmc.org/javadocs/spigot/org/bukkit/ChatColor.html>.
        // @tags
        // TODO
        // -->
        if (mechanism.matches("glow_color") && mechanism.requireEnum(false, ChatColor.values())) {
            getCitizen().getOrAddTrait(ScoreboardTrait.class).setColor(ChatColor.valueOf(mechanism.getValue().asString().toUpperCase()));
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
            Waypoints wp = getCitizen().getOrAddTrait(Waypoints.class);
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
            Waypoints wp = getCitizen().getOrAddTrait(Waypoints.class);
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

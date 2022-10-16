package com.denizenscript.denizen.objects;

import com.denizenscript.denizen.Denizen;
import com.denizenscript.denizen.npc.traits.*;
import com.denizenscript.denizen.scripts.commands.npc.EngageCommand;
import com.denizenscript.denizen.scripts.containers.core.AssignmentScriptContainer;
import com.denizenscript.denizen.scripts.containers.core.InteractScriptContainer;
import com.denizenscript.denizen.scripts.containers.core.InteractScriptHelper;
import com.denizenscript.denizen.scripts.triggers.AbstractTrigger;
import com.denizenscript.denizencore.flags.AbstractFlagTracker;
import com.denizenscript.denizencore.flags.FlaggableObject;
import com.denizenscript.denizencore.tags.ObjectTagProcessor;
import com.denizenscript.denizencore.utilities.CoreConfiguration;
import com.denizenscript.denizen.utilities.BukkitImplDeprecations;
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
import net.citizensnpcs.api.util.DataKey;
import net.citizensnpcs.api.util.MemoryDataKey;
import net.citizensnpcs.npc.ai.NPCHolder;
import net.citizensnpcs.npc.skin.SkinnableEntity;
import net.citizensnpcs.trait.*;
import net.citizensnpcs.trait.waypoint.*;
import net.citizensnpcs.util.Anchor;
import net.citizensnpcs.util.Pose;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

import java.util.*;

public class NPCTag implements ObjectTag, Adjustable, InventoryHolder, EntityFormObject, FlaggableObject {

    // <--[ObjectType]
    // @name NPCTag
    // @prefix n
    // @base EntityTag
    // @implements FlaggableObject
    // @ExampleTagBase npc
    // @ExampleValues <npc>
    // @ExampleForReturns
    // - kill %VALUE%
    // @ExampleForReturns
    // - heal %VALUE%
    // @ExampleForReturns
    // - walk %VALUE% <player.location>
    // @format
    // The identity format for NPCs is the NPC's id number.
    // For example, 'n@5'.
    // Or, an NPC's id number, followed by a comma, followed by a custom registry name.
    // For example 'n@12,specialnpcs'
    //
    // @description
    // An NPCTag represents an NPC configured through Citizens.
    //
    // This object type is flaggable.
    // Flags on this object type will be stored in the Citizens saves.yml file, under the 'denizen_flags' trait.
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
            if (CoreConfiguration.debugVerbose) {
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
            if (CoreConfiguration.debugVerbose) {
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
            if (CoreConfiguration.debugVerbose) {
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

    public List<InteractScriptContainer> getInteractScripts() {
        return InteractScriptHelper.getInteractScripts(this);
    }

    public List<InteractScriptContainer> getInteractScripts(PlayerTag player, Class<? extends AbstractTrigger> triggerType) {
        return InteractScriptHelper.getInteractScripts(this, player, true, triggerType);
    }

    public List<InteractScriptContainer> getInteractScriptsQuietly(PlayerTag player, Class<? extends AbstractTrigger> triggerType) {
        return InteractScriptHelper.getInteractScripts(this, player, false, triggerType);
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

    @Override
    public Object getJavaObject() {
        return getCitizen();
    }

    public boolean isSpawned() {
        return npc.isSpawned();
    }

    public UUID getOwner() {
        return getCitizen().getOrAddTrait(Owner.class).getOwnerId();
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

    public ListTag action(String actionName, PlayerTag player, Map<String, ObjectTag> context) {
        ListTag result = new ListTag();
        if (getCitizen() != null) {
            if (getCitizen().hasTrait(AssignmentTrait.class)) {
                for (AssignmentScriptContainer container : getCitizen().getOrAddTrait(AssignmentTrait.class).containerCache) {
                    if (container != null && container.shouldEnable()) {
                        ListTag singleResult = Denizen.getInstance().npcHelper.getActionHandler().doAction(actionName, this, player, container, context);
                        if (singleResult != null) {
                            result.addAll(singleResult);
                        }
                    }
                }
            }
        }
        return result;
    }

    public ListTag action(String actionName, PlayerTag player) {
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
            return "<LG>n@<Y>" + npc.getId() + "<GR> (" + getName() + "<GR>)";
        }
        else {
            return "<LG>n@<Y>" + npc.getId() + "<LG>," + npc.getOwningRegistry().getName() + "<GR> (" + getName() + "<GR>)";
        }
    }

    @Override
    public boolean isUnique() {
        return true;
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
        this.prefix = prefix;
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
        tagProcessor.registerTag(ElementTag.class, "is_npc", (attribute, object) -> {
            return new ElementTag(true);
        });

        // Defined in EntityTag
        tagProcessor.registerTag(ObjectTag.class, "location", (attribute, object) -> {
            if (attribute.startsWith("previous_location", 2)) {
                attribute.fulfill(1);
                BukkitImplDeprecations.npcPreviousLocationTag.warn(attribute.context);
                return NPCTagBase.previousLocations.get(object.getId());
            }
            if (object.isSpawned()) {
                return new EntityTag(object).doLocationTag(attribute);
            }
            return object.getLocation();
        });

        // <--[tag]
        // @attribute <NPCTag.previous_location>
        // @returns LocationTag
        // @description
        // Returns the NPC's previous navigated location.
        // -->
        tagProcessor.registerTag(LocationTag.class, "previous_location", (attribute, object) -> {
            return NPCTagBase.previousLocations.get(object.getId());
        });

        // Defined in EntityTag
        tagProcessor.registerTag(LocationTag.class, "eye_location", (attribute, object) -> {
            return object.getEyeLocation();
        });

        // <--[tag]
        // @attribute <NPCTag.has_nickname>
        // @returns ElementTag(Boolean)
        // @description
        // Returns true if the NPC has a nickname.
        // -->
        tagProcessor.registerTag(ElementTag.class, "has_nickname", (attribute, object) -> {
            NPC citizen = object.getCitizen();
            return new ElementTag(citizen.hasTrait(NicknameTrait.class) && citizen.getOrAddTrait(NicknameTrait.class).hasNickname());
        });

        // <--[tag]
        // @attribute <NPCTag.is_sitting>
        // @returns ElementTag(Boolean)
        // @description
        // Returns true if the NPC is sitting. Relates to <@link command sit>.
        // -->
        tagProcessor.registerTag(ElementTag.class, "is_sitting", (attribute, object) -> {
            NPC citizen = object.getCitizen();
            return new ElementTag(citizen.hasTrait(SittingTrait.class) && citizen.getOrAddTrait(SittingTrait.class).isSitting());
        });

        // <--[tag]
        // @attribute <NPCTag.is_sleeping>
        // @returns ElementTag(Boolean)
        // @description
        // Returns true if the NPC is sleeping. Relates to <@link command sleep>.
        // -->
        tagProcessor.registerTag(ElementTag.class, "is_sleeping", (attribute, object) -> {
            NPC citizen = object.getCitizen();
            return new ElementTag(citizen.hasTrait(SleepingTrait.class) && citizen.getOrAddTrait(SleepingTrait.class).isSleeping());
        });

        // <--[tag]
        // @attribute <NPCTag.nickname>
        // @returns ElementTag
        // @description
        // Returns the NPC's display name, as set by the Nickname trait (or the default NPC name).
        // -->
        tagProcessor.registerTag(ElementTag.class, "nickname", (attribute, object) -> {
            return new ElementTag(object.getCitizen().hasTrait(NicknameTrait.class) ? object.getCitizen().getOrAddTrait(NicknameTrait.class)
                    .getNickname() : object.getName());
        });

        // Documented in EntityTag
        tagProcessor.registerTag(ElementTag.class, "name", (attribute, object) -> {
            if (attribute.startsWith("nickname", 2)) {
                BukkitImplDeprecations.npcNicknameTag.warn(attribute.context);
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
        tagProcessor.registerTag(ListTag.class, "traits", (attribute, object) -> {
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
        tagProcessor.registerTag(ElementTag.class, "has_trait", (attribute, object) -> {
            if (attribute.hasParam()) {
                Class<? extends Trait> trait = CitizensAPI.getTraitFactory().getTraitClass(attribute.getParam());
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
        tagProcessor.registerTag(ElementTag.class, "pushable", (attribute, object) -> {
            return new ElementTag(object.getPushableTrait().isPushable());
        }, "is_pushable");

        // <--[tag]
        // @attribute <NPCTag.has_trigger[<trigger>]>
        // @returns ElementTag(Boolean)
        // @description
        // Returns whether the NPC has a specified trigger.
        // -->
        tagProcessor.registerTag(ElementTag.class, "has_trigger", (attribute, object) -> {
            if (!attribute.hasParam()) {
                return null;
            }
            if (!object.getCitizen().hasTrait(TriggerTrait.class)) {
                return new ElementTag(false);
            }
            TriggerTrait trait = object.getCitizen().getOrAddTrait(TriggerTrait.class);
            return new ElementTag(trait.hasTrigger(attribute.getParam()));
        });

        // <--[tag]
        // @attribute <NPCTag.has_anchors>
        // @returns ElementTag(Boolean)
        // @description
        // Returns whether the NPC has anchors assigned.
        // -->
        tagProcessor.registerTag(ElementTag.class, "has_anchors", (attribute, object) -> {
            return (new ElementTag(object.getCitizen().getOrAddTrait(Anchors.class).getAnchors().size() > 0));
        });

        // <--[tag]
        // @attribute <NPCTag.list_anchors>
        // @returns ListTag
        // @description
        // Returns a list of anchor names currently assigned to the NPC.
        // -->
        tagProcessor.registerTag(ListTag.class, "list_anchors", (attribute, object) -> {
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
        tagProcessor.registerTag(ObjectTag.class, "anchor", (attribute, object) -> {
            Anchors trait = object.getCitizen().getOrAddTrait(Anchors.class);
            if (attribute.hasParam()) {
                Anchor anchor = trait.getAnchor(attribute.getParam());
                    if (anchor != null) {
                        return new LocationTag(anchor.getLocation());
                    }
                    else {
                        attribute.echoError("NPC Anchor '" + attribute.getParam() + "' is not defined.");
                        return null;
                    }
            }
            else if (attribute.startsWith("list", 2)) {
                attribute.fulfill(1);
                BukkitImplDeprecations.npcAnchorListTag.warn(attribute.context);
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
        tagProcessor.registerTag(ElementTag.class, "constant", (attribute, object) -> {
            if (attribute.hasParam()) {
                if (object.getCitizen().hasTrait(ConstantsTrait.class)
                        && object.getCitizen().getOrAddTrait(ConstantsTrait.class).getConstant(attribute.getParam()) != null) {
                    return new ElementTag(object.getCitizen().getOrAddTrait(ConstantsTrait.class)
                            .getConstant(attribute.getParam()));
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
        tagProcessor.registerTag(ElementTag.class, "has_pose", (attribute, object) -> {
            if (attribute.hasParam()) {
                return new ElementTag(object.getCitizen().getOrAddTrait(Poses.class).hasPose(attribute.getParam()));
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
        tagProcessor.registerTag(LocationTag.class, "pose", (attribute, object) -> {
            if (attribute.hasParam()) {
                Pose pose = object.getCitizen().getOrAddTrait(Poses.class).getPose(attribute.getParam());
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
        tagProcessor.registerTag(ObjectTag.class, "name_hologram_npc", (attribute, object) -> {
            if (!object.getCitizen().hasTrait(HologramTrait.class)) {
                return null;
            }
            HologramTrait hologram = object.getCitizen().getTraitNullable(HologramTrait.class);
            Entity entity = hologram.getNameEntity();
            if (entity == null) {
                return null;
            }
            return new EntityTag(entity).getDenizenObject();
        });

        // <--[tag]
        // @attribute <NPCTag.hologram_npcs>
        // @returns ListTag(NPCTag)
        // @description
        // Returns the list of hologram NPCs attached to an NPC (if any).
        // Note that these can regenerate at any time.
        // -->
        tagProcessor.registerTag(ListTag.class, "hologram_npcs", (attribute, object) -> {
            if (!object.getCitizen().hasTrait(HologramTrait.class)) {
                return null;
            }
            HologramTrait hologram = object.getCitizen().getTraitNullable(HologramTrait.class);
            Collection<ArmorStand> stands = hologram.getHologramEntities();
            if (stands == null || stands.isEmpty()) {
                return null;
            }
            ListTag output = new ListTag();
            for (ArmorStand stand : stands) {
                output.addObject(new EntityTag(stand).getDenizenObject());
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
        tagProcessor.registerTag(ListTag.class, "hologram_lines", (attribute, object) -> {
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
        tagProcessor.registerTag(ElementTag.class, "hologram_direction", (attribute, object) -> {
            if (!object.getCitizen().hasTrait(HologramTrait.class)) {
                return null;
            }
            HologramTrait hologram = object.getCitizen().getTraitNullable(HologramTrait.class);
            return new ElementTag(hologram.getDirection());
        });

        // <--[tag]
        // @attribute <NPCTag.hologram_line_height>
        // @returns ElementTag(Decimal)
        // @mechanism NPCTag.hologram_line_height
        // @description
        // Returns the line height for an NPC's hologram. Can be -1, indicating a default value should be used.
        // -->
        tagProcessor.registerTag(ElementTag.class, "hologram_line_height", (attribute, object) -> {
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
        tagProcessor.registerTag(ElementTag.class, "is_sneaking", (attribute, object) -> {
            if (!object.isSpawned() && object.getEntity() instanceof Player) {
                return null;
            }
            return new ElementTag(((Player) object.getEntity()).isSneaking());
        });

        // <--[tag]
        // @attribute <NPCTag.engaged[(<player>)]>
        // @returns ElementTag(Boolean)
        // @description
        // Returns whether the NPC is currently engaged.
        // See <@link command engage>
        // -->
        tagProcessor.registerTag(ElementTag.class, "engaged", (attribute, object) -> {
            return new ElementTag(EngageCommand.getEngaged(object.getCitizen(), attribute.hasParam() ? attribute.paramAsType(PlayerTag.class) : null));
        }, "is_engaged");

        // <--[tag]
        // @attribute <NPCTag.invulnerable>
        // @returns ElementTag(Boolean)
        // @description
        // Returns whether the NPC is currently invulnerable.
        // See <@link command vulnerable>
        // -->
        tagProcessor.registerTag(ElementTag.class, "invulnerable", (attribute, object) -> {
            return new ElementTag(object.getCitizen().data().get(NPC.DEFAULT_PROTECTED_METADATA, true));
        }, "vulnerable");

        // <--[tag]
        // @attribute <NPCTag.id>
        // @returns ElementTag(Number)
        // @description
        // Returns the NPC's ID number.
        // -->
        tagProcessor.registerTag(ElementTag.class, "id", (attribute, object) -> {
            return new ElementTag(object.getId());
        });

        // <--[tag]
        // @attribute <NPCTag.owner>
        // @returns PlayerTag
        // @mechanism NPCTag.owner
        // @description
        // Returns the owner of the NPC as a PlayerTag, if any.
        // -->
        tagProcessor.registerTag(ObjectTag.class, "owner", (attribute, object) -> {
            UUID owner = object.getOwner();
            if (owner == null) {
                return null;
            }
            OfflinePlayer player = Bukkit.getOfflinePlayer(owner);
            if (player.isOnline() || player.hasPlayedBefore()) {
                return new PlayerTag(player);
            }
            return null;
        });

        // <--[tag]
        // @attribute <NPCTag.has_skin>
        // @returns ElementTag(Boolean)
        // @mechanism NPCTag.skin
        // @description
        // Returns whether the NPC has a custom skin.
        // -->
        tagProcessor.registerTag(ElementTag.class, "has_skin", (attribute, object) -> {
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
        tagProcessor.registerTag(ElementTag.class, "skin_blob", (attribute, object) -> {
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
        tagProcessor.registerTag(ElementTag.class, "skull_skin", (attribute, object) -> {
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
        tagProcessor.registerTag(ElementTag.class, "skin", (attribute, object) -> {
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
        tagProcessor.registerTag(ElementTag.class, "auto_update_skin", (attribute, object) -> {
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
        tagProcessor.registerTag(InventoryTag.class, "inventory", (attribute, object) -> {
            return object.getDenizenInventory();
        });

        // <--[tag]
        // @attribute <NPCTag.is_spawned>
        // @returns ElementTag(Boolean)
        // @description
        // Returns whether the NPC is spawned.
        // -->
        tagProcessor.registerTag(ElementTag.class, "is_spawned", (attribute, object) -> {
            return new ElementTag(object.isSpawned());
        });

        // <--[tag]
        // @attribute <NPCTag.is_protected>
        // @returns ElementTag(Boolean)
        // @description
        // Returns whether the NPC is protected.
        // -->
        tagProcessor.registerTag(ElementTag.class, "is_protected", (attribute, object) -> {
            return new ElementTag(object.getCitizen().isProtected());
        });

        // <--[tag]
        // @attribute <NPCTag.lookclose>
        // @returns ElementTag(Boolean)
        // @mechanism NPCTag.lookclose
        // @description
        // Returns whether the NPC has lookclose enabled.
        // -->
        tagProcessor.registerTag(ElementTag.class, "lookclose", (attribute, object) -> {
            NPC citizen = object.getCitizen();
            if (citizen.hasTrait(LookClose.class)) {
                // There is no method to check if the NPC has LookClose enabled...
                // LookClose.toString() returns "LookClose{" + enabled + "}"
                String lookclose = citizen.getOrAddTrait(LookClose.class).toString();
                lookclose = lookclose.substring(10, lookclose.length() - 1);
                return new ElementTag(Boolean.parseBoolean(lookclose));
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
        tagProcessor.registerTag(ElementTag.class, "controllable", (attribute, object) -> {
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
        tagProcessor.registerTag(ElementTag.class, "targetable", (attribute, object) -> {
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
        tagProcessor.registerTag(ElementTag.class, "teleport_on_stuck", (attribute, object) -> {
            return new ElementTag(object.getNavigator().getDefaultParameters().stuckAction() == TeleportStuckAction.INSTANCE);
        });

        tagProcessor.registerTag(ElementTag.class, "has_script", (attribute, object) -> {
            BukkitImplDeprecations.hasScriptTags.warn(attribute.context);
            NPC citizen = object.getCitizen();
            return new ElementTag(citizen.hasTrait(AssignmentTrait.class));
        });

        // <--[tag]
        // @attribute <NPCTag.script>
        // @returns ScriptTag
        // @deprecated Use 'NPCTag.scripts' (plural) instead.
        // @description
        // Deprecated variant of <@link tag NPCTag.scripts>.
        // -->
        tagProcessor.registerTag(ScriptTag.class, "script", (attribute, object) -> {
            BukkitImplDeprecations.npcScriptSingle.warn(attribute.context);
            NPC citizen = object.getCitizen();
            if (!citizen.hasTrait(AssignmentTrait.class)) {
                return null;
            }
            else {
                for (AssignmentScriptContainer container : citizen.getOrAddTrait(AssignmentTrait.class).containerCache) {
                    if (container != null) {
                        return new ScriptTag(container);
                    }
                }
                return null;
            }
        });

        // <--[tag]
        // @attribute <NPCTag.scripts>
        // @returns ListTag(ScriptTag)
        // @description
        // Returns a list of all assignment scripts on the NPC. Returns null if none.
        // -->
        tagProcessor.registerTag(ListTag.class, "scripts", (attribute, object) -> {
            NPC citizen = object.getCitizen();
            if (!citizen.hasTrait(AssignmentTrait.class)) {
                return null;
            }
            else {
                ListTag result = new ListTag();
                for (AssignmentScriptContainer container : citizen.getOrAddTrait(AssignmentTrait.class).containerCache) {
                    if (container != null) {
                       result.addObject(new ScriptTag(container));
                    }
                }
                return result;
            }
        });

        // <--[tag]
        // @attribute <NPCTag.distance_margin>
        // @returns ElementTag(Decimal)
        // @mechanism NPCTag.distance_margin
        // @description
        // Returns the NPC's current pathfinding distance margin. That is, how close it needs to get to its destination (in block-lengths).
        // -->
        tagProcessor.registerTag(ElementTag.class, "distance_margin", (attribute, object) -> {
            return new ElementTag(object.getNavigator().getDefaultParameters().distanceMargin());
        });

        // <--[tag]
        // @attribute <NPCTag.path_distance_margin>
        // @returns ElementTag(Decimal)
        // @mechanism NPCTag.path_distance_margin
        // @description
        // Returns the NPC's current pathfinding distance margin. That is, how close it needs to get to individual points along its path.
        // -->
        tagProcessor.registerTag(ElementTag.class, "path_distance_margin", (attribute, object) -> {
            return new ElementTag(object.getNavigator().getDefaultParameters().pathDistanceMargin());
        });

        // <--[tag]
        // @attribute <NPCTag.use_new_finder>
        // @returns ElementTag(Boolean)
        // @mechanism NPCTag.use_new_finder
        // @description
        // If output is 'true', the NPC uses the 'new' Citizens A-Star pathfinder.
        // if 'false', the NPC uses the 'old' minecraft vanilla mob pathfinder.
        // -->
        tagProcessor.registerTag(ElementTag.class, "use_new_finder", (attribute, object) -> {
            return new ElementTag(object.getNavigator().getDefaultParameters().useNewPathfinder());
        });

        // <--[tag]
        // @attribute <NPCTag.is_navigating>
        // @returns ElementTag(Boolean)
        // @description
        // Returns whether the NPC is currently navigating.
        // -->
        tagProcessor.registerTag(ElementTag.class, "is_navigating", (attribute, object) -> {
            return new ElementTag(object.getNavigator().isNavigating());
        });

        // <--[tag]
        // @attribute <NPCTag.speed>
        // @returns ElementTag(Decimal)
        // @mechanism NPCTag.speed
        // @description
        // Returns the current speed of the NPC.
        // -->
        tagProcessor.registerTag(ElementTag.class, "speed", (attribute, object) -> {
            return new ElementTag(object.getNavigator().getLocalParameters().speed());
        });

        // <--[tag]
        // @attribute <NPCTag.range>
        // @returns ElementTag(Decimal)
        // @mechanism NPCTag.range
        // @description
        // Returns the NPC's current maximum pathfinding range.
        // -->
        tagProcessor.registerTag(ElementTag.class, "range", (attribute, object) -> {
            return new ElementTag(object.getNavigator().getLocalParameters().range());
        });

        // <--[tag]
        // @attribute <NPCTag.attack_range>
        // @returns ElementTag(Decimal)
        // @mechanism NPCTag.attack_range
        // @description
        // Returns the NPC's current navigator attack range limit.
        // -->
        tagProcessor.registerTag(ElementTag.class, "attack_range", (attribute, object) -> {
            return new ElementTag(object.getNavigator().getLocalParameters().attackRange());
        });

        // <--[tag]
        // @attribute <NPCTag.attack_strategy>
        // @returns ElementTag
        // @description
        // Returns the NPC's current navigator attack strategy.
        // Not related to Sentinel combat.
        // -->
        tagProcessor.registerTag(ElementTag.class, "attack_strategy", (attribute, object) -> {
            return new ElementTag(object.getNavigator().getLocalParameters().attackStrategy().toString());
        });

        // <--[tag]
        // @attribute <NPCTag.speed_modifier>
        // @returns ElementTag(Decimal)
        // @description
        // Returns the NPC's current movement speed modifier (a multiplier applied over their base speed).
        // -->
        tagProcessor.registerTag(ElementTag.class, "speed_modifier", (attribute, object) -> {
            return new ElementTag(object.getNavigator().getLocalParameters().speedModifier());
        });

        // <--[tag]
        // @attribute <NPCTag.base_speed>
        // @returns ElementTag(Decimal)
        // @description
        // Returns the NPC's base navigation speed.
        // -->
        tagProcessor.registerTag(ElementTag.class, "base_speed", (attribute, object) -> {
            return new ElementTag(object.getNavigator().getLocalParameters().baseSpeed());
        });

        // <--[tag]
        // @attribute <NPCTag.avoid_water>
        // @returns ElementTag(Boolean)
        // @description
        // Returns whether the NPC will avoid water.
        // -->
        tagProcessor.registerTag(ElementTag.class, "avoid_water", (attribute, object) -> {
            return new ElementTag(object.getNavigator().getLocalParameters().avoidWater());
        });

        // <--[tag]
        // @attribute <NPCTag.target_location>
        // @returns LocationTag
        // @description
        // Returns the location the NPC is currently navigating towards (if any).
        // -->
        tagProcessor.registerTag(LocationTag.class, "target_location", (attribute, object) -> {
            if (object.getNavigator().getTargetAsLocation() == null) {
                return null;
            }
            return new LocationTag(object.getNavigator().getTargetAsLocation());
        });

        // <--[tag]
        // @attribute <NPCTag.navigator_look_at>
        // @returns LocationTag
        // @mechanism NPCTag.navigator_look_at
        // @description
        // Returns the location the NPC will currently look at while moving, if any.
        // -->
        tagProcessor.registerTag(LocationTag.class, "navigator_look_at", (attribute, object) -> {
            if (object.getNavigator().getLocalParameters().lookAtFunction() == null) {
                return null;
            }
            Location res = object.getNavigator().getLocalParameters().lookAtFunction().apply(object.getNavigator());
            if (res == null) {
                return null;
            }
            return new LocationTag(res);
        });

        // <--[tag]
        // @attribute <NPCTag.is_fighting>
        // @returns ElementTag(Boolean)
        // @description
        // Returns whether the NPC is currently targeting an entity for the Citizens internal punching pathfinder.
        // Not compatible with Sentinel.
        // -->
        tagProcessor.registerTag(ElementTag.class, "is_fighting", (attribute, object) -> {
            return new ElementTag(object.getNavigator().getEntityTarget() != null && object.getNavigator().getEntityTarget().isAggressive());
        });

        // <--[tag]
        // @attribute <NPCTag.target_type>
        // @returns ElementTag
        // @description
        // Returns the entity type of the NPC's current navigation target (if any).
        // -->
        tagProcessor.registerTag(ElementTag.class, "target_type", (attribute, object) -> {
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
        tagProcessor.registerTag(EntityFormObject.class, "target_entity", (attribute, object) -> {
            if (object.getNavigator().getEntityTarget() == null || object.getNavigator().getEntityTarget().getTarget() == null) {
                return null;
            }
            return new EntityTag(object.getNavigator().getEntityTarget().getTarget()).getDenizenObject();
        });

        // <--[tag]
        // @attribute <NPCTag.registry_name>
        // @returns ElementTag
        // @description
        // Returns the name of the registry this NPC came from.
        // -->
        tagProcessor.registerTag(ElementTag.class, "registry_name", (attribute, object) -> {
            return new ElementTag(object.getCitizen().getOwningRegistry().getName());
        });

        // <--[tag]
        // @attribute <NPCTag.citizens_data[<key>]>
        // @returns ElementTag
        // @description
        // Returns the value of a Citizens NPC metadata key.
        // -->
        tagProcessor.registerTag(ElementTag.class, "citizens_data", (attribute, object) -> {
            if (!attribute.hasParam()) {
                return null;
            }
            Object val = object.getCitizen().data().get(attribute.getParam());
            if (val == null) {
                return null;
            }
            return new ElementTag(val.toString());
        });

        // <--[tag]
        // @attribute <NPCTag.citizens_data_keys>
        // @returns ListTag
        // @description
        // Returns a list of Citizens NPC metadata keys.
        // -->
        tagProcessor.registerTag(ListTag.class, "citizens_data_keys", (attribute, object) -> {
            DataKey holder = new MemoryDataKey();
            object.getCitizen().data().saveTo(holder);
            ListTag result = new ListTag();
            for (DataKey key : holder.getSubKeys()) {
                result.addObject(new ElementTag(key.name(), true));
            }
            return result;
        });

        tagProcessor.registerTag(NPCTag.class, "navigator", (attribute, object) -> {
            BukkitImplDeprecations.oldNPCNavigator.warn(attribute.context);
            return object;
        });
    }

    public static ObjectTagProcessor<NPCTag> tagProcessor = new ObjectTagProcessor<>();

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
        mechanism.echoError("Cannot apply properties to an NPC!");
    }

    @Override
    public void adjust(Mechanism mechanism) {

        // TODO: For all the mechanism tags, add the @Mechanism link!

        // <--[mechanism]
        // @object NPCTag
        // @name set_assignment
        // @input ScriptTag
        // @description
        // Sets the NPC's assignment script. Equivalent to 'clear_assignments' + 'add_assignment'.
        // @tags
        // <NPCTag.script>
        // -->
        if (mechanism.matches("set_assignment") && mechanism.requireObject(ScriptTag.class)) {
            AssignmentTrait trait = getCitizen().getOrAddTrait(AssignmentTrait.class);
            trait.clearAssignments(null);
            trait.addAssignmentScript((AssignmentScriptContainer) mechanism.valueAsType(ScriptTag.class).getContainer(), null);
        }

        // <--[mechanism]
        // @object NPCTag
        // @name add_assignment
        // @input ScriptTag
        // @description
        // Adds an assignment script to the NPC.
        // @tags
        // <NPCTag.script>
        // -->
        if (mechanism.matches("add_assignment") && mechanism.requireObject(ScriptTag.class)) {
            getCitizen().getOrAddTrait(AssignmentTrait.class).addAssignmentScript((AssignmentScriptContainer) mechanism.valueAsType(ScriptTag.class).getContainer(), null);
        }

        // <--[mechanism]
        // @object NPCTag
        // @name remove_assignment
        // @input ScriptTag
        // @description
        // Removes an assignment script from the NPC.
        // @tags
        // <NPCTag.script>
        // -->
        if (mechanism.matches("remove_assignment")) {
            if (npc.hasTrait(AssignmentTrait.class)) {
                if (mechanism.hasValue()) {
                    AssignmentTrait trait = getCitizen().getOrAddTrait(AssignmentTrait.class);
                    trait.removeAssignmentScript(mechanism.getValue().asString(), null);
                    trait.checkAutoRemove();
                }
                else {
                    BukkitImplDeprecations.assignmentRemove.warn(mechanism.context);
                    getCitizen().getOrAddTrait(AssignmentTrait.class).clearAssignments(null);
                    npc.removeTrait(AssignmentTrait.class);
                }
            }
        }

        // <--[mechanism]
        // @object NPCTag
        // @name clear_assignments
        // @input None
        // @description
        // Removes all the NPC's assignment scripts.
        // @tags
        // <NPCTag.script>
        // -->
        if (mechanism.matches("clear_assignments")) {
            if (npc.hasTrait(AssignmentTrait.class)) {
                getCitizen().getOrAddTrait(AssignmentTrait.class).clearAssignments(null);
                npc.removeTrait(AssignmentTrait.class);
            }
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
            for (String line : mechanism.valueAsType(ListTag.class)) {
                hologram.addLine(line);
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
        if (mechanism.matches("hologram_direction") && mechanism.requireEnum(HologramTrait.HologramDirection.class)) {
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
            getCitizen().setName(mechanism.getValue().asString().length() > 256 ? mechanism.getValue().asString().substring(0, 256) : mechanism.getValue().asString());
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
            BukkitImplDeprecations.npcSpawnMechanism.warn(mechanism.context);
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
            BukkitImplDeprecations.npcDespawnMech.warn(mechanism.context);
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
        // @name use_new_finder
        // @input ElementTag(Boolean)
        // @description
        // If input is 'true', causes the NPC to use the 'new' Citizens A-Star pathfinder.
        // if 'false', causes the NPC to use the 'old' minecraft vanilla mob pathfinder.
        // @tags
        // <NPCTag.use_new_finder>
        // -->
        if (mechanism.matches("use_new_finder") && mechanism.requireBoolean()) {
            getNavigator().getDefaultParameters().useNewPathfinder(mechanism.getValue().asBoolean());
        }

        // <--[mechanism]
        // @object NPCTag
        // @name navigator_look_at
        // @input LocationTag
        // @description
        // Sets the location the NPC will currently look at while moving.
        // Give no value to let the NPC automatically look where it's going.
        // Should be set after the NPC has started moving.
        // @tags
        // <NPCTag.navigator_look_at>
        // -->
        if (mechanism.matches("navigator_look_at")) {
            if (mechanism.hasValue() && mechanism.requireObject(LocationTag.class)) {
                final LocationTag loc = mechanism.valueAsType(LocationTag.class);
                getNavigator().getLocalParameters().lookAtFunction((n) -> loc);
            }
            else {
                getNavigator().getLocalParameters().lookAtFunction(null);
            }
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
        if (mechanism.matches("glow_color") && mechanism.requireEnum(ChatColor.class)) {
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
            Location target = mechanism.valueAsType(LocationTag.class).clone();
            Waypoints wp = getCitizen().getOrAddTrait(Waypoints.class);
            if ((wp.getCurrentProvider() instanceof LinearWaypointProvider)) {
                ((LinearWaypointProvider) wp.getCurrentProvider()).addWaypoint(new Waypoint(target));
            }
            else if ((wp.getCurrentProvider() instanceof WaypointProvider.EnumerableWaypointProvider)) {
                ((List<Waypoint>) ((WaypointProvider.EnumerableWaypointProvider) wp.getCurrentProvider()).waypoints()).add(new Waypoint(target));
            }
            else if ((wp.getCurrentProvider() instanceof WanderWaypointProvider)) {
                ((WanderWaypointProvider) wp.getCurrentProvider()).getRegionCentres().add(target);
            }
        }

        tagProcessor.processMechanism(this, mechanism);

        // Pass along to EntityTag mechanism handler if not already handled.
        if (!mechanism.fulfilled()) {
            if (isSpawned()) {
                new EntityTag(getEntity()).adjust(mechanism);
            }
        }
    }

    @Override
    public boolean advancedMatches(String matcher) {
        return isSpawned() && getDenizenEntity().tryAdvancedMatcher(matcher);
    }

    /**
     * Return an appropriate error-header output for this object, if any.
     */
    @Override
    public String getErrorHeaderContext() {
        return " with NPC '<A>" + debuggable() + "<LR>'";
    }
}

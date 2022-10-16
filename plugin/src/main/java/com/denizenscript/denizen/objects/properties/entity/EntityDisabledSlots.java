package com.denizenscript.denizen.objects.properties.entity;

import com.denizenscript.denizen.utilities.BukkitImplDeprecations;
import com.denizenscript.denizen.utilities.nbt.CustomNBT;
import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.core.ListTag;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.core.MapTag;
import com.denizenscript.denizencore.objects.properties.Property;
import com.denizenscript.denizencore.objects.properties.PropertyParser;
import com.denizenscript.denizencore.utilities.CoreUtilities;
import com.denizenscript.denizencore.utilities.text.StringHolder;
import org.bukkit.entity.ArmorStand;
import org.bukkit.inventory.EquipmentSlot;

import java.util.*;

public class EntityDisabledSlots implements Property {

    public static boolean describes(ObjectTag entity) {
        return entity instanceof EntityTag
                && ((EntityTag) entity).getBukkitEntity() instanceof ArmorStand;
    }

    public static EntityDisabledSlots getFrom(ObjectTag entity) {
        if (!describes(entity)) {
            return null;
        }
        else {
            return new EntityDisabledSlots((EntityTag) entity);
        }
    }

    public static final String[] handledMechs = new String[] {
            "disabled_slots_raw", "disabled_slots"
    };

    private EntityDisabledSlots(EntityTag entity) {
        dentity = entity;
    }

    EntityTag dentity;

    public enum Action {
        ALL(0), REMOVE(8), PLACE(16);

        private final int id;

        Action(int id) {
            this.id = id;
        }

        public int getId() {
            return id;
        }
    }

    private ListTag getDisabledSlots() {
        Map<EquipmentSlot, Set<Action>> map = CustomNBT.getDisabledSlots(dentity.getBukkitEntity());
        ListTag list = new ListTag();
        for (Map.Entry<EquipmentSlot, Set<Action>> entry : map.entrySet()) {
            for (Action action : entry.getValue()) {
                list.add(CoreUtilities.toLowerCase(entry.getKey().name() + "/" + action.name()));
            }
        }
        return list;
    }

    private MapTag getDisabledSlotsMap() {
        Map<EquipmentSlot, Set<Action>> map = CustomNBT.getDisabledSlots(dentity.getBukkitEntity());
        MapTag mapTag = new MapTag();
        for (Map.Entry<EquipmentSlot, Set<Action>> entry : map.entrySet()) {
            ListTag actions = new ListTag();
            for (Action action : entry.getValue()) {
                actions.addObject(new ElementTag(action));
            }
            mapTag.putObject(entry.getKey().name(), actions);
        }
        return mapTag;
    }

    @Override
    public String getPropertyString() {
        MapTag map = getDisabledSlotsMap();
        return map.map.isEmpty() ? null : map.identify();
    }

    @Override
    public String getPropertyId() {
        return "disabled_slots";
    }

    public static void registerTags() {

        // <--[tag]
        // @attribute <EntityTag.disabled_slots>
        // @returns ListTag
        // @mechanism EntityTag.disabled_slots
        // @deprecated Use 'EntityTag.disabled_slots_data'
        // @group properties
        // @description
        // Deprecated in favor of <@link tag EntityTag.disabled_slots_data>.
        // -->
        PropertyParser.registerTag(EntityDisabledSlots.class, ObjectTag.class, "disabled_slots", (attribute, object) -> {

            // <--[tag]
            // @attribute <EntityTag.disabled_slots.raw>
            // @returns ElementTag(Number)
            // @mechanism EntityTag.disabled_slots_raw
            // @deprecated Use 'disabled_slots_data'
            // @group properties
            // @description
            // Deprecated in favor of <@link tag EntityTag.disabled_slots_data>.
            // -->
            if (attribute.startsWith("raw", 2)) {
                BukkitImplDeprecations.armorStandRawSlot.warn(attribute.context);
                attribute.fulfill(1);
                return new ElementTag(CustomNBT.getCustomIntNBT(object.dentity.getBukkitEntity(), CustomNBT.KEY_DISABLED_SLOTS));
            }

            BukkitImplDeprecations.armorStandDisabledSlotsOldFormat.warn(attribute.context);
            return object.getDisabledSlots();
        });

        // <--[tag]
        // @attribute <EntityTag.disabled_slots_data>
        // @returns MapTag
        // @mechanism EntityTag.disabled_slots
        // @group properties
        // @description
        // If the entity is an armor stand, returns its disabled slots as a map of slot names to list of actions.
        // -->
        PropertyParser.registerTag(EntityDisabledSlots.class, MapTag.class, "disabled_slots_data", (attribute, object) -> {
            return object.getDisabledSlotsMap();
        });
    }

    @Override
    public void adjust(Mechanism mechanism) {

        // <--[mechanism]
        // @object EntityTag
        // @name disabled_slots_raw
        // @input ElementTag(Number)
        // @deprecated Use 'disabled_slots'
        // @description
        // Deprecated in favor of <@link mechanism EntityTag.disabled_slots>.
        // @tags
        // <EntityTag.disabled_slots>
        // <EntityTag.disabled_slots.raw>
        // -->
        if (mechanism.matches("disabled_slots_raw") && mechanism.requireInteger()) {
            BukkitImplDeprecations.armorStandRawSlot.warn(mechanism.context);
            CustomNBT.addCustomNBT(dentity.getBukkitEntity(), CustomNBT.KEY_DISABLED_SLOTS, mechanism.getValue().asInt());
        }

        // <--[mechanism]
        // @object EntityTag
        // @name disabled_slots
        // @input MapTag
        // @description
        // Sets the disabled slots of an armor stand as a map of slot names to list of actions.
        // For example: [HEAD=PLACE|REMOVE;CHEST=PLACE;FEET=ALL]
        // Provide no input to enable all slots.
        // Slots: <@link url https://hub.spigotmc.org/javadocs/spigot/org/bukkit/inventory/EquipmentSlot.html>
        // Actions: ALL, REMOVE, PLACE
        // NOTE: Minecraft contains a bug where disabling ALL for the HAND slot still allows item removal.
        // To fully disable hand interaction, disable ALL and REMOVE.
        // @tags
        // <EntityTag.disabled_slots_data>
        // -->
        if (mechanism.matches("disabled_slots")) {
            if (!mechanism.hasValue()) {
                CustomNBT.removeCustomNBT(dentity.getBukkitEntity(), CustomNBT.KEY_DISABLED_SLOTS);
                return;
            }
            Map<EquipmentSlot, Set<Action>> map = new HashMap<>();
            if (mechanism.value.canBeType(MapTag.class)) {
                MapTag input = mechanism.valueAsType(MapTag.class);
                for (Map.Entry<StringHolder, ObjectTag> entry : input.map.entrySet()) {
                    EquipmentSlot slot = new ElementTag(entry.getKey().str).asEnum(EquipmentSlot.class);

                    if (slot == null) {
                        mechanism.echoError("Invalid equipment slot specified: " + entry.getKey().str);
                        continue;
                    }
                    ListTag actionsInput = entry.getValue().asType(ListTag.class, mechanism.context);
                    Set<Action> actions = new HashSet<>();
                    for (String actionStr : actionsInput) {
                        Action action = new ElementTag(actionStr).asEnum(Action.class);
                        if (action == null) {
                            mechanism.echoError("Invalid action specified: " + actionStr);
                            continue;
                        }
                        actions.add(action);
                    }
                    map.put(slot, actions);
                }
            }
            else {
                BukkitImplDeprecations.armorStandDisabledSlotsOldFormat.warn(mechanism.context);
                ListTag input = mechanism.valueAsType(ListTag.class);
                for (String string : input) {
                    String[] split = string.split("/", 2);
                    EquipmentSlot slot = new ElementTag(split[0]).asEnum(EquipmentSlot.class);
                    Action action = Action.ALL;
                    if (slot == null) {
                        mechanism.echoError("Invalid equipment slot specified: " + split[0]);
                        continue;
                    }
                    if (split.length == 2) {
                        action = new ElementTag(split[1]).asEnum(Action.class);
                        if (action == null) {
                            mechanism.echoError("Invalid action specified: " + split[1]);
                            continue;
                        }
                    }
                    Set<Action> set = map.computeIfAbsent(slot, k -> new HashSet<>());
                    set.add(action);
                }
            }
            CustomNBT.setDisabledSlots(dentity.getBukkitEntity(), map);
        }
    }
}

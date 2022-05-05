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
            "disabled_slots", "disabled_slots_raw"
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

    private ListTag getDisabledSlotsMap() {
        Map<EquipmentSlot, Set<Action>> map = CustomNBT.getDisabledSlots(dentity.getBukkitEntity());
        ListTag list = new ListTag();
        for (Map.Entry<EquipmentSlot, Set<Action>> entry : map.entrySet()) {
            for (Action action : entry.getValue()) {
                MapTag mapTag = new MapTag();
                mapTag.putObject("slot", new ElementTag(CoreUtilities.toLowerCase(entry.getKey().name())));
                mapTag.putObject("action", new ElementTag(CoreUtilities.toLowerCase(action.name())));
                list.addObject(mapTag);
            }
        }
        return list;
    }

    @Override
    public String getPropertyString() {
        ListTag list = getDisabledSlotsMap();
        return list.isEmpty() ? null : list.identify();
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
        // @group properties
        // @description
        // If the entity is an armor stand, returns a list of its disabled slots in the form slot/action|...
        // Consider instead using <@link tag EntityTag.disabled_slots_data>.
        // -->
        PropertyParser.<EntityDisabledSlots, ObjectTag>registerTag(ObjectTag.class, "disabled_slots", (attribute, object) -> {
            if (attribute.startsWith("raw", 2)) {
                BukkitImplDeprecations.armorStandRawSlot.warn(attribute.context);
                attribute.fulfill(1);
                return new ElementTag(CustomNBT.getCustomIntNBT(object.dentity.getBukkitEntity(), CustomNBT.KEY_DISABLED_SLOTS));
            }

            return object.getDisabledSlots();
        });

        // <--[tag]
        // @attribute <EntityTag.disabled_slots_data>
        // @returns ListTag
        // @mechanism EntityTag.disabled_slots
        // @group properties
        // @description
        // If the entity is an armor stand, returns it's disabled slots as a list of maps with "slot" and "action" keys.
        // -->
        PropertyParser.<EntityDisabledSlots, ListTag>registerTag(ListTag.class, "disabled_slots_data", (attribute, object) -> {
            return object.getDisabledSlotsMap();
        });
    }

    @Override
    public void adjust(Mechanism mechanism) {

        if (mechanism.matches("disabled_slots_raw") && mechanism.requireInteger()) {
            BukkitImplDeprecations.armorStandRawSlot.warn(mechanism.context);
            CustomNBT.addCustomNBT(dentity.getBukkitEntity(), CustomNBT.KEY_DISABLED_SLOTS, mechanism.getValue().asInt());
        }

        // <--[mechanism]
        // @object EntityTag
        // @name disabled_slots
        // @input ListTag
        // @description
        // Sets the disabled slots of an armor stand.
        // Input is a list of MapTags with a "slot" key, optionally include an "action" key to disable specific interactions (defaults to ALL).
        // Specify no input to enable all slots.
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

            Collection<ObjectTag> list = CoreUtilities.objectToList(mechanism.value, mechanism.context);
            Map<EquipmentSlot, Set<Action>> map = new HashMap<>();

            for (ObjectTag object : list) {
                EquipmentSlot slot;
                Action action = Action.ALL;
                if (object.canBeType(MapTag.class)) {
                    MapTag mapTag = object.asType(MapTag.class, mechanism.context);
                    ObjectTag slotObject = mapTag.getObject("slot");
                    ObjectTag actionObject = mapTag.getObject("action");
                    if (slotObject != null) {
                        if (slotObject.asElement().matchesEnum(EquipmentSlot.class)) {
                            slot = slotObject.asElement().asEnum(EquipmentSlot.class);
                        }
                        else {
                            mechanism.echoError("Invalid equipment slot specified: " + slotObject);
                            continue;
                        }
                    }
                    else {
                        mechanism.echoError("Invalid equipment slot specified: slot is required.");
                        continue;
                    }
                    if (actionObject != null) {
                        if (actionObject.asElement().matchesEnum(Action.class)) {
                            action = actionObject.asElement().asEnum(Action.class);
                        }
                        else {
                            mechanism.echoError("Invalid action specified: " + actionObject);
                            continue;
                        }
                    }
                }
                else {
                    String[] split = object.toString().toUpperCase().split("/", 2);

                    slot = new ElementTag(split[0]).asEnum(EquipmentSlot.class);

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


                }
                Set<Action> set = map.computeIfAbsent(slot, k -> new HashSet<>());
                set.add(action);
            }

            CustomNBT.setDisabledSlots(dentity.getBukkitEntity(), map);
        }
    }
}

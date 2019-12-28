package com.denizenscript.denizen.objects.properties.entity;

import com.denizenscript.denizen.utilities.debugging.Debug;
import com.denizenscript.denizen.utilities.nbt.CustomNBT;
import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.core.ListTag;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.properties.Property;
import com.denizenscript.denizencore.tags.Attribute;
import com.denizenscript.denizencore.utilities.CoreUtilities;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.EquipmentSlot;

import java.util.*;

public class EntityDisabledSlots implements Property {

    public static boolean describes(ObjectTag entity) {
        return entity instanceof EntityTag && ((EntityTag) entity).getBukkitEntityType() == EntityType.ARMOR_STAND;
    }

    public static EntityDisabledSlots getFrom(ObjectTag entity) {
        if (!describes(entity)) {
            return null;
        }
        else {
            return new EntityDisabledSlots((EntityTag) entity);
        }
    }

    public static final String[] handledTags = new String[] {
            "disabled_slots"
    };

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

    @Override
    public String getPropertyString() {
        ListTag list = getDisabledSlots();
        return list.isEmpty() ? null : list.identify();
    }

    @Override
    public String getPropertyId() {
        return "disabled_slots";
    }

    @Override
    public ObjectTag getObjectAttribute(Attribute attribute) {

        if (attribute == null) {
            return null;
        }

        // <--[tag]
        // @attribute <EntityTag.disabled_slots.raw>
        // @returns ElementTag(Number)
        // @mechanism EntityTag.disabled_slots_raw
        // @group properties
        // @description
        // If the entity is an armor stand, returns its raw disabled slots value.
        // See <@link url https://minecraft.gamepedia.com/Armor_Stand/ED>
        // -->
        if (attribute.startsWith("disabled_slots.raw")) {
            return new ElementTag(CustomNBT.getCustomIntNBT(dentity.getBukkitEntity(), CustomNBT.KEY_DISABLED_SLOTS))
                    .getObjectAttribute(attribute.fulfill(2));
        }

        // <--[tag]
        // @attribute <EntityTag.disabled_slots>
        // @returns ListTag
        // @mechanism EntityTag.disabled_slots
        // @group properties
        // @description
        // If the entity is an armor stand, returns a list of its disabled slots in the form li@slot/action|...
        // -->
        if (attribute.startsWith("disabled_slots")) {
            return getDisabledSlots().getObjectAttribute(attribute.fulfill(1));
        }

        return null;
    }

    @Override
    public void adjust(Mechanism mechanism) {

        // <--[mechanism]
        // @object EntityTag
        // @name disabled_slots_raw
        // @input ElementTag(Number)
        // @description
        // Sets the raw disabled slots value of an armor stand.
        // See <@link url https://minecraft.gamepedia.com/Armor_Stand/ED>
        // @tags
        // <EntityTag.disabled_slots>
        // <EntityTag.disabled_slots.raw>
        // -->
        if (mechanism.matches("disabled_slots_raw") && mechanism.requireInteger()) {
            CustomNBT.addCustomNBT(dentity.getBukkitEntity(), CustomNBT.KEY_DISABLED_SLOTS, mechanism.getValue().asInt());
        }

        // <--[mechanism]
        // @object EntityTag
        // @name disabled_slots
        // @input ListTag
        // @description
        // Sets the disabled slots of an armor stand in the form li@slot(/action)|...
        // Optionally include an action to disable specific interactions (defaults to ALL).
        // Leave empty to enable all slots.
        // Slots: <@link url https://hub.spigotmc.org/javadocs/spigot/org/bukkit/inventory/EquipmentSlot.html>
        // Actions: ALL, REMOVE, PLACE
        // NOTE: Minecraft contains a bug where disabling HAND/ALL still allows item removal.
        // To fully disable hand interaction, disable HAND/ALL and HAND/REMOVE.
        // @tags
        // <EntityTag.disabled_slots>
        // <EntityTag.disabled_slots.raw>
        // -->
        if (mechanism.matches("disabled_slots")) {
            if (!mechanism.hasValue()) {
                CustomNBT.removeCustomNBT(dentity.getBukkitEntity(), CustomNBT.KEY_DISABLED_SLOTS);
                return;
            }

            ListTag list = mechanism.valueAsType(ListTag.class);
            Map<EquipmentSlot, Set<Action>> map = new HashMap<>();

            for (String string : list) {
                String[] split = string.toUpperCase().split("/", 2);

                EquipmentSlot slot;
                Action action = null;

                try {
                    slot = EquipmentSlot.valueOf(split[0]);
                }
                catch (IllegalArgumentException e) {
                    Debug.echoError("Invalid equipment slot specified: " + split[0]);
                    continue;
                }

                if (split.length == 2) {
                    try {
                        action = Action.valueOf(split[1]);
                    }
                    catch (IllegalArgumentException e) {
                        Debug.echoError("Invalid action specified: " + split[1]);
                        continue;
                    }
                }

                Set<Action> set = map.computeIfAbsent(slot, k -> new HashSet<>());
                set.add(action == null ? Action.ALL : action);
            }

            CustomNBT.setDisabledSlots(dentity.getBukkitEntity(), map);
        }
    }
}

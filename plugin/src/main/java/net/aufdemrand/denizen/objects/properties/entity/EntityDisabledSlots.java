package net.aufdemrand.denizen.objects.properties.entity;

import net.aufdemrand.denizen.objects.dEntity;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.aufdemrand.denizen.utilities.nbt.CustomNBT;
import net.aufdemrand.denizencore.objects.Element;
import net.aufdemrand.denizencore.objects.Mechanism;
import net.aufdemrand.denizencore.objects.dList;
import net.aufdemrand.denizencore.objects.dObject;
import net.aufdemrand.denizencore.objects.properties.Property;
import net.aufdemrand.denizencore.tags.Attribute;
import net.aufdemrand.denizencore.utilities.CoreUtilities;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.EquipmentSlot;

import java.util.*;

public class EntityDisabledSlots implements Property {

    public static boolean describes(dObject entity) {
        return entity instanceof dEntity && ((dEntity) entity).getBukkitEntityType() == EntityType.ARMOR_STAND;
    }

    public static EntityDisabledSlots getFrom(dObject entity) {
        if (!describes(entity)) {
            return null;
        }
        else {
            return new EntityDisabledSlots((dEntity) entity);
        }
    }

    public static final String[] handledTags = new String[] {
            "disabled_slots"
    };

    public static final String[] handledMechs = new String[] {
            "disabled_slots_raw", "disabled_slots"
    };


    ///////////////////
    // Instance Fields and Methods
    /////////////

    private EntityDisabledSlots(dEntity entity) {
        dentity = entity;
    }

    dEntity dentity;

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

    private dList getDisabledSlots() {
        Map<EquipmentSlot, Set<Action>> map = CustomNBT.getDisabledSlots(dentity.getBukkitEntity());
        dList list = new dList();
        for (Map.Entry<EquipmentSlot, Set<Action>> entry : map.entrySet()) {
            for (Action action : entry.getValue()) {
                list.add(CoreUtilities.toLowerCase(entry.getKey().name() + "/" + action.name()));
            }
        }
        return list;
    }

    /////////
    // Property Methods
    ///////

    @Override
    public String getPropertyString() {
        dList list = getDisabledSlots();
        return list.isEmpty() ? null : list.identify();
    }

    @Override
    public String getPropertyId() {
        return "disabled_slots";
    }

    ///////////
    // dObject Attributes
    ////////

    @Override
    public String getAttribute(Attribute attribute) {

        if (attribute == null) {
            return "null";
        }

        // <--[tag]
        // @attribute <e@entity.disabled_slots.raw>
        // @returns Element(Number)
        // @mechanism dEntity.disabled_slots_raw
        // @group properties
        // @description
        // If the entity is an armor stand, returns its raw disabled slots value.
        // See <@link url https://minecraft.gamepedia.com/Armor_Stand/ED>
        // -->
        if (attribute.startsWith("disabled_slots.raw")) {
            return new Element(CustomNBT.getCustomIntNBT(dentity.getBukkitEntity(), CustomNBT.KEY_DISABLED_SLOTS))
                    .getAttribute(attribute.fulfill(2));
        }

        // <--[tag]
        // @attribute <e@entity.disabled_slots>
        // @returns dList
        // @mechanism dEntity.disabled_slots
        // @group properties
        // @description
        // If the entity is an armor stand, returns a list of its disabled slots in the form li@slot/action|...
        // -->
        if (attribute.startsWith("disabled_slots")) {
            return getDisabledSlots().getAttribute(attribute.fulfill(1));
        }

        return null;
    }

    @Override
    public void adjust(Mechanism mechanism) {

        // <--[mechanism]
        // @object dEntity
        // @name disabled_slots_raw
        // @input Element(Number)
        // @description
        // Sets the raw disabled slots value of an armor stand.
        // See <@link url https://minecraft.gamepedia.com/Armor_Stand/ED>
        // @tags
        // <e@entity.disabled_slots>
        // <e@entity.disabled_slots.raw>
        // -->
        if (mechanism.matches("disabled_slots_raw") && mechanism.requireInteger()) {
            CustomNBT.addCustomNBT(dentity.getBukkitEntity(), CustomNBT.KEY_DISABLED_SLOTS, mechanism.getValue().asInt());
        }

        // <--[mechanism]
        // @object dEntity
        // @name disabled_slots
        // @input dList
        // @description
        // Sets the disabled slots of an armor stand in the form li@slot(/action)|...
        // Optionally include an action to disable specific interactions (defaults to ALL).
        // Leave empty to enable all slots.
        // Slots: <@link url https://hub.spigotmc.org/javadocs/spigot/org/bukkit/inventory/EquipmentSlot.html>
        // Actions: ALL, REMOVE, PLACE
        // NOTE: Minecraft contains a bug where disabling HAND/ALL still allows item removal.
        // To fully disable hand interaction, disable HAND/ALL and HAND/REMOVE.
        // @tags
        // <e@entity.disabled_slots>
        // <e@entity.disabled_slots.raw>
        // -->
        if (mechanism.matches("disabled_slots")) {
            if (!mechanism.hasValue()) {
                CustomNBT.removeCustomNBT(dentity.getBukkitEntity(), CustomNBT.KEY_DISABLED_SLOTS);
                return;
            }

            dList list = mechanism.getValue().asType(dList.class);
            Map<EquipmentSlot, Set<Action>> map = new HashMap<>();

            for (String string : list) {
                String[] split = string.toUpperCase().split("/", 2);

                EquipmentSlot slot;
                Action action = null;

                try {
                    slot = EquipmentSlot.valueOf(split[0]);
                }
                catch (IllegalArgumentException e) {
                    dB.echoError("Invalid equipment slot specified: " + split[0]);
                    continue;
                }

                if (split.length == 2) {
                    try {
                        action = Action.valueOf(split[1]);
                    }
                    catch (IllegalArgumentException e) {
                        dB.echoError("Invalid action specified: " + split[1]);
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

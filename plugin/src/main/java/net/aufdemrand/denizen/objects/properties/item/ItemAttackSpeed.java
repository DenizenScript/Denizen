package net.aufdemrand.denizen.objects.properties.item;

import net.aufdemrand.denizen.nms.NMSHandler;
import net.aufdemrand.denizen.nms.enums.EntityAttribute;
import net.aufdemrand.denizen.nms.util.EntityAttributeModifier;
import net.aufdemrand.denizen.objects.dItem;
import net.aufdemrand.denizencore.objects.Element;
import net.aufdemrand.denizencore.objects.Mechanism;
import net.aufdemrand.denizencore.objects.dList;
import net.aufdemrand.denizencore.objects.dObject;
import net.aufdemrand.denizencore.objects.properties.Property;
import net.aufdemrand.denizencore.tags.Attribute;
import net.aufdemrand.denizencore.utilities.CoreUtilities;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class ItemAttackSpeed implements Property {

    public static boolean describes(dObject item) {
        return item instanceof dItem;
    }

    public static ItemAttackSpeed getFrom(dObject item) {
        if (!describes(item)) {
            return null;
        }
        else {
            return new ItemAttackSpeed((dItem) item);
        }
    }


    private ItemAttackSpeed(dItem item) {
        this.item = item;
    }

    dItem item;

    @Override
    public String getPropertyString() {
        dList list = new dList();
        Map<EntityAttribute, List<EntityAttributeModifier>> modifiers = NMSHandler.getInstance().getItemHelper()
                .getAttributeModifiers(item.getItemStack());
        if (modifiers.containsKey(EntityAttribute.GENERIC_ATTACK_SPEED)) {
            for (EntityAttributeModifier modifier : modifiers.get(EntityAttribute.GENERIC_ATTACK_SPEED)) {
                list.add(modifier.getUniqueId().toString());
                list.add(modifier.getName());
                list.add(modifier.getOperation().name());
                list.add(new Element(modifier.getAmount()).identify());
            }
        }
        return list.isEmpty() ? null : list.identify();
    }

    @Override
    public String getPropertyId() {
        return "attack_speed";
    }

    @Override
    public String getAttribute(Attribute attribute) {

        if (attribute == null) {
            return null;
        }

        // <--[tag]
        // @attribute <i@item.attack_speed>
        // @returns dList
        // @group properties
        // @mechanism dItem.attack_speed
        // @description
        // Gets the attack speed modifiers of the item in the format:
        // UUID/NAME/OPERATION/AMOUNT|...
        // Available operations: ADD_NUMBER, ADD_SCALAR, MULTIPLY_SCALAR_1
        // -->
        if (attribute.startsWith("attack_speed")) {
            String string = getPropertyString();
            if (string == null) {
                return null;
            }
            return new Element(string).getAttribute(attribute.fulfill(1));
        }

        return null;
    }

    @Override
    public void adjust(Mechanism mechanism) {

        // <--[mechanism]
        // @object dItem
        // @name attack_speed
        // @input dList
        // @description
        // Changes the attack speed modifier using the format:
        // UUID/NAME/OPERATION/AMOUNT|...
        // Available operations: ADD_NUMBER, ADD_SCALAR, MULTIPLY_SCALAR_1
        // @tags
        // <i@item.attack_speed>
        // -->

        if (mechanism.matches("attack_speed")) {
            Map<EntityAttribute, List<EntityAttributeModifier>> map = NMSHandler.getInstance().getItemHelper()
                    .getAttributeModifiers(item.getItemStack());
            List<EntityAttributeModifier> modifiers = new ArrayList<EntityAttributeModifier>();
            dList value = mechanism.getValue().asType(dList.class);
            for (String string : value) {
                List<String> split = CoreUtilities.split(string, '/', 4);
                UUID uuid = UUID.fromString(split.get(0));
                String name = split.get(1);
                EntityAttributeModifier.Operation operation = EntityAttributeModifier.Operation.valueOf(split.get(2).toUpperCase());
                double amount = new Element(split.get(3)).asDouble();
                modifiers.add(new EntityAttributeModifier(uuid, name, operation, amount));
            }
            map.put(EntityAttribute.GENERIC_ATTACK_SPEED, modifiers);
            item.setItemStack(NMSHandler.getInstance().getItemHelper().setAttributeModifiers(item.getItemStack(), map));
        }
    }
}

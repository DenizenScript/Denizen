package com.denizenscript.denizen.objects.properties.inventory;

import com.denizenscript.denizen.objects.InventoryTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.properties.ObjectProperty;
import com.denizenscript.denizencore.objects.properties.PropertyParser;
import org.bukkit.event.inventory.InventoryType;

public class InventorySize extends ObjectProperty<InventoryTag> {

    public static boolean describes(ObjectTag inventory) {
        return true;
    }

    public int getSize() {
        if (object.getInventory() == null) {
            return 0;
        }
        return object.getInventory().getSize();
    }

    public void setSize(int size) {
        object.setSize(size);
    }

    @Override
    public ElementTag getPropertyValue() {
        if (getSize() > 0 && (object.getIdType().equals("generic") || object.getIdType().equals("script")) && object.getInventoryType() == InventoryType.CHEST) {
            return new ElementTag(getSize());
        }
        return null;
    }

    @Override
    public String getPropertyId() {
        return "size";
    }

    public static void register() {

        // <--[tag]
        // @attribute <InventoryTag.size>
        // @returns ElementTag(Number)
        // @group properties
        // @mechanism InventoryTag.size
        // @description
        // Return the number of slots in the inventory.
        // -->
        PropertyParser.registerTag(InventorySize.class, ElementTag.class, "size", (attribute, inventory) -> {
            return new ElementTag(inventory.getSize());
        });

        // <--[mechanism]
        // @object InventoryTag
        // @name size
        // @input ElementTag(Number)
        // @description
        // Sets the size of the inventory. (Only works for "generic" chest inventories.)
        // @tags
        // <InventoryTag.size>
        // -->
        PropertyParser.registerMechanism(InventorySize.class, ElementTag.class, "size", (prop, mechanism, param) -> {
            if (prop.object.getIdType().equals("generic") || prop.object.getIdType().equals("script")) {
                prop.setSize(param.asInt());
            }
            else {
                mechanism.echoError("Inventories of type '" + prop.object.getIdType() + "' cannot have their size changed!");
            }
        });
    }
}

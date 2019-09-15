package com.denizenscript.denizen.objects.properties.inventory;

import com.denizenscript.denizen.objects.InventoryTag;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.properties.Property;
import com.denizenscript.denizencore.tags.Attribute;

public class InventoryScriptName implements Property {

    public static boolean describes(ObjectTag inventory) {
        // All inventories can have a script_name
        return inventory instanceof InventoryTag;
    }

    public static InventoryScriptName getFrom(ObjectTag inventory) {
        if (!describes(inventory)) {
            return null;
        }
        return new InventoryScriptName((InventoryTag) inventory);
    }

    public static final String[] handledTags = new String[] {
            "script_name"
    };

    public static final String[] handledMechs = new String[] {
            "script_name"
    };

    ///////////////////
    // Instance Fields and Methods
    /////////////

    InventoryTag inventory;

    public InventoryScriptName(InventoryTag inventory) {
        this.inventory = inventory;
    }

    /////////
    // Property Methods
    ///////

    @Override
    public String getPropertyString() {
        return inventory.scriptName;
    }

    @Override
    public String getPropertyId() {
        return "contents";
    }

    @Override
    public ObjectTag getObjectAttribute(Attribute attribute) {

        if (attribute == null) {
            return null;
        }

        // <--[tag]
        // @attribute <InventoryTag.script_name>
        // @returns ElementTag
        // @group properties
        // @description
        // Returns the name of the script that this inventory came from (if any).
        // -->
        if (attribute.startsWith("script_name")) {
            return new ElementTag(inventory.scriptName)
                    .getObjectAttribute(attribute.fulfill(1));
        }

        return null;
    }

    @Override
    public void adjust(Mechanism mechanism) {

        // Undocumented / internal
        if (mechanism.matches("script_name")) {
            inventory.scriptName = mechanism.getValue().asString();
        }
    }
}

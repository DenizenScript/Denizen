package com.denizenscript.denizen.objects.properties.inventory;

import com.denizenscript.denizen.objects.InventoryTag;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.properties.Property;
import com.denizenscript.denizencore.objects.properties.PropertyParser;

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

    public static final String[] handledMechs = new String[] {
            "script_name"
    };

    InventoryTag inventory;

    public InventoryScriptName(InventoryTag inventory) {
        this.inventory = inventory;
    }

    @Override
    public String getPropertyString() {
        return inventory.scriptName;
    }

    @Override
    public String getPropertyId() {
        return "contents";
    }

    public static void registerTags() {

        // <--[tag]
        // @attribute <InventoryTag.script_name>
        // @returns ElementTag
        // @group properties
        // @description
        // Returns the name of the script that this inventory came from (if any).
        // -->
        PropertyParser.<InventoryScriptName>registerTag("script_name", (attribute, inventory) -> {
            if (inventory.inventory.scriptName == null) {
                return null;
            }
            return new ElementTag(inventory.inventory.scriptName);
        });
    }

    @Override
    public void adjust(Mechanism mechanism) {

        // Undocumented / internal
        if (mechanism.matches("script_name")) {
            inventory.scriptName = mechanism.getValue().asString();
        }
    }
}

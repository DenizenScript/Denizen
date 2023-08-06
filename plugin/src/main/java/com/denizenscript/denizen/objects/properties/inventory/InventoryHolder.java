package com.denizenscript.denizen.objects.properties.inventory;

import com.denizenscript.denizen.objects.*;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.core.ScriptTag;
import com.denizenscript.denizencore.objects.properties.ObjectProperty;
import com.denizenscript.denizencore.objects.properties.PropertyParser;
import org.bukkit.event.inventory.InventoryType;

public class InventoryHolder extends ObjectProperty<InventoryTag, ObjectTag> {

    public static boolean describes(InventoryTag inventory) {
        return true;
    }

    @Override
    public ObjectTag getPropertyValue() {
        ObjectTag holder = object.getIdHolder();
        if (holder == null || (object.getIdType().equals("generic") && object.getInventoryType() == InventoryType.CHEST)) {
            return null;
        }
        else {
            return holder;
        }
    }

    @Override
    public void setPropertyValue(ObjectTag param, Mechanism mechanism) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getPropertyId() {
        return "holder";
    }

    public static void register() {

        // <--[tag]
        // @attribute <InventoryTag.id_holder>
        // @returns ObjectTag
        // @group properties
        // @description
        // Returns Denizen's holder ID for this inventory. (player object, location object, etc.)
        // -->
        PropertyParser.registerTag(InventoryHolder.class, ObjectTag.class, "id_holder", (attribute, prop) -> {
            return prop.object.getIdHolder();
        });

        // <--[tag]
        // @attribute <InventoryTag.script>
        // @returns ScriptTag
        // @group properties
        // @description
        // Returns the script that this inventory came from (if any).
        // -->
        PropertyParser.registerTag(InventoryHolder.class, ScriptTag.class, "script", (attribute, prop) -> {
            ObjectTag holder = prop.object.getIdHolder();
            if (holder instanceof ScriptTag) {
                return ((ScriptTag) holder).validate();
            }
            return null;
        });
    }
}

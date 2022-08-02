package com.denizenscript.denizen.objects.properties.inventory;

import com.denizenscript.denizen.objects.*;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.core.ScriptTag;
import com.denizenscript.denizencore.objects.properties.Property;
import com.denizenscript.denizencore.objects.properties.PropertyParser;
import org.bukkit.event.inventory.InventoryType;

public class InventoryHolder implements Property {

    public static boolean describes(ObjectTag inventory) {
        // All inventories can have a holder
        return inventory instanceof InventoryTag;
    }

    public static InventoryHolder getFrom(ObjectTag inventory) {
        if (!describes(inventory)) {
            return null;
        }
        return new InventoryHolder((InventoryTag) inventory);
    }

    public static final String[] handledMechs = new String[] {
    };

    InventoryTag inventory;

    public InventoryHolder(InventoryTag inventory) {
        this.inventory = inventory;
    }

    @Override
    public String getPropertyString() {
        ObjectTag holder = inventory.getIdHolder();
        if (holder == null || (inventory.getIdType().equals("generic") && inventory.getInventoryType() == InventoryType.CHEST)) {
            return null;
        }
        else {
            return holder.identify();
        }
    }

    @Override
    public String getPropertyId() {
        return "holder";
    }

    public static void registerTags() {

        // <--[tag]
        // @attribute <InventoryTag.id_holder>
        // @returns ObjectTag
        // @group properties
        // @description
        // Returns Denizen's holder ID for this inventory. (player object, location object, etc.)
        // -->
        PropertyParser.<InventoryHolder, ObjectTag>registerTag(ObjectTag.class, "id_holder", (attribute, object) -> {
            return object.inventory.getIdHolder();
        });

        // <--[tag]
        // @attribute <InventoryTag.script>
        // @returns ScriptTag
        // @group properties
        // @description
        // Returns the script that this inventory came from (if any).
        // -->
        PropertyParser.<InventoryHolder, ScriptTag>registerTag(ScriptTag.class, "script", (attribute, object) -> {
            ObjectTag holder = object.inventory.getIdHolder();
            if (holder instanceof ScriptTag) {
                return ((ScriptTag) holder).validate();
            }
            return null;
        });
    }

    @Override
    public void adjust(Mechanism mechanism) {
    }
}

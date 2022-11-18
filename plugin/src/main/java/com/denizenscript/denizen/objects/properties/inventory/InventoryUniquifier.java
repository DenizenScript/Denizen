package com.denizenscript.denizen.objects.properties.inventory;

import com.denizenscript.denizen.objects.InventoryTag;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.properties.Property;

public class InventoryUniquifier implements Property {

    public static boolean describes(ObjectTag inventory) {
        return inventory instanceof InventoryTag
                && ((InventoryTag) inventory).isGeneric();
    }

    public static InventoryUniquifier getFrom(ObjectTag inventory) {
        if (!describes(inventory)) {
            return null;
        }
        return new InventoryUniquifier((InventoryTag) inventory);
    }

    public static final String[] handledMechs = new String[] {
    }; // The mechanism exists as part of the internal load sequence.

    InventoryTag inventory;

    public InventoryUniquifier(InventoryTag inventory) {
        this.inventory = inventory;
    }

    @Override
    public String getPropertyString() {
        if (inventory.uniquifier == null || inventory.isSaving) {
            return null;
        }
        return String.valueOf(inventory.uniquifier);
    }

    @Override
    public String getPropertyId() {
        return "uniquifier";
    }

    public static void register() {
        // Intentionally no tags.
    }

    @Override
    public void adjust(Mechanism mechanism) {
    }
}

package com.denizenscript.denizen.objects.properties.inventory;

import com.denizenscript.denizen.objects.InventoryTag;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.properties.ObjectProperty;

public class InventoryUniquifier extends ObjectProperty<InventoryTag, ElementTag> {

    public static boolean describes(InventoryTag inventory) {
        return inventory.isGeneric();
    }

    @Override
    public ElementTag getPropertyValue() {
        if (object.uniquifier == null || object.isSaving) {
            return null;
        }
        return new ElementTag(object.uniquifier);
    }

    @Override
    public void setPropertyValue(ElementTag val, Mechanism mechanism) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getPropertyId() {
        return "uniquifier";
    }

    public static void register() {
        // Intentionally no tags.
    }
}

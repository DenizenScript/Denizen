package com.denizenscript.denizen.paper.properties;

import com.denizenscript.denizen.objects.ItemTag;
import com.denizenscript.denizen.objects.properties.item.ItemProperty;
import com.denizenscript.denizen.paper.utilities.DataComponentAdapter;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.core.ListTag;
import io.papermc.paper.datacomponent.DataComponentType;

public class ItemRemoved extends ItemProperty<ListTag> {

    // <--[property]
    // @object ItemTag
    // @name removed
    // @input ListTag
    // @description
    // Controls the properties explicitly removed from an item.
    // This can be used to remove item's default behavior, such as making consumable items un-consumable.
    // See also <@link language Item Components>.
    // -->

    public static boolean describes(ItemTag item) {
        return !item.getItemStack().isEmpty();
    }

    @Override
    public ListTag getPropertyValue() {
        return new ListTag(getMaterial().getDefaultDataTypes(),
                componentType -> getItemStack().isDataOverridden(componentType) && !getItemStack().hasData(componentType),
                componentType -> new ElementTag(componentType.key().asMinimalString(), true));
    }

    @Override
    public boolean isDefaultValue(ListTag value) {
        return value.isEmpty();
    }

    @Override
    public void setPropertyValue(ListTag value, Mechanism mechanism) {
        for (String input : value) {
            DataComponentType componentType = DataComponentAdapter.getComponentType(input);
            if (componentType == null) {
                mechanism.echoError("Invalid type to remove '" + input + "' specified: must be a valid property or item component name.");
                continue;
            }
            getItemStack().unsetData(componentType);
        }
    }

    @Override
    public String getPropertyId() {
        return "removed";
    }

    public static void register() {
        autoRegister("removed", ItemRemoved.class, ListTag.class, false);
    }
}

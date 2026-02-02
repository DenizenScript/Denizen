package com.denizenscript.denizen.paper.properties;

import com.denizenscript.denizen.objects.ItemTag;
import com.denizenscript.denizen.objects.properties.item.ItemProperty;
import com.denizenscript.denizen.paper.datacomponents.DataComponentAdapter;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.core.ListTag;
import io.papermc.paper.datacomponent.DataComponentType;

public class ItemRemovedComponents extends ItemProperty<ListTag> {

    // <--[property]
    // @object ItemTag
    // @name removed_components
    // @input ListTag
    // @description
    // Controls the item components explicitly removed from an item.
    // This can be used to remove item's default behavior, such as making consumable items non-consumable.
    // See also <@link language Item Components>.
    // -->

    public static boolean describes(ItemTag item) {
        return !item.getItemStack().isEmpty();
    }

    public boolean isRemoved(DataComponentType componentType) {
        return getItemStack().isDataOverridden(componentType) && !getItemStack().hasData(componentType);
    }

    @Override
    public ListTag getPropertyValue() {
        return new ListTag(getMaterial().getDefaultDataTypes(), this::isRemoved, componentType -> new ElementTag(componentType.key().asMinimalString(), true));
    }

    @Override
    public boolean isDefaultValue(ListTag value) {
        return value.isEmpty();
    }

    @Override
    public void setPropertyValue(ListTag value, Mechanism mechanism) {
        for (DataComponentType componentType : getMaterial().getDefaultDataTypes()) {
            if (isRemoved(componentType)) {
                getItemStack().resetData(componentType);
            }
        }
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
        return "removed_components";
    }

    public static void register() {
        autoRegister("removed_components", ItemRemovedComponents.class, ListTag.class, false);
    }
}

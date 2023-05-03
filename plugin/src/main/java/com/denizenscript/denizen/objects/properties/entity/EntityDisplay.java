package com.denizenscript.denizen.objects.properties.entity;

import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.core.ElementTag;
import org.bukkit.entity.ItemDisplay;

public class EntityDisplay extends EntityProperty<ElementTag> {

    // <--[property]
    // @object EntityTag
    // @name display
    // @input ElementTag
    // @description
    // The model transform an item display entity will display, can be any of <@link url https://hub.spigotmc.org/javadocs/spigot/org/bukkit/entity/ItemDisplay.ItemDisplayTransform.html>.
    // -->

    public static boolean describes(EntityTag entity) {
        return entity.getBukkitEntity() instanceof ItemDisplay;
    }

    @Override
    public ElementTag getPropertyValue() {
        return new ElementTag(as(ItemDisplay.class).getItemDisplayTransform());
    }

    @Override
    public boolean isDefaultValue(ElementTag value) {
        return value.asEnum(ItemDisplay.ItemDisplayTransform.class) == ItemDisplay.ItemDisplayTransform.NONE;
    }

    @Override
    public void setPropertyValue(ElementTag value, Mechanism mechanism) {
        if (mechanism.requireEnum(ItemDisplay.ItemDisplayTransform.class)) {
            as(ItemDisplay.class).setItemDisplayTransform(value.asEnum(ItemDisplay.ItemDisplayTransform.class));
        }
    }

    @Override
    public String getPropertyId() {
        return "display";
    }

    public static void register() {
        autoRegister("display", EntityDisplay.class, ElementTag.class, false);
    }
}

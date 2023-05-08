package com.denizenscript.denizen.objects.properties.entity;

import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.core.ElementTag;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.TextDisplay;

public class EntityDisplay extends EntityProperty<ElementTag> {

    // <--[property]
    // @object EntityTag
    // @name display
    // @input ElementTag
    // @synonyms EntityTag.display_transform, EntityTag.text_alignment
    // @description
    // For an item display entity this is the model transform it will display, can be any of <@link url https://hub.spigotmc.org/javadocs/spigot/org/bukkit/entity/ItemDisplay.ItemDisplayTransform.html>.
    // For a text display entity this is its text alignment, can be any of <@link url https://hub.spigotmc.org/javadocs/spigot/org/bukkit/entity/TextDisplay.html>.
    // -->

    public static boolean describes(EntityTag entity) {
        return entity.getBukkitEntity() instanceof ItemDisplay || entity.getBukkitEntity() instanceof TextDisplay;
    }

    @Override
    public ElementTag getPropertyValue() {
        if (getEntity() instanceof ItemDisplay itemDisplay) {
            return new ElementTag(itemDisplay.getItemDisplayTransform());
        }
        return new ElementTag(as(TextDisplay.class).getAlignment());
    }

    @Override
    public boolean isDefaultValue(ElementTag value) {
        if (getEntity() instanceof ItemDisplay) {
            return value.asEnum(ItemDisplay.ItemDisplayTransform.class) == ItemDisplay.ItemDisplayTransform.NONE;
        }
        return value.asEnum(TextDisplay.TextAlignment.class) == TextDisplay.TextAlignment.CENTER;
    }

    @Override
    public void setPropertyValue(ElementTag value, Mechanism mechanism) {
        if (getEntity() instanceof ItemDisplay itemDisplay) {
            if (mechanism.requireEnum(ItemDisplay.ItemDisplayTransform.class)) {
                itemDisplay.setItemDisplayTransform(value.asEnum(ItemDisplay.ItemDisplayTransform.class));
            }
        }
        else if (mechanism.requireEnum(TextDisplay.TextAlignment.class)) {
            as(TextDisplay.class).setAlignment(value.asEnum(TextDisplay.TextAlignment.class));
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

package com.denizenscript.denizen.objects.properties.entity;

import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizen.utilities.PaperAPITools;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.core.ElementTag;
import org.bukkit.entity.TextDisplay;

public class EntityText extends EntityProperty<ElementTag> {

    // <--[property]
    // @object EntityTag
    // @name text
    // @input ElementTag
    // @description
    // A text display entity's text, supports new lines.
    // -->

    public static boolean describes(EntityTag entity) {
        return entity.getBukkitEntity() instanceof TextDisplay;
    }

    @Override
    public ElementTag getPropertyValue() {
        return new ElementTag(PaperAPITools.instance.getText(as(TextDisplay.class)), true);
    }

    @Override
    public boolean isDefaultValue(ElementTag value) {
        return value.asString().isEmpty();
    }

    @Override
    public void setPropertyValue(ElementTag value, Mechanism mechanism) {
        PaperAPITools.instance.setText(as(TextDisplay.class), value.asString());
    }

    @Override
    public String getPropertyId() {
        return "text";
    }

    public static void register() {
        autoRegister("text", EntityText.class, ElementTag.class, false);
    }
}

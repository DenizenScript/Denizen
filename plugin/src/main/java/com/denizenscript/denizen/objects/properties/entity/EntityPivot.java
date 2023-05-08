package com.denizenscript.denizen.objects.properties.entity;

import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.core.ElementTag;
import org.bukkit.entity.Display;

public class EntityPivot extends EntityProperty<ElementTag> {

    // <--[property]
    // @object EntityTag
    // @name pivot
    // @input ElementTag
    // @synonyms EntityTag.billboard
    // @description
    // A display entity's pivot point/axes, can be any of <@link url https://hub.spigotmc.org/javadocs/spigot/org/bukkit/entity/Display.Billboard.html>.
    // -->

    public static boolean describes(EntityTag entity) {
        return entity.getBukkitEntity() instanceof Display;
    }

    @Override
    public ElementTag getPropertyValue() {
        return new ElementTag(as(Display.class).getBillboard());
    }

    @Override
    public boolean isDefaultValue(ElementTag value) {
        return value.asEnum(Display.Billboard.class) == Display.Billboard.FIXED;
    }

    @Override
    public void setPropertyValue(ElementTag value, Mechanism mechanism) {
        if (mechanism.requireEnum(Display.Billboard.class)) {
            as(Display.class).setBillboard(value.asEnum(Display.Billboard.class));
        }
    }

    @Override
    public String getPropertyId() {
        return "pivot";
    }

    public static void register() {
        autoRegister("pivot", EntityPivot.class, ElementTag.class, false);
    }
}

package com.denizenscript.denizen.objects.properties.entity;

import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.properties.Property;
import com.denizenscript.denizencore.objects.properties.PropertyParser;
import org.bukkit.entity.WitherSkull;

public class EntityCharged implements Property {

    public static boolean describes(ObjectTag entity) {
        return entity instanceof EntityTag && ((EntityTag) entity).getBukkitEntity() instanceof WitherSkull;
    }

    public static EntityCharged getFrom(ObjectTag entity) {
        if (!describes(entity)) {
            return null;
        }
        else {
            return new EntityCharged((EntityTag) entity);
        }
    }

    public static final String[] handledMechs = new String[] {
            "charged"
    };

    private EntityCharged(EntityTag entity) {
        this.entity = entity;
    }

    EntityTag entity;

    @Override
    public String getPropertyString() {
        return ((WitherSkull) entity.getBukkitEntity()).isCharged() ? "true" : "false";
    }

    @Override
    public String getPropertyId() {
        return "charged";
    }

    public static void registerTags() {

        // <--[tag]
        // @attribute <EntityTag.charged>
        // @returns ElementTag(Boolean)
        // @mechanism EntityTag.charged
        // @group properties
        // @description
        // If the entity is wither_skull, returns whether the skull is charged.
        // -->
        PropertyParser.<EntityCharged>registerTag("charged", (attribute, object) -> {
            return new ElementTag(((WitherSkull) object.entity.getBukkitEntity()).isCharged());
        });
    }

    @Override
    public void adjust(Mechanism mechanism) {

        // <--[mechanism]
        // @object EntityTag
        // @name charged
        // @input ElementTag(Boolean)
        // @description
        // If the entity is wither_skull, changes whether the skull is charged.
        // @tags
        // <EntityTag.charged>
        // -->
        if (mechanism.matches("charged") && mechanism.requireBoolean()) {
            ((WitherSkull) entity.getBukkitEntity()).setCharged(mechanism.getValue().asBoolean());
        }
    }
}

package com.denizenscript.denizen.objects.properties.entity;

import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.properties.Property;
import com.denizenscript.denizencore.tags.Attribute;
import org.bukkit.entity.Spellcaster;

public class EntitySpell implements Property {

    public static boolean describes(ObjectTag entity) {
        return entity instanceof EntityTag && ((EntityTag) entity).getBukkitEntity() instanceof Spellcaster;
    }

    public static EntitySpell getFrom(ObjectTag entity) {
        if (!describes(entity)) {
            return null;
        }
        else {
            return new EntitySpell((EntityTag) entity);
        }
    }

    public static final String[] handledTags = new String[] {
            "spell"
    };

    public static final String[] handledMechs = new String[] {
            "spell"
    };

    private EntitySpell(EntityTag entity) {
        dentity = entity;
    }

    EntityTag dentity;

    @Override
    public String getPropertyString() {
        return ((Spellcaster) dentity.getBukkitEntity()).getSpell().toString();
    }

    @Override
    public String getPropertyId() {
        return "spell";
    }

    @Override
    public ObjectTag getObjectAttribute(Attribute attribute) {

        if (attribute == null) {
            return null;
        }

        // <--[tag]
        // @attribute <EntityTag.spell>
        // @returns ElementTag
        // @mechanism EntityTag.spell
        // @group properties
        // @description
        // Returns the spell the entity is currently casting.
        // Can be: <@link url https://hub.spigotmc.org/javadocs/spigot/org/bukkit/entity/Spellcaster.Spell.html>
        // -->
        if (attribute.startsWith("spell")) {
            return new ElementTag(((Spellcaster) dentity.getBukkitEntity()).getSpell())
                    .getObjectAttribute(attribute.fulfill(1));
        }

        return null;
    }

    @Override
    public void adjust(Mechanism mechanism) {

        // <--[mechanism]
        // @object EntityTag
        // @name spell
        // @input ElementTag
        // @description
        // Sets the spell the entity should cast. Valid spells are: <@link url https://hub.spigotmc.org/javadocs/spigot/org/bukkit/entity/Spellcaster.Spell.html>
        // @tags
        // <EntityTag.spell>
        // -->
        if (mechanism.matches("spell") && mechanism.requireEnum(Spellcaster.Spell.class)) {
            ((Spellcaster) dentity.getBukkitEntity()).setSpell(Spellcaster.Spell.valueOf(mechanism.getValue().asString().toUpperCase()));
        }
    }
}

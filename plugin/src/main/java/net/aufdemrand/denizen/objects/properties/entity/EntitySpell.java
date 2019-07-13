package net.aufdemrand.denizen.objects.properties.entity;

import net.aufdemrand.denizen.objects.dEntity;
import com.denizenscript.denizencore.objects.Element;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.dObject;
import com.denizenscript.denizencore.objects.properties.Property;
import com.denizenscript.denizencore.tags.Attribute;
import org.bukkit.entity.Spellcaster;

public class EntitySpell implements Property {

    public static boolean describes(dObject entity) {
        return entity instanceof dEntity && ((dEntity) entity).getBukkitEntity() instanceof Spellcaster;
    }

    public static EntitySpell getFrom(dObject entity) {
        if (!describes(entity)) {
            return null;
        }
        else {
            return new EntitySpell((dEntity) entity);
        }
    }

    public static final String[] handledTags = new String[] {
            "spell"
    };

    public static final String[] handledMechs = new String[] {
            "spell"
    };


    ///////////////////
    // Instance Fields and Methods
    /////////////

    private EntitySpell(dEntity entity) {
        dentity = entity;
    }

    dEntity dentity;

    /////////
    // Property Methods
    ///////

    @Override
    public String getPropertyString() {
        return ((Spellcaster) dentity.getBukkitEntity()).getSpell().toString();
    }

    @Override
    public String getPropertyId() {
        return "spell";
    }

    ///////////
    // dObject Attributes
    ////////

    @Override
    public String getAttribute(Attribute attribute) {

        if (attribute == null) {
            return "null";
        }

        // <--[tag]
        // @attribute <e@entity.spell>
        // @returns Element
        // @mechanism dEntity.spell
        // @group properties
        // @description
        // Returns the spell the entity is currently casting.
        // Can be: <@link url https://hub.spigotmc.org/javadocs/spigot/org/bukkit/entity/Spellcaster.Spell.html>
        // -->
        if (attribute.startsWith("spell")) {
            return new Element(((Spellcaster) dentity.getBukkitEntity()).getSpell().name())
                    .getAttribute(attribute.fulfill(1));
        }

        return null;
    }

    @Override
    public void adjust(Mechanism mechanism) {

        // <--[mechanism]
        // @object dEntity
        // @name spell
        // @input Element
        // @description
        // Sets the spell the entity should cast. Valid spells are: <@link url https://hub.spigotmc.org/javadocs/spigot/org/bukkit/entity/Spellcaster.Spell.html>
        // @tags
        // <e@entity.spell>
        // -->

        if (mechanism.matches("spell") && mechanism.requireEnum(false, Spellcaster.Spell.values())) {
            ((Spellcaster) dentity.getBukkitEntity()).setSpell(Spellcaster.Spell.valueOf(mechanism.getValue().asString().toUpperCase()));
        }
    }
}

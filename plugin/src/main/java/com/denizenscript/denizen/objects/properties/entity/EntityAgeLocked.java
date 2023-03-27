package com.denizenscript.denizen.objects.properties.entity;

import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.properties.PropertyParser;
import net.citizensnpcs.trait.Age;
import org.bukkit.entity.Breedable;

public class EntityAgeLocked extends EntityProperty {

    public static boolean describes(EntityTag entity) {
        return entity.getBukkitEntity() instanceof Breedable;
    }

    @Override
    public ElementTag getPropertyValue() {
        return as(Breedable.class).getAgeLock() ? new ElementTag(true) : null;
    }

    @Override
    public String getPropertyId() {
        return "age_locked";
    }

    public static void register() {

        // <--[tag]
        // @attribute <EntityTag.age_locked>
        // @returns ElementTag(Boolean)
        // @mechanism EntityTag.age_locked
        // @group properties
        // @description
        // Returns whether the entity is locked into it's current age.
        // -->
        PropertyParser.registerTag(EntityAgeLocked.class, ElementTag.class, "age_locked", (attribute, prop) -> {
            return new ElementTag(prop.as(Breedable.class).getAgeLock());
        });

        // <--[mechanism]
        // @object EntityTag
        // @name age_locked
        // @input ElementTag(Boolean)
        // @description
        // Sets whether the entity is locked into its current age.
        // @tags
        // <EntityTag.age_locked>
        // -->
        PropertyParser.registerMechanism(EntityAgeLocked.class, ElementTag.class, "age_locked", (prop, mechanism, input) -> {
            if (mechanism.requireBoolean()) {
                if (prop.object.isCitizensNPC()) {
                    prop.object.getDenizenNPC().getCitizen().getOrAddTrait(Age.class).setLocked(input.asBoolean());
                }
                else {
                    prop.as(Breedable.class).setAgeLock(input.asBoolean());
                }
            }
        });
    }
}

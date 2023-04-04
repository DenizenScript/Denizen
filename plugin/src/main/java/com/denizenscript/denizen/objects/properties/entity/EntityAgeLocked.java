package com.denizenscript.denizen.objects.properties.entity;

import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.core.ElementTag;
import net.citizensnpcs.trait.Age;
import org.bukkit.entity.Breedable;

public class EntityAgeLocked extends EntityProperty<ElementTag> {

    // <--[property]
    // @object EntityTag
    // @name age_locked
    // @input ElementTag(Boolean)
    // @description
    // Controls whether the entity is locked into its current age.
    // -->

    public static boolean describes(EntityTag entity) {
        return entity.getBukkitEntity() instanceof Breedable;
    }

    @Override
    public boolean isDefaultValue(ElementTag val) {
        return !val.asBoolean();
    }

    @Override
    public ElementTag getPropertyValue() {
        return new ElementTag(as(Breedable.class).getAgeLock());
    }

    @Override
    public void setPropertyValue(ElementTag param, Mechanism mechanism) {
        if (mechanism.requireBoolean()) {
            if (object.isCitizensNPC()) {
                object.getDenizenNPC().getCitizen().getOrAddTrait(Age.class).setLocked(param.asBoolean());
            }
            else {
                as(Breedable.class).setAgeLock(param.asBoolean());
            }
        }
    }

    @Override
    public String getPropertyId() {
        return "age_locked";
    }

    public static void register() {
        autoRegister("age_locked", EntityAgeLocked.class, ElementTag.class, false, "is_age_locked", "age_lock");

        // <--[tag]
        // @attribute <EntityTag.is_age_locked>
        // @returns ElementTag(Boolean)
        // @group properties
        // @deprecated use 'age_locked'.
        // @description
        // Deprecated in favor of <@link tag EntityTag.age_locked>.
        // -->

        // <--[mechanism]
        // @object EntityTag
        // @name age_lock
        // @input ElementTag(Boolean)
        // @deprecated use 'age_locked'.
        // @description
        // Deprecated in favor of <@link mechanism EntityTag.age_locked>.
        // -->
    }
}
